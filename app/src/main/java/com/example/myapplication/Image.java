package com.example.myapplication;

import java.io.Serializable;

/**
 * Created by Lincoln on 04/04/16.
 */
public class Image implements Serializable{
    private String name;
    private String albumName;
    private String timestamp;
    private String caption;
    private String path;
    private String time;

    public Image() {
    }

    public Image(String albumName,String name, String timestamp,String path, String caption) {
        this.albumName = albumName;
        this.name = name;
        this.timestamp = timestamp;
        this.caption = caption;
        this.path= path;
        this.time = CONSTANTS.converToTime(timestamp);

    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getCaption() {
        return caption;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
