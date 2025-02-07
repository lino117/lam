package com.example.progettolam.fragment;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.progettolam.R;
import com.example.progettolam.Worker.PeriodicNotificationWorker;
import com.example.progettolam.autoRegistration.AutoRegistrationReceiver;
import com.example.progettolam.chronometer.ChronometerService;
import com.example.progettolam.database.ActivityRecordDbHelper;
import com.example.progettolam.struct.Record;
import com.example.progettolam.timeConvertitor.TimeConverter;
//import com.example.progettolam.transition.HWDetectionService;
import com.example.progettolam.recognitionTransition.UserActivityDetectionService;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private static final String TAG = "DynamicFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;
    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 1;
    private Button btnStart, btnStop;
    private Spinner activityList;
    private TextView tvActiviting, tvStep;
    private String selectedActivity;
    private ActivityRecordDbHelper dbHelper;
    private MaterialButton btnNotification, btnTransition, btnSendTestIntent;
    private AppCompatTextView txNotification, txTransition,tvDuration;
    private long numberSteps, pauseOffset, finalSteps, duration;
    private UserActivityDetectionService userActivityDetectionService;
    String TRANSITIONS_RECEIVER_ACTION = "com.example.progettolam.transition.TRANSITIONS_RECEIVER_ACTION";
    private Context context;
    private boolean isActivatedChronometer,mReceiverRegistered,notification_open = false;
    private MaterialButton btnSentIntentChorno;
    private TimeConverter timeConvertitor;


    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            notification_open = getArguments().getBoolean("notification_open");
            selectedActivity = getArguments().getString("activity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = requireContext();
        initViews(view);
        initClickListeners();

        if (notification_open && !mReceiverRegistered){
            Log.d("Chrono fragment","notificaiont_id "+notification_open);
            switchToAppChronometer();
        }
//        initChronometer();
//        initServices();
    }

    public void switchToAppChronometer() {
        registerReceiver(context, chronometerReceiver, new IntentFilter("com.example.chronometer.UPDATE"), ContextCompat.RECEIVER_NOT_EXPORTED);
        isActivatedChronometer = true;
        mReceiverRegistered=true;
        if (isVisible()){
            btnStart.setText(R.string.cmeter_pause);
            tvActiviting.setText(selectedActivity);
        }
        notification_open = false;

        ArrayAdapter<String> adapter = (ArrayAdapter<String>) activityList.getAdapter();
        int position = adapter.getPosition(selectedActivity);
        activityList.setSelection(position);
    }

    private void chronometerManager(String flag) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(context, ChronometerService.class);
            Bundle bundle = new Bundle();
            bundle.putString("chronometerState", flag);
            bundle.putString("activity", selectedActivity);
            bundle.putString("serviceMode","inApp");
            intent.putExtras(bundle);
            intent.setAction("FRAGMENT_REGISTRATION");
            context.startService(intent);

//            initActivityDetection();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        }

    }

    private final BroadcastReceiver chronometerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long duration = intent.getLongExtra("duration", 0);
            int steps = intent.getIntExtra("steps", 0);
            setDisplay(duration,steps);
            if (isVisible()){
                tvDuration.setText(String.format("%02d:%02d:%02d", duration / 3600, duration / 60, duration % 60));
                tvStep.setText(steps + " steps");
            }

        }
    };
    private void setDisplay(long duration, int steps) {
        Log.d("Step fragment setDisplay",finalSteps+"passi "+this.duration+"sec");
        this.duration = duration;
        this.finalSteps = steps;
    }

    private void initActivityDetection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "detection_channel";
//            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("detection_channel", name, importance);
//            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(channel);
        }
        userActivityDetectionService = new UserActivityDetectionService(requireContext());
//        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
//        @SuppressLint("MutableImplicitPendingIntent") PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
        userActivityDetectionService.startActivityUpdates(userActivityDetectionService.buildTransitionRequest());
        txTransition.setText("Transition On");
        btnTransition.setText("Click to switch off");
    }

    private void initViews(View view) {
        dbHelper = new ActivityRecordDbHelper(view.getContext());
        btnStart = view.findViewById(R.id.startBtn);
        btnStop = view.findViewById(R.id.stopBtn);

        activityList = view.findViewById(R.id.list_activity);
        tvActiviting = view.findViewById(R.id.tv_activiting);
        tvStep = view.findViewById(R.id.stepNum);

        btnNotification = view.findViewById(R.id.periodicControll);
        txNotification = view.findViewById(R.id.tx_notification);

        btnTransition = view.findViewById(R.id.trasitionControll);
        txTransition = view.findViewById(R.id.tx_transition);
        btnSendTestIntent = view.findViewById(R.id.btn_send_testIntent);
        btnSentIntentChorno = view.findViewById(R.id.btn_send_testIntent_for_Chrono);
        pauseOffset = 0;
        tvDuration=view.findViewById(R.id.tv_time);
        timeConvertitor = new TimeConverter();
    }

    private void initClickListeners() {

        btnStart.setOnClickListener(view -> handleStartButtonClick());
        btnStop.setOnClickListener(view -> handleStopButtonClick());
        btnNotification.setOnClickListener(view -> switchNotification());
        btnTransition.setOnClickListener(view -> switchTransition());
        btnSendTestIntent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent stopAutoRegistrationIntent = new Intent(context, AutoRegistrationReceiver.class);
                stopAutoRegistrationIntent.setAction("com.example.chronometer.AUTO_REGISTRATION_END");
                stopAutoRegistrationIntent.putExtra("duration", 10);
                stopAutoRegistrationIntent.putExtra("activity","Unknown");
                stopAutoRegistrationIntent.putExtra("endTimeActivity",System.currentTimeMillis());
                Log.d("Chrono", "invio stop intent"+stopAutoRegistrationIntent);
                context.sendBroadcast(stopAutoRegistrationIntent);
            }
        });

        btnSentIntentChorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent acceptIntent = new Intent(context, ChronometerService.class);
