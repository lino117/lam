package com.example.progettolam.fragment;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.progettolam.R;
import com.example.progettolam.Worker.PeriodicNotificationWorker;
import com.example.progettolam.database.activityRecordDbHelper;
import com.example.progettolam.struct.Record;
//import com.example.progettolam.transition.HWDetectionService;
import com.example.progettolam.transition.UserActivityDetectionReceiver;
import com.example.progettolam.transition.UserActivityDetectionService;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class homeFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "DynamicFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;
    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 1;

    private Button btnStart, btnStop;
    private Chronometer chronometer;
    private Spinner activityList;
    private TextView tvActiviting,tvStep;
    private String selectedActivity;
    private long pauseOffset;
    private activityRecordDbHelper dbHelper;
    private MaterialButton btnNotification, btnTransition,btnSendTestIntent;
    private AppCompatTextView txNotification, txTransition;

    private SensorManager sensorManager;
    UserActivityDetectionReceiver userActivityDetectionReceiver;
//    HWDetectionService hwDetectionService;
    private Sensor stepSensor;
    private long stepsAtStart;
    private long numberSteps;
    private long finalNumSteps;
    private UserActivityDetectionService userActivityDetectionService;
    private SQLiteDatabase db;

    public homeFragment() {
        // Required empty public constructor
    }

    public static homeFragment newInstance(String param1, String param2) {
        homeFragment fragment = new homeFragment();
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
            getArguments().getString(ARG_PARAM1);
            getArguments().getString(ARG_PARAM2);
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

        initViews(view);
        initClickListeners();
        checkPermissions();

    }

    private void initActivityDetection() {
        String TRANSITIONS_RECEIVER_ACTION = "com.example.progettolam.transition.TRANSITIONS_RECEIVER_ACTION";
//        String ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION";

        userActivityDetectionService = new UserActivityDetectionService(requireContext());
        Intent intent = new Intent(TRANSITIONS_RECEIVER_ACTION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        userActivityDetectionService.startActivityUpdates(userActivityDetectionService.buildTransitionRequest(),pendingIntent);

//        hwDetectionService = new HWDetectionService(requireContext());
//        Intent intent = new Intent(ACTION_PROCESS_LOCATION);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
//        hwDetectionService.startActivityRecognition(hwDetectionService.buildTransitionRequest(),pendingIntent);

//        userActivityDetectionReceiver = new UserActivityDetectionReceiver();
//        registerReceiver(requireContext(), userActivityDetectionReceiver, new IntentFilter(TRANSITIONS_RECEIVER_ACTION), ContextCompat.RECEIVER_NOT_EXPORTED);
//        registerReceiver(requireContext(), userActivityDetectionReceiver, new IntentFilter(ACTION_PROCESS_LOCATION), ContextCompat.RECEIVER_NOT_EXPORTED);


        txTransition.setText("Transition On");
        btnTransition.setText("Click to switch off");
    }

    private void initViews(View view) {
        dbHelper = new activityRecordDbHelper(view.getContext());
        btnStart = view.findViewById(R.id.startBtn);
        btnStop = view.findViewById(R.id.stopBtn);
        chronometer = view.findViewById(R.id.chrmter);
        activityList = view.findViewById(R.id.list_activity);
        tvActiviting = view.findViewById(R.id.tv_activiting);
        tvStep = view.findViewById(R.id.stepNum);

        btnNotification = view.findViewById(R.id.periodicControll);
        txNotification = view.findViewById(R.id.tx_notification);

        btnTransition = view.findViewById(R.id.trasitionControll);
        txTransition = view.findViewById(R.id.tx_transition);
        btnSendTestIntent = view.findViewById(R.id.btn_send_testIntent);
        pauseOffset = 0;
    }
    private void initClickListeners() {
        btnStart.setOnClickListener(view -> handleStartButtonClick());
        btnStop.setOnClickListener(view -> handleStopButtonClick());
        btnNotification.setOnClickListener(view -> switchNotification());
        btnTransition.setOnClickListener(view -> switchTransition());
        btnSendTestIntent.setOnClickListener(view -> sendTestIntent(view));
    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
            // Il permesso è stato concesso, procedi con il sensore
            initializeSensorManager();
            initActivityDetection();
        } else {
            // Richiedi il permesso
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

//            initPeriodicNotification();
            btnNotification.setText("Click to switch on");
            txNotification.setText("Notification is Off");
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
    private void initializeSensorManager() {

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor == null) {
                Toast.makeText(requireContext(), "Step counter sensor is not available on this device", Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(requireContext(), "Avengeeeer", Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "SensorManager initialization failed.");
        }
    }

    private void handleStartButtonClick() {
        selectedActivity = activityList.getSelectedItem().toString();
        if (!chronometer.isActivated()) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            chronometer.setActivated(true);
            btnStart.setText(R.string.cmeter_pause);
            tvActiviting.setText(selectedActivity);

            if (stepSensor != null && selectedActivity.equals( "Walking")) {
                sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }


        } else {
            chronometer.stop();
            chronometer.setActivated(false);
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            btnStart.setText(R.string.cmeter_continue);

            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
            Log.d("ActivityRecognition pass","dentro pause final->"+finalNumSteps+"step-> "+numberSteps);

            // registra passo fatto
            finalNumSteps += numberSteps;
            // inizia valori
            stepsAtStart = numberSteps = 0;
        }
    }

    private void handleStopButtonClick() {
        if (chronometer.isActivated() || pauseOffset != 0) {
            selectedActivity = activityList.getSelectedItem().toString();
            chronometer.stop();
            Record record = createRecord(System.currentTimeMillis(), SystemClock.elapsedRealtime());
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.setActivated(false);
            pauseOffset = 0;
            btnStart.setText(R.string.cmeter_start);
            tvActiviting.setText("");

            if (sensorManager != null) {
                sensorManager.unregisterListener(this);
            }
//            db = dbHelper.getWritableDatabase();
            long newRowID = dbHelper.insertData(record);

            if (newRowID != -1) {
                Toast.makeText(requireContext(), record.toString(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "Insert error", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Record createRecord(long currentTime, long relativeTime) {
        long duration = correctBias(relativeTime);
        long startTime = currentTime - duration;
        finalNumSteps+=numberSteps;
//        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
//        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
        Record record = new Record();
        record.setNameActivity(selectedActivity);
        record.setDuration((int) (duration / 1000));
        if (selectedActivity.equals("Walking")){
            record.setStep((int) finalNumSteps);
        }else {
            record.setStep(null);
        }

        record.setStart_time(startTime % 86400000);
        record.setEnd_time(currentTime % 86400000 );
        record.setStart_day(setDay(startTime) );
        record.setEnd_day(setDay(currentTime));

        stepsAtStart = numberSteps = finalNumSteps = 0;
        return record;
    }
    private Long setDay(Long  time) {

        // Crea un oggetto Calendar e imposta il tempo
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        // Azzerare le ore, i minuti, i secondi e i millisecondi
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Log.d("day",(int)(calendar.getTimeInMillis()) +"   "+calendar.getTimeInMillis());
        return calendar.getTimeInMillis();
    };
    private long correctBias(long relativeTime) {
        final String text = chronometer.getText().toString();
        final int bias = Character.getNumericValue(text.charAt(4));
        long noCorrectDuration = relativeTime - chronometer.getBase();

        final int lastDigit = (int) (noCorrectDuration / 1000 % 60 % 10);
        if (lastDigit > bias || (bias == 9 && lastDigit == 0)) {
            return noCorrectDuration - 1000;
        }
        return noCorrectDuration;
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (stepSensor != null) {
//            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event != null && event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            if (chronometer.isActivated()) {
                // stepsAtStart = 0 quindi assegno quello corrente, ovvero event.valus[0]
                if (stepsAtStart == 0) {
                    stepsAtStart = (long) event.values[0];
                }
                // ogni volta che cambia, registra quanti passi son stati fatti
                numberSteps = (long) event.values[0] - stepsAtStart;

                long displaySteps = finalNumSteps + numberSteps;
                tvStep.setText(String.valueOf(displaySteps));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Sensor accuracy changed: " + accuracy);
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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        switchNotification();
//        switchTransition();
//        requireContext().unregisterReceiver(userActivityDetectionReceiver);
    }

    public void sendTestIntent(View view) {
        Intent testIntent = new Intent(requireContext(), UserActivityDetectionReceiver.class);
        testIntent.setAction("com.google.android.gms.location.ACTIVITY_TRANSITION"); // Assicurati che l'azione corrisponda
        requireContext().sendBroadcast(testIntent);
    }
}
