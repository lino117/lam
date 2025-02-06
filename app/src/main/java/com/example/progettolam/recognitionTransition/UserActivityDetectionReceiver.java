package com.example.progettolam.recognitionTransition;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.progettolam.R;

import com.example.progettolam.chronometer.ChronometerService;
import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.example.progettolam.database.activityRecordDbHelper;


import java.util.Calendar;
import java.util.TimeZone;

public class UserActivityDetectionReceiver extends BroadcastReceiver {
    private RemoteViews remoteViews;
    private static final String CHANNEL_ID = "detection_channel";
    private activityRecordDbHelper dpHelper;
    private NotificationManagerCompat nm;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        dpHelper = new activityRecordDbHelper(context);
        String exitAcivity = "";
        String enterActivity = "";
        String messageTitle;
        String messageBody = null;
        nm = NotificationManagerCompat.from(context.getApplicationContext());
        createNotificationChannel(context);

        if (intent!=null){
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            if (result!=null) {
                for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                    if (getTransitionType(event.getTransitionType()).equals("Enter")) {
                        enterActivity = getActivityType(event.getActivityType());
                    } else {
                        exitAcivity = getActivityType(event.getActivityType());
                    }
            }
                messageTitle = "Sembra che stai "+enterActivity;
                messageBody = "Vuoi cominciare a registrare?";
            }else {
                messageTitle="Activity Recognition result non ce";
            }
        }else {
            messageTitle = "No activity detectng";
            messageBody = "Do you wanna register it?";
        }
        long startTime = getTimeRange()[0];
        long endTime = getTimeRange()[1];
        if (System.currentTimeMillis() >= startTime && System.currentTimeMillis() <= endTime){
            if (enterActivity.equals("Unrecognized")){
                Intent intentStartAutoRegistration = new Intent(context, ChronometerService.class);
                intentStartAutoRegistration.setAction("AUTO_REGISTRATION");
                intentStartAutoRegistration.putExtra("activity","Unknown");
                intentStartAutoRegistration.putExtra("chronometerState","onStart");
                intentStartAutoRegistration.putExtra("endTime",endTime);
                intentStartAutoRegistration.putExtra("endTimeActivity",System.currentTimeMillis());
                context.startService(intentStartAutoRegistration);
            }
        }else {
            nm.notify(117, creatNotification(context, messageTitle, messageBody, enterActivity));

        }
    }
    private String getActivityType(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE: return "Driving";
            case DetectedActivity.STILL: return "Sitting";
            case DetectedActivity.WALKING: return "Walking";
//            case DetectedActivity.ON_FOOT: return "In piede";
//            case DetectedActivity.RUNNING: return "Corsa";
//            case DetectedActivity.UNKNOWN: return "Unknown";
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "detection_channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context.getApplicationContext());
            notificationManager.createNotificationChannel(channel);
        }

    }
    public Notification creatNotification(Context context, String messageTitle, String messageBody, String enterActivity){
        Intent acceptIntent = new Intent(context, ChronometerService.class);
        acceptIntent.setAction("ACCEPT_REGISTRATION");
        acceptIntent.putExtra("activity",enterActivity);
        acceptIntent.putExtra("notification_id", 117);
        acceptIntent.putExtra("chronometerState","onStart");
        PendingIntent acceptPendingIntent = PendingIntent.getService(context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);

        Intent rejectIntent = new Intent(context, ChronometerService.class);
        rejectIntent.putExtra("notification_id", 117);
        rejectIntent.setAction("REJECT_REGISTRATION");
        PendingIntent rejectPendingIntent = PendingIntent.getService(context, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);

        return new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .addAction(R.drawable.accept, "Non adesso.", rejectPendingIntent)
                .addAction(R.drawable.reject, "Registra ora!", acceptPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
    }
    private long[] getTimeRange(){
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 23);  // Imposta l'orario di inizio 21:00 del giorno corrente
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startTime = calendar.getTimeInMillis();

        calendar.add(Calendar.DATE, 1);  // Aggiungi un giorno alla data di inizio)
        calendar.set(Calendar.HOUR_OF_DAY, 8);  // Imposta l'orario di fine 8:00 del giorno successivo
        long endTime = calendar.getTimeInMillis();
        return new long[]{startTime, endTime};
    }
}
