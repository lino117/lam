package com.example.progettolam.fragment;

import static androidx.core.content.ContextCompat.registerReceiver;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.progettolam.R;
import com.example.progettolam.chronometer.ChronometerService;
import com.example.progettolam.database.ActivityRecordDbHelper;
import com.example.progettolam.sharedPreferences.PrefsManager;
import com.example.progettolam.struct.Record;
import com.example.progettolam.timeConverter.TimeConverter;

public class HomeFragment extends Fragment {
    private static final String TAG = "DynamicFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 1;

    private Button btnStart, btnStop;
    private Spinner activityList;
    private TextView tvActiviting, tvStep;
    private String selectedActivity;
    private ActivityRecordDbHelper dbHelper;
    private AppCompatTextView tvDuration;
    private long finalSteps, duration;

    private Context context;
    private boolean isActivatedFragmentChronometer, mReceiverRegistered, notification_open = false;
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

    }
    private final BroadcastReceiver chronometerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long duration = intent.getLongExtra("duration", 0);
            int steps = intent.getIntExtra("steps", 0);
            String activity = intent.getStringExtra("activity");
            setVariables(duration, steps, activity);
            if (isVisible()) {
                tvDuration.setText(String.format("%02d:%02d:%02d", duration / 3600, duration / 60, duration % 60));
                tvActiviting.setText(activity);
                if (selectedActivity!=null && selectedActivity.equals("Walking")){
                    tvStep.setText(steps + " steps");
                }
            }
        }
    };

    private void setVariables(long duration, int steps, String activity) {
        this.duration = duration;
        this.finalSteps = steps;
        this.selectedActivity = activity;
    }

    public void switchToAppChronometer() {
        registerReceiver(context, chronometerReceiver, new IntentFilter("com.example.chronometer.UPDATE"), ContextCompat.RECEIVER_NOT_EXPORTED);
        isActivatedFragmentChronometer = true;
        mReceiverRegistered = true;

        btnStart.setText(R.string.cmeter_pause);

        notification_open = false;
        Log.d("Chrono fragment", "ChronoMeter in app is on "+selectedActivity);
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

            if (flag.equals("onStart") ){
                PrefsManager.saveString("activity", selectedActivity);
            }

            Log.d("Chrono",selectedActivity+"all inizio di start");
            bundle.putString("serviceMode", "inApp");
            intent.putExtras(bundle);
            intent.setAction("FRAGMENT_REGISTRATION");
            context.startService(intent);

        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
        }

    }

    private void initViews(View view) {
        dbHelper = new ActivityRecordDbHelper(view.getContext());
        btnStart = view.findViewById(R.id.startBtn);
        btnStop = view.findViewById(R.id.stopBtn);

        activityList = view.findViewById(R.id.list_activity);
        tvActiviting = view.findViewById(R.id.tv_activiting);
        tvStep = view.findViewById(R.id.stepNum);


        tvDuration = view.findViewById(R.id.tv_time);
        timeConvertitor = new TimeConverter();
    }

    private void initClickListeners() {

        btnStart.setOnClickListener(view -> handleStartButtonClick());
        btnStop.setOnClickListener(view -> handleStopButtonClick());

    }

    private void handleStartButtonClick() {
        selectedActivity = activityList.getSelectedItem().toString();
        if (!isActivatedFragmentChronometer) {
            isActivatedFragmentChronometer = true;
            mReceiverRegistered = true;
            registerReceiver(context, chronometerReceiver, new IntentFilter("com.example.chronometer.UPDATE"), ContextCompat.RECEIVER_NOT_EXPORTED);
            chronometerManager("onStart");
            btnStart.setText(R.string.cmeter_pause);
            tvActiviting.setText(selectedActivity);
        } else {
            isActivatedFragmentChronometer = false;
            mReceiverRegistered = false;
            context.unregisterReceiver(chronometerReceiver);
            chronometerManager("onPause");
            btnStart.setText(R.string.cmeter_continue);
        }
    }

    private void handleStopButtonClick() {

        isActivatedFragmentChronometer = false;
        if (mReceiverRegistered) {
            context.unregisterReceiver(chronometerReceiver);
            mReceiverRegistered = false;
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

    private void resetDisplay() {
        tvDuration.setText(R.string.cMeterStartValue);
        tvActiviting.setText("");
        tvStep.setText("");
    }

    private Record createRecord(long currentTime) {
        // trasforma duration da secondi in ms
        long startTime = currentTime - this.duration * 1000;
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

    @Override
    public void onResume() {
        super.onResume();
//        if (stepSensor != null) {
//            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
//        }
    }

    public boolean updateData() {

        return mReceiverRegistered;
    }

    private boolean isChronoServiceActived() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            Log.d("Chrono", service.service.getClassName());
            if (service.service.getClassName().equals("com.example.progettolam.chronometer.ChronometerService")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Chrono fragment", "onStart in fragment");
        Log.d("Chrono fragment", "on Start: mRegister+serviceActived check :"+(!mReceiverRegistered && isChronoServiceActived()));
        if (!mReceiverRegistered && isChronoServiceActived()) {
            if (selectedActivity==null){
                selectedActivity=PrefsManager.getString("activity");
            }
            Log.d("Chrono fragment", "on Start: no register no activeChrono activity is "+selectedActivity);
            String stopForegroundAction = "com.example.chronometer.STOP_FOREGROUND_SERVICE";
            Intent stopForegroundIntent = new Intent(stopForegroundAction);
            stopForegroundIntent.putExtra("activity", selectedActivity);
            context.sendBroadcast(stopForegroundIntent);
            switchToAppChronometer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mReceiverRegistered && isActivatedFragmentChronometer) {
            Log.d("Chrono fragment", "onStop in fragment " + mReceiverRegistered + " " + updateData());
            String startForegroundAction = "com.example.chronometer.START_FOREGROUND_SERVICE";
            Intent startForegroundIntent = new Intent(startForegroundAction);
            context.sendBroadcast(startForegroundIntent);
            notification_open = false;
            context.unregisterReceiver(chronometerReceiver);
            isActivatedFragmentChronometer = false;
            mReceiverRegistered = false;
            resetDisplay();
            activityList.setSelection(0);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiverRegistered) {
            context.unregisterReceiver(chronometerReceiver);
        }
    }
}
