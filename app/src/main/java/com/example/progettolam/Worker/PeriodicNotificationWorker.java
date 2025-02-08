package com.example.progettolam.Worker;


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.progettolam.R;
import com.example.progettolam.database.ActivityRecordDbHelper;

public class PeriodicNotificationWorker extends Worker {
    private String CHANNEL_ID = "periodic_messsage";
    private NotificationManagerCompat nm;
    private ActivityRecordDbHelper dpHelper;
    private CharSequence messageStep,messageStepBody;

    public PeriodicNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        createNotificationChannel();
        dpHelper = new ActivityRecordDbHelper(context);
    }

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        Cursor cursor = dpHelper.getYesterdaySteps(dpHelper.getReadableDatabase());
        if (cursor.moveToNext()){
            int steps = cursor.getInt(0);
            if (steps < 3000){
                messageStep = "Vedo che ieri non hai fatto tanti passi!";
                messageStepBody="E' consigliato di fare piu di 3000 passi al giorno";
            }else if (steps< 10000) {
                messageStep = "Ieri hai fatto piu di 3000 passi!";
                messageStepBody= "Prova a fare piu passi oggi per stare ancora meglio!";
            }else{
                messageStep = "Congratulazione! Risulta che hai fatto piu di 10000 passi ieri!";
                messageStepBody= "Continua cosi e mantieni questo stile di vita!";
            }
            Log.d("periodic","Steps: " + steps);
        }else {
            messageStep="Vedo che non hai fatto nessun passo ieri!";
            messageStepBody="E ora di iniziare di registrare i passi fatti";
        }
        nm = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle(messageStep)
                .setContentText(messageStepBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        nm.notify(666,builder.build());

        // Indicate whether the work finished successfully with the Result
        return Result.success();
    }
    public void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Periodic_Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
            notificationManager.createNotificationChannel(channel);
        }

    }

}
