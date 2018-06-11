package com.dayal.talkative.activities.classes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Friends {

    public String date;

    public Friends(){

    }

    public Friends(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
