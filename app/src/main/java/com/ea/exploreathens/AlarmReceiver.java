package com.ea.exploreathens;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("mynotification", "Notification received!");
        //Trigger the notification
        NotificationPublisher.showNotification(context, MainActivity.class,
                "You have 5 unwatched videos", "Watch them now?");
    }

}
