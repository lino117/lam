package com.example.progettolam.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
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

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.progettolam.R;
import com.example.progettolam.database.activityRecordDbHelper;
import com.example.progettolam.struct.Record;

import java.text.SimpleDateFormat;
import java.util.Date;

public class homeFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "DynamicFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;

    private Button btnStart, btnStop;
    private Chronometer chronometer;
    private Spinner activityList;
    private TextView tvActiviting,tvStep;
    private String selectedActivity;
    private long pauseOffset;
    private activityRecordDbHelper dbHelper;

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private long stepsAtStart;
    private long numberSteps;
    private long finalNumSteps;

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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                == PackageManager.PERMISSION_GRANTED) {
            // Il permesso è stato concesso, procedi con il sensore
            initializeSensorManager();
        } else {
            // Richiedi il permesso
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        }

    }
    private void initViews(View view) {
        dbHelper = new activityRecordDbHelper(view.getContext());
        btnStart = view.findViewById(R.id.startBtn);
        btnStop = view.findViewById(R.id.stopBtn);
        chronometer = view.findViewById(R.id.chrmter);
        activityList = view.findViewById(R.id.list_activity);
        tvActiviting = view.findViewById(R.id.tv_activiting);
        tvStep = view.findViewById(R.id.stepNum);
        pauseOffset = 0;
    }

    private void initClickListeners() {
        btnStart.setOnClickListener(view -> handleStartButtonClick());
        btnStop.setOnClickListener(view -> handleStopButtonClick());
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
            // registra passo fatto
            finalNumSteps += numberSteps;

            long newRowID = dbHelper.insertData(record);
            // inizializza tutti i valori dopo aver inserito i dati
            stepsAtStart = numberSteps = finalNumSteps =0;

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

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");

        Record record = new Record();
        record.setNameActivity(selectedActivity);
        record.setDuration((int) (duration / 1000));
        if (selectedActivity.equals("Walking")){
            record.setStep((int) finalNumSteps);
        }else {
            record.setStep(null);
        }
        record.setStartTime(timeFormat.format(new Date(startTime)));
        record.setEndTime(timeFormat.format(new Date(currentTime)));
        record.setStartDay(dayFormat.format(new Date(startTime)));
        record.setEndDay(dayFormat.format(new Date(currentTime)));

        return record;
    }

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
    // come avere i passi registrati. quando ricevere passi registrati.
    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("tag","dentro on change di register qua clash");
        if (event != null && event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            if (chronometer.isActivated()) {
                // stepsAtStart = 0 quindi assegno quello corrente, ovvero event.valus[0]
                if (stepsAtStart == 0) {
                    stepsAtStart = (long) event.values[0];

                }

                // ogni volta che cambia, registra quanti passi son stati fatti
                numberSteps = (long) event.values[0] - stepsAtStart;
                tvStep.setText(String.valueOf(numberSteps));

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Sensor accuracy changed: " + accuracy);
    }
}
