package fr.litarvan.thundermusic.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import android.content.Context;
import android.os.Environment;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.jaudiotagger.tag.images.AndroidArtwork;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.reference.PictureTypes;
import org.json.JSONArray;
import org.json.JSONObject;

public class MusicManager
{
    private Context context;
    private EventManager eventManager;
    private MusicPlayer player;

    private File songFolder;
    private File cacheFile;
    private List<Song> songs;

    public MusicManager(Context context, EventManager eventManager)
    {
        this.context = context;
        this.eventManager = eventManager;
    }

    public void load() throws Exception
    {
        songFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        songFolder.mkdirs();

        cacheFile = new File(context.getCacheDir(), "musics.json");
        songs = new ArrayList<>();

        if (cacheFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheFile)))) {
                StringBuilder result = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                JSONArray songs = new JSONArray(result.toString());
                for (int i = 0; i < songs.length(); i++) {
                    this.songs.add(Song.fromJSON(songs.getJSONObject(i)));
                }
            }
        }

        File[] files = songFolder.listFiles();
        for (File file : files) {
            if (!file.getName().endsWith(".mp3")) {
                continue;
            }

            Song original = null;
            for (Song song : songs) {
                if (song.getFile().getAbsolutePath().equals(file.getAbsolutePath())) {
                    original = song;
                    break;
                }
            }

            if (original == null) {
                AudioFile audio = AudioFileIO.read(file);
                Tag tags = audio.getTag();

                Song song = create(new SongToDownload(
                    UUID.randomUUID().toString().substring(0, 10),
                    tags.getFirst(FieldKey.TITLE),
                    tags.getFirst(FieldKey.ARTIST),
                    null
                ), file, null, false);

                updateThumb(song);
            } else if (!original.getImage().exists()) {
                updateThumb(original);
            }
        }

        updateCache();
    }

    public File getFile(SongToDownload song)
    {
        String file = song.getArtist() + " - " + song.getTitle() + " (" + song.getId() + ").mp3";
        file = file.replaceAll("[/:|\\[\\]]", "s");

        return new File(songFolder, file);
    }

    public Song create(SongToDownload downloaded, File file, byte[] thumbnail, boolean updateCache) throws Exception
    {
        Song song = new Song(
            downloaded.getId(),
            downloaded.getTitle(),
            downloaded.getArtist(),
            null,
            file
        );

        songs.add(song);

        writeTags(song, thumbnail);
        updateThumb(song);

        if (updateCache) {
            updateCache();
        }

        return song;
    }

    public void update(Song song) throws Exception
    {
        writeTags(song, null);
        updateCache();
    }

    public void remove(Song song) throws Exception
    {
        for (int i = 0; i < songs.size(); i++)
        {
            if (songs.get(i).getId().equals(song.getId()))
            {
                songs.remove(i);
            }
        }

        updateCache();
    }

    protected void updateThumb(Song song) throws Exception
    {
        File thumb = File.createTempFile("thumb-" + song.getId() + "-", ".png", context.getCacheDir());
        AudioFile audio = AudioFileIO.read(song.getFile());

        try (FileOutputStream out = new FileOutputStream(thumb))
        {
            out.write(audio.getTag().getFirstArtwork().getBinaryData());
        }

        song.setImage(thumb);
    }

    protected void writeTags(Song song, byte[] thumbnail) throws Exception
    {
        AudioFile audio = AudioFileIO.read(song.getFile());
        Tag tags = audio.getTag();

        tags.setField(FieldKey.CUSTOM1, song.getId());
        tags.setField(FieldKey.ARTIST, song.getArtist());
        tags.setField(FieldKey.TITLE, song.getTitle());

        if (thumbnail != null) {
            Artwork artwork = new AndroidArtwork();
            artwork.setBinaryData(thumbnail);
            artwork.setMimeType(ImageFormats.getMimeTypeForBinarySignature(thumbnail));
            artwork.setDescription("");
            artwork.setPictureType(PictureTypes.DEFAULT_ID);

            tags.getArtworkList().add(artwork);
        }

        audio.commit();
    }

    protected void updateCache() throws Exception
    {
        Collections.sort(songs, new Comparator<Song>() {
            @Override
            public int compare(Song song, Song t1) {
                return song.getTitle().compareToIgnoreCase(t1.getTitle());
            }
        });

        JSONArray songs = new JSONArray();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cacheFile)))) {
            for (Song song : this.songs) {
                songs.put(song.toJSON()); // TODO: Check this
            }

            writer.write(songs.toString());
        }

        JSONObject event = new JSONObject();
        JSONArray array = new JSONArray();

        for (Song song : this.songs) {
            array.put(song.toJSON()); // TODO: Check this
        }

        event.put("type", "update");
        event.put("songs", array);

        eventManager.emit(event);
    }

    public void setPlayer(MusicPlayer player)
    {
        this.player = player;
    }

    public List<Song> getSongs()
    {
        return songs;
    }
}