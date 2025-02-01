package com.example.progettolam.Worker;


import static androidx.core.content.ContentProviderCompat.requireContext;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.progettolam.R;

public class PeriodicNotificationWorker extends Worker {
    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 1;
    private String CHANNEL_ID = "periodic_messsage";
    private NotificationManagerCompat nm;
    public PeriodicNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        nm = NotificationManagerCompat.from(getApplicationContext());
        createNotificationChannel();
//        creatNotificationMessage();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle("Siamo al memento di fare più passi!")
                .setContentText("Risulta che non hai fatto tanti passi oggi!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        nm.notify(666,builder.build());
        Log.d("teg","sta lavorando");
        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }
    public void createNotificationChannel() {
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
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.createNotificationChannel(channel);
        }

    }


}
