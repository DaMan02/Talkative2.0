package com.dayal.talkative.activities.classes;

import android.support.v4.app.NotificationCompat;

import com.dayal.talkative.R;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.default_pic)
            .setContentTitle("My notification")
            .setContentText("Hello")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);


}