//                acceptIntent.setAction("ACCEPT_REGISTRATION");
//                acceptIntent.putExtra("activity","Walking");
//                acceptIntent.putExtra("notification_id", 117);
//                acceptIntent.putExtra("chronometerState","onStart");
////                PendingIntent acceptPendingIntent = PendingIntent.getService(context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
//                context.startService(acceptIntent);
                Log.d("Chrono","invio start registration intent");
                Intent intentStartAutoRegistration = new Intent(context, ChronometerService.class);
                intentStartAutoRegistration.setAction("AUTO_REGISTRATION");
                intentStartAutoRegistration.putExtra("activity","Unknown");
                intentStartAutoRegistration.putExtra("chronometerState","onStart");
                intentStartAutoRegistration.putExtra("endTime","1738948800000");
                intentStartAutoRegistration.putExtra("endTimeActivity",timeConvertitor.toLocalTimeZone(System.currentTimeMillis()));
                context.startService(intentStartAutoRegistration);
            }

        });
    }

    private void initServices() {


        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

            initPeriodicNotification();
//            btnNotification.setText("Click to switch on");
//            txNotification.setText("Notification is Off");
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_POST_NOTIFICATIONS);
        }
    }

    private void initPeriodicNotification() {
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(PeriodicNotificationWorker.class,
                        15, TimeUnit.MINUTES)
                        .setInitialDelay(10, TimeUnit.SECONDS)
                        .build();

        WorkManager.getInstance(requireContext()).
                enqueueUniquePeriodicWork(
                        "notification_work",
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                        periodicWorkRequest);

        txNotification.setText("Notification is On");
        btnNotification.setText("Click to switch off");
    }

    // TODO: da cancellare
    // fino qua

    private void handleStartButtonClick() {
        selectedActivity = activityList.getSelectedItem().toString();
        if (!isActivatedChronometer) {
            isActivatedChronometer = true;
            mReceiverRegistered=true;
            registerReceiver(context, chronometerReceiver, new IntentFilter("com.example.chronometer.UPDATE"), ContextCompat.RECEIVER_NOT_EXPORTED);
            chronometerManager("onStart");
            btnStart.setText(R.string.cmeter_pause);
            tvActiviting.setText(selectedActivity);
        } else {
            isActivatedChronometer=false;
            mReceiverRegistered=false;
            context.unregisterReceiver(chronometerReceiver);
            chronometerManager("onPause");
            btnStart.setText(R.string.cmeter_continue);
        }
    }
    private void handleStopButtonClick() {

        isActivatedChronometer=false;
        if (mReceiverRegistered){
            context.unregisterReceiver(chronometerReceiver);
            mReceiverRegistered=false;
        }

        chronometerManager("onStop");
        selectedActivity = activityList.getSelectedItem().toString();
        Record record = createRecord(timeConvertitor.toLocalTimeZone(System.currentTimeMillis()));
        btnStart.setText(R.string.cmeter_start);
        resetDisplay();

        long newRowID = dbHelper.insertData(record);
        if (newRowID != -1) {
            Toast.makeText(requireContext(), record.toString(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), "Insert error", Toast.LENGTH_LONG).show();
        }
    }
    private void resetDisplay(){
        tvDuration.setText(R.string.cMeterStartValue);
        tvActiviting.setText("");
        tvStep.setText("");
    }
    private Record createRecord(long currentTime) {
        // trasforma duration da secondi in ms
        long startTime = currentTime - this.duration*1000;
//        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
        Record record = new Record();
        record.setNameActivity(selectedActivity);
        record.setDuration((int) (this.duration));
        if (selectedActivity.equals("Walking")) {
            record.setStep((int) finalSteps);
        } else {
            record.setStep(null);
        }
        record.setStart_time(startTime % 86400000);
        record.setEnd_time(currentTime % 86400000);
        record.setStart_day(timeConvertitor.setDayTimeToZero(startTime));
        record.setEnd_day(timeConvertitor.setDayTimeToZero(currentTime));
        return record;
    }
    private void switchTransition() {

        userActivityDetectionService.stopActivityUpdates();
        txTransition.setText("Transition is Off");
        btnTransition.setText("restart to switch On");

    }

    public void switchNotification() {

        WorkManager wk = WorkManager.getInstance(requireContext());
        try {
            List<WorkInfo> workInfos = wk.getWorkInfosForUniqueWork("notification_work").get();
            if (workInfos != null && !workInfos.isEmpty()) {
                WorkInfo.State state = workInfos.get(0).getState();
                if (state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.RUNNING) {
                    wk.cancelUniqueWork("notification_work");
                    btnNotification.setText("Click to switch on");
                    txNotification.setText("Notification is Off");
                } else {
                    initPeriodicNotification();
                }
            }
//            else {
//                initPeriodicNotification();
//            }
            } catch(ExecutionException | InterruptedException e){
                throw new RuntimeException(e);
            }

    }

    public void sendTestIntent(View view) {
        Log.d("Chrono", "sendStartAutoRegistrationIntent");
        Intent stopAutoRegistrationIntent = new Intent(context, AutoRegistrationReceiver.class);
        stopAutoRegistrationIntent.setAction("com.example.chronometer.AUTO_REGISTRATION_END");
        stopAutoRegistrationIntent.putExtra("duration", duration);
        stopAutoRegistrationIntent.putExtra("activity","Unknown");
        context.sendBroadcast(stopAutoRegistrationIntent);
//        Intent testIntent = new Intent(requireContext(), UserActivityDetectionReceiver.class);
//        testIntent.setAction(TRANSITIONS_RECEIVER_ACTION);
////        requireContext().sendBroadcast(testIntent);
//
////        Intent intent = new Intent(R)
//        List<ActivityTransitionEvent> events = new ArrayList<>();
//
//        // You can set desired events with their corresponding state
//
//        ActivityTransitionEvent transitionEvent = new ActivityTransitionEvent(
//                DetectedActivity.IN_VEHICLE,
//                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
//                SystemClock.elapsedRealtimeNanos());
//        events.add(transitionEvent);
//
//        ActivityTransitionResult result = new ActivityTransitionResult(events);
//
//        SafeParcelableSerializer.serializeToIntentExtra(result, testIntent, "com.google.android.location.internal.EXTRA_ACTIVITY_TRANSITION_RESULT");
//        requireContext().sendBroadcast(testIntent);
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (stepSensor != null) {
//            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
//        }
    }

    public boolean ismReceiverRegistered() {
        return mReceiverRegistered;
    }
    private boolean isChronoServiceActived(){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.d("Chrono",service.service.getClassName());
            if (service.service.getClassName().equals("com.example.progettolam.chronometer.ChronometerService")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        // interroga lo stato di foreground service
        // uso lo stesso di quello in activity
        // magari si puo provare di riusare parte del codice
        if (!mReceiverRegistered && isChronoServiceActived()){
            Log.d("Chrono fragment","onStart in fragment");
            String stopForegroundAction = "com.example.chronometer.STOP_FOREGROUND_SERVICE";
            Intent stopForegroundIntent = new Intent(stopForegroundAction);
//            stopForegroundIntent.setAction(stopForegroundAction);
            context.sendBroadcast(stopForegroundIntent);
            Log.d("Chrono fragment","invio stop foreground intent");
            switchToAppChronometer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // riprende lo stato di foreground service
        // porta registrazion in foreground service solo se
        // - in app chronometer attivo e servizio e in attivo
        if (mReceiverRegistered && isChronoServiceActived()){
            Log.d("Chrono fragment","onStop in fragment "+mReceiverRegistered+" "+ismReceiverRegistered());
            String startForegroundAction = "com.example.chronometer.START_FOREGROUND_SERVICE";
            Intent startForegroundIntent = new Intent(startForegroundAction);
            context.sendBroadcast(startForegroundIntent);
            notification_open = false;
            context.unregisterReceiver(chronometerReceiver);
            mReceiverRegistered=false;
            resetDisplay();
            activityList.setSelection(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        switchNotification();
//        userActivityDetectionService.stopActivityUpdates();

//        WorkManager wk = WorkManager.getInstance(requireContext());
//
//        List<WorkInfo> workInfos = null;
//        try {
//            workInfos = wk.getWorkInfosForUniqueWork("notification_work").get();
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        if (workInfos != null && !workInfos.isEmpty()) {
//            WorkInfo.State state = workInfos.get(0).getState();
//            if (state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.RUNNING) {
//                wk.cancelUniqueWork("notification_work");
//            }
//        }
//        switchTransition();
//        requireContext().unregisterReceiver(userActivityDetectionReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiverRegistered){
            context.unregisterReceiver(chronometerReceiver);
        }
    }
}
