package com.example.progettolam.transition;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.progettolam.R;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.example.progettolam.database.activityRecordDbHelper;
import com.example.progettolam.struct.Record;
//import com.huawei.hms.location.ActivityConversionData;
//import com.huawei.hms.location.ActivityConversionResponse;


import java.util.List;

public class UserActivityDetectionReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "detection_channel";
    private activityRecordDbHelper dpHelper;
    String ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION";
    private NotificationManagerCompat nm;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        dpHelper = new activityRecordDbHelper(context);
        nm = NotificationManagerCompat.from(context.getApplicationContext());
        createNotificationChannel(context);
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            int type = mostProbableActivity.getType();
            int confidence = mostProbableActivity.getConfidence();

            dpHelper.insertData(setRecord());
            String activityType = getActivityType(type);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.notif_icon)
                    .setContentTitle("Abbiamo notato che stai "+activityType)
                    .setContentText("Vuoi registarla?")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            nm.notify(117,builder.build());
//            Toast.makeText(context.getApplicationContext(), "Activity: " + activityType + " Confidence: " + confidence, Toast.LENGTH_LONG).show();
//            Log.d("ActivityRecognition", "Activity: " + activityType + " Confidence: " + confidence);
        }else {
            Log.d("ActivityRecognition","detection has no result");
            Toast.makeText(context.getApplicationContext(), "nulla ce ", Toast.LENGTH_LONG).show();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context.getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.notif_icon)
                    .setContentTitle("Non sto sentendo la tua attivita")
                    .setContentText("Vuoi registarla?")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            nm.notify(118,builder.build());
        }
//
//        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_PROCESS_LOCATION.equals(action)) {
//                // 从intent中获取ActivityConversionResponse，从response中提取活动转换事件列表
//                ActivityConversionResponse activityConversionResponse = ActivityConversionResponse.getDataFromIntent(intent);
//                List<ActivityConversionData> list = activityConversionResponse.getActivityConversionDatas();
//                // TODO: 您可以对获取的活动转换事件列表，进行相应的处理
//                Log.d("ActivityRecognition","ActivityConversionResponse"+list.toString());
//            }
//        }
    }
    private Record setRecord(){
        Record record = new Record();
        record.setNameActivity(getActivityType(1));
        record.setStep(117);

        return record;
    }
    private String getActivityType(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE: return "In Vehicle";
            case DetectedActivity.STILL: return "Still";
            case DetectedActivity.WALKING: return "Walking";
            case DetectedActivity.UNKNOWN: return "Unknown";
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
