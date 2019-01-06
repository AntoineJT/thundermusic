package fr.litarvan.thundermusic.core;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

public class Song
{
    private String id;
    private String title;
    private String artist;
    private File image;
    private File file;

    public Song(String id, String title, String artist, File image, File file)
    {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.image = image;
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

    public File getImage()
    {
        return image;
    }

    public void setImage(File image)
    {
        this.image = image;
    }

    public File getFile()
    {
        return file;
    }

    public static Song fromJSON(JSONObject song) throws JSONException
    {
        File image = song.has("image") ? new File(song.getString("image")) : null;

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

        if (image != null) {
            //result.put("image", image.getAbsolutePath());
            result.put("thumbnail", image.getAbsolutePath());
        }

        //result.put("file", file.getAbsolutePath());
        result.put("url", file.getAbsolutePath());

        return result;
    }
}
