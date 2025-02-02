package com.example.progettolam.transition;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.progettolam.R;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.example.progettolam.database.activityRecordDbHelper;
import com.example.progettolam.struct.Record;


import java.util.List;

public class UserActivityDetectionReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "detection_channel";
    private activityRecordDbHelper dpHelper;
    String ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION";
    private NotificationManagerCompat nm;
    private CharSequence activityList;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        dpHelper = new activityRecordDbHelper(context);
        CharSequence messageTitle;
        CharSequence messageBody = null;
        nm = NotificationManagerCompat.from(context.getApplicationContext());
        createNotificationChannel(context);
//        if (ActivityTransitionResult.hasResult(intent)) {
//            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
//            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
//                messageBody+="Activity: "+getActivityType(event.getActivityType())+" ";
//            }
//        }
//        messageTitle="prova";
//        if (ActivityRecognitionResult.hasResult(intent)) {
        if (intent!=null){
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
//            Bundle extra = intent.getExtras();
//            String sendPackge = intent.getPackage();
//            Log.d("ActivityRecognition","intent package is "+sendPackge);
//            if (extra!=null){
//                Log.d("ActivityRecognition","extras are not null----?>"+extra);
//            }
//            Log.d("ActivityRecognition","ActivityTransitionResult ->"+result+" questo e intent "+intent);

            if (result!=null) {
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                activityList+="Activity: "+getActivityType(event.getActivityType())+" in "+getTransitionType(event.getTransitionType());
            }
                messageTitle = "Sembra che sta funzionando";
                messageBody = activityList;
            }else {
                messageTitle="Activity Recognition result non ce";
            }
        }else {
            messageTitle = "No activity detectng";
            messageBody = "Do you wanna register it?";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        nm.notify(117, builder.build());
    }
    private String getActivityType(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE: return "In Vehicle";
            case DetectedActivity.STILL: return "Still";
            case DetectedActivity.WALKING: return "Walking";
            case DetectedActivity.ON_FOOT: return "On Foot";
            case DetectedActivity.RUNNING: return "Running";
            case DetectedActivity.UNKNOWN: return "Unknown";
            default: return "Unrecognized";
        }
    }
    private String getTransitionType(int type){
        switch (type){
            case 0: return "Enter";
            case 1: return "Exit";
            default: return "Unrecognized";
        }
    }
    public void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Periodic_Notification";
//            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
            notificationManager.createNotificationChannel(channel);
        }

    }
}
