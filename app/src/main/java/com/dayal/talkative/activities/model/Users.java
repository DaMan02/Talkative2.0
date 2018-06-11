package com.dayal.talkative.activities.model;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Users {
    public String name;
    public String status;
    public String image;
    public String thumb_Image;

    public Users() {
    }

    public Users(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
    }


    public String getThumb_Image() {
        return thumb_Image;
    }

    public void setThumb_Image(String thumb_Image) {
        this.thumb_Image = thumb_Image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
