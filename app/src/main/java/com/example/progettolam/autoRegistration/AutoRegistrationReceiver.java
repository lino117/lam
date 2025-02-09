package com.example.progettolam.autoRegistration;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.progettolam.R;
import com.example.progettolam.database.ActivityRecordDbHelper;
import com.example.progettolam.struct.Record;
import com.example.progettolam.timeConverter.TimeConverter;

import java.text.SimpleDateFormat;

public class AutoRegistrationReceiver extends BroadcastReceiver {
    TimeConverter timeConverter = new TimeConverter();
    private NotificationManagerCompat nm;

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        ActivityRecordDbHelper dbHelper = new ActivityRecordDbHelper(context);
        if (intent != null) {
            String action = intent.getAction();
            // parte inseirimento dati
            if (action.equals("com.example.chronometer.AUTO_REGISTRATION_END")) {
                long duration = intent.getIntExtra("duration", 0);
                String activity = intent.getStringExtra("activity");
                long endTimeActivity = intent.getLongExtra("endTimeActivity", 0);
                Record record = createRecord(duration, endTimeActivity, activity);
                long id = dbHelper.insertData(record);
                if (id != -1) {
                    Log.d("Chrono", "dati inseriti correttamente");
                } else {
                    Log.d("Chrono", "dati non inseriti correttamente");
                }
                // parte crea una notifica con dati inseriti
                nm = NotificationManagerCompat.from(context);
                NotificationChannel channel = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    channel = new NotificationChannel("auto_registration", "autoRegistration", NotificationManager.IMPORTANCE_DEFAULT);
                }
                nm.createNotificationChannel(channel);
                nm.notify(118, createNotification(context, record));
                // parte che manda broadcast per terminare servizio di chronometro
                Log.d("Chrono", "termina service intent send to broadcast");

                Intent stopServiceIntent = new Intent("STOP_SERVICE_FROM_AUTO_REGISTRATION");
                context.sendBroadcast(stopServiceIntent);
            }

        }
    }

    private Record createRecord(long duration, long endTimeActivity, String selectedActivity) {
        long startTimeActivity = endTimeActivity - duration * 1000;
        Record record = new Record();
        record.setNameActivity(selectedActivity);
        record.setDuration((int) (duration));
        record.setStep(null);
        record.setStart_time(startTimeActivity % 86400000);
        record.setEnd_time(endTimeActivity % 86400000);
        record.setStart_day(timeConverter.setDayTimeToZero(startTimeActivity));
        record.setEnd_day(timeConverter.setDayTimeToZero(endTimeActivity));
        return record;
    }


    private Notification createNotification(Context context, Record record) {
        SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat daySdf = new SimpleDateFormat("dd/MM/yyyy");
        String messageTitle = "Abbiamo registrato un nuovo Record";
        String messageBody = "Nome attività: " + record.getNameActivity() + " " +
                "Durata: " + timeSdf.format(record.getDuration()) + " " +
                "Iniziato alle: " + timeSdf.format(record.getStart_time()) + " " + daySdf.format(record.getStart_day()) + " " +
                "Terminato alle: " + timeSdf.format(record.getEnd_time()) + " " + daySdf.format(record.getEnd_day());
        return new NotificationCompat.Builder(context, "auto_registration")
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .build();
    }

}
