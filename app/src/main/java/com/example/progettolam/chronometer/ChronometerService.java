package com.example.progettolam.chronometer;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.progettolam.MainActivity;
import com.example.progettolam.R;
import com.example.progettolam.autoRegistration.AutoRegistrationReceiver;
import com.example.progettolam.sharedPreferences.PrefsManager;
import com.example.progettolam.specialFeature.StepCounterService;
import com.example.progettolam.timeConverter.TimeConverter;

import java.util.Objects;

public class ChronometerService extends Service implements StepCounterService.StepListener {
    private long pauseOffset;
    private long chronometerBase;
    private Integer finalSteps = 0;
    private HandlerThread handlerThread;
    private Handler handler;
    private String activity;
    private boolean isBound = false;
    private StepCounterService stepCounterService;
    // parte per special features
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            stepCounterService = ((StepCounterService.Binder) service).getService();
            stepCounterService.setStepListener(ChronometerService.this);
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
    private boolean foregroundForRecognition;
    private NotificationManagerCompat nm;
    private RemoteViews remoteViews;
    private BroadcastReceiver updateReceiver;
    private final String START_FOREGROUND_SERVICE = "com.example.chronometer.START_FOREGROUND_SERVICE";
    private final String STOP_FOREGROUND_SERVICE = "com.example.chronometer.STOP_FOREGROUND_SERVICE";
    private boolean updateReceiverRegistered = false;
    private long endTimeAutoRegistration;
    private String serviceMode;
    private TimeConverter timeConverter;

    @SuppressLint("NewApi")
    @Override
    public void onCreate() {
        super.onCreate();
        handlerThread = new HandlerThread("ChronometerThread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        timeConverter = new TimeConverter();
        nm = NotificationManagerCompat.from(this);

        this.chronometerBase = SystemClock.elapsedRealtime();
        this.pauseOffset = 0;
        this.finalSteps = 0;
        this.foregroundForRecognition = false;
        this.serviceMode = "";
        this.activity=" ";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("chrono_channel", "Cronometro", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(START_FOREGROUND_SERVICE);
        filter.addAction(STOP_FOREGROUND_SERVICE);
        filter.addAction("STOP_SERVICE_FROM_AUTO_REGISTRATION");

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    String action = intent.getAction();
                    if (action.equals(STOP_FOREGROUND_SERVICE) && serviceMode.equals("foreground")) {
                        stopForeground(true);
                        foregroundForRecognition = false;
                        serviceMode = "inApp";
                        Log.d("Chrono revicer", "intent stop foreground activity"+intent.getStringExtra("activity"));
                        activity=intent.getStringExtra("activity");
                    } else if (action.equals(START_FOREGROUND_SERVICE) && serviceMode != "foreground") {
                        startForeground(1, createNotification());
                        foregroundForRecognition = true;
                        serviceMode = "foreground";
                    } else if (action.equals("STOP_SERVICE_FROM_AUTO_REGISTRATION")) {
                        stopSelf();
                    }
                } else {
                    Log.d("Chrono revicer", "intent nullo");
                }
            }
        };

        registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        updateReceiverRegistered = true;
        remoteViews = new RemoteViews(getPackageName(), R.layout.notification_layout);
        handler.post(new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                long duration = (SystemClock.elapsedRealtime() - chronometerBase) / 1000;

                if (serviceMode.equals("autoRegistration")) {
                    long endTimeActivity = timeConverter.toLocalTimeZone(System.currentTimeMillis());
                    if (endTimeActivity >= endTimeAutoRegistration) {
                        Intent stopAutoRegistrationIntent = new Intent(getApplicationContext(), AutoRegistrationReceiver.class);
                        stopAutoRegistrationIntent.setAction("com.example.chronometer.AUTO_REGISTRATION_END");
                        stopAutoRegistrationIntent.putExtra("duration", duration);
                        stopAutoRegistrationIntent.putExtra("activity", "Unknown");
                        stopAutoRegistrationIntent.putExtra("endTimeActivity", endTimeActivity);
                        sendBroadcast(stopAutoRegistrationIntent);
                    }
                } else {
                    Intent intent = new Intent("com.example.chronometer.UPDATE");
                    intent.putExtra("duration", duration);
                    intent.putExtra("steps", finalSteps);
                    intent.putExtra("activity", activity);
                    Log.d("Chrono",activity+" questo e attivita nel intent");
                    if (foregroundForRecognition) {
                        Log.d("Chrono","sto inviando da foreground");
                        remoteViews.setTextViewText(R.id.tv_foreground_duration, String.format("%02d:%02d:%02d", duration / 3600, duration / 60, duration % 60));
                        if (activity.equals("Walking")) {
                            remoteViews.setTextViewText(R.id.tv_foreground_sfNumber, String.valueOf(finalSteps));
                            remoteViews.setTextViewText(R.id.tv_foreground_specialFeature, "Steps");
                        }
                        Notification notification = createNotification();
                        nm.notify(1, notification);
                    } else {


                        sendBroadcast(intent);
                    }
                    handler.postDelayed(this, 1000);
                }


            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("Chrono revicer on startcomand","intent "+intent);
        if (intent != null) {
            // questo pezzo solo se arriva da qualche intent! non ci capisco piu nulla...
            String action = intent.getAction();

            if (action.equals("REJECT_REGISTRATION")) {
                if (updateReceiverRegistered) {
                    unregisterReceiver(updateReceiver);
                    updateReceiverRegistered = false;
                }
                nm.cancel(intent.getIntExtra("notification_id", 0));
                stopSelf();
            } else {
                String choronometerState = intent.getStringExtra("chronometerState");
//                activity = intent.getStringExtra("activity");
                activity = PrefsManager.getString("activity");
                Log.d("Chrono","activity in on start command: "+activity);
                // stabilire se intent arriva da activity oppure da recognition-transition
                if (action.equals("ACCEPT_REGISTRATION")) {
                    // 1 e' notification_id
                    startForeground(1, createNotification());
                    foregroundForRecognition = true;
                    nm.cancel(intent.getIntExtra("notification_id", 0));
                }
                if (action.equals("AUTO_REGISTRATION")) {
                    this.endTimeAutoRegistration = intent.getLongExtra("endTime", 0);
                }
                switch (Objects.requireNonNull(choronometerState)) {
                    case "onStart":
                        this.onStart();
                        if (isBound) {
                            stepCounterService.registerSensor();
                        }
                        break;
                    case "onPause":
                        this.onPause();
                        if (isBound) {
                            stepCounterService.onPause();
                            stepCounterService.unregisterSensor();

                        }
                        break;
                    case "onStop":
                        this.onStop();
                        break;
                }
                if (!isBound) {
                    if (activity.equals("Walking")) {
                        Intent stepServiceIntent = new Intent(this, StepCounterService.class);
                        bindService(stepServiceIntent, serviceConnection, BIND_AUTO_CREATE);
                        remoteViews.setTextViewText(R.id.tv_foreground_specialFeature, "Steps");
                    }
                    remoteViews.setTextViewText(R.id.tv_foreground_activity, activity);
                }
            }
        } else {
            Log.d("Chrono revicer on start comand", "intent action null non so dove arriva");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        handlerThread.quit();
        if (updateReceiverRegistered) {
            unregisterReceiver(updateReceiver);
            updateReceiverRegistered = false;
        }
        if (isBound) {
            unbindService(serviceConnection);
        }

        super.onDestroy();
    }

    private void onStart() {
        this.chronometerBase = SystemClock.elapsedRealtime() - pauseOffset;
    }

    private void onPause() {
        this.pauseOffset = SystemClock.elapsedRealtime() - this.chronometerBase;
    }

    private void onStop() {
        this.chronometerBase = SystemClock.elapsedRealtime() - pauseOffset;
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStepCountUpdated(int steps) {
        finalSteps += steps;
    }

    private Notification createNotification() {

        Intent openActivityIntent = new Intent(this, MainActivity.class);
        openActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openActivityIntent.setAction("UPDATE_SERVICE_FOREGROUND_STATE");
        openActivityIntent.putExtra("notification_open", true);
        openActivityIntent.putExtra("activity", activity);
        PendingIntent openPendingIntent = PendingIntent.getActivity(this, 0, openActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        return new NotificationCompat.Builder(this, "chrono_channel")
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle("Registrazione Partita")
                .setContentText("Click per registrala in app")
                .setContent(remoteViews)
                .setContentIntent(openPendingIntent)
                .build();
    }
}
