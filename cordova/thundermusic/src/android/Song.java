package fr.litarvan.thundermusic.core;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

public class Song
{
    private String id;
    private String title;
    private String artist;
    private File thumbnail;
    private File file;

    public Song(String id, String title, String artist, File thumbnail, File file)
    {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.thumbnail = thumbnail;
        this.file = file;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public String getArtist()
    {
        return artist;
    }

    public File getThumbnail()
    {
        return thumbnail;
    }

    public void setThumbnail(File thumbnail)
    {
        this.thumbnail = thumbnail;
    }

    public File getFile()
    {
        return file;
    }

    public static Song fromJSON(JSONObject song) throws JSONException
    {
        File image = song.has("thumbnail") ? new File(song.getString("thumbnail")) : null;

        return new Song(
            song.getString("id"),
            song.getString("title"),
            song.getString("artist"),
            image,
            new File(song.getString("file"))
        );
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject result = new JSONObject();

        result.put("id", id);
        result.put("title", title);
        result.put("artist", artist);

        if (thumbnail != null) {
            result.put("thumbnail", thumbnail.getAbsolutePath());
        }

        result.put("file", file.getAbsolutePath());

        return result;
    }
}
