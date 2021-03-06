package com.example.countdowntimer;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

public class Notifications extends Application {
    public static final String CHANNEL_1_ID = "channel1";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel(){
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
           NotificationChannel channel1 = new NotificationChannel(
                   CHANNEL_1_ID,
                   "Channel1",
                   NotificationManager.IMPORTANCE_HIGH
           );
           channel1.setDescription("This is Channel1");


           NotificationManager manager = getSystemService(NotificationManager.class);
           manager.createNotificationChannel(channel1);
       }
    }

}
