package br.com.geekfox.socialpicture.json;

import java.io.Serializable;

/**
 * Created by Rafael Brasileiro on 21/06/2017.
 */

public class ImageData implements Serializable{

    private int id;
    private String image;
    private String extension;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
