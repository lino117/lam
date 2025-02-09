package com.example.progettolam;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.progettolam.Worker.PeriodicNotificationWorker;
import com.example.progettolam.fragment.HomeFragment;
import com.example.progettolam.fragment.HistoryFragment;
import com.example.progettolam.fragment.StatisticFragment;
import com.example.progettolam.recognitionTransition.UserActivityDetectionService;
import com.example.progettolam.sharedPreferences.PrefsManager;
import com.google.android.gms.location.ActivityTransitionRequest;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout llTime, llRecords, llStatistic;
    private ImageView ivTime, ivRecords, ivStatistic;
    private TextView tvTime, tvRecords, tvStatistic;
    private UserActivityDetectionService userActivityDetectionService;
    FragmentManager fragmentManager;
    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fragmentManager = getSupportFragmentManager();
        initView();
        initEvent();

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            initPeriodicNotification();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_POST_NOTIFICATIONS);
        }
    }
    private void initView() {
        llTime = findViewById(R.id.ll_time);
        llRecords = findViewById(R.id.ll_records);
        llStatistic = findViewById(R.id.ll_statistic);

        ivTime = findViewById(R.id.iv_time);
        ivRecords = findViewById(R.id.iv_records);
        ivStatistic = findViewById(R.id.iv_statistic);

        tvTime = findViewById(R.id.tv_time);
        tvRecords = findViewById(R.id.tv_records);
        tvStatistic = findViewById(R.id.tv_statistic);
        PrefsManager.init(this);
        userActivityDetectionService = new UserActivityDetectionService(this);
    }
    private void initEvent() {
        setActiveBottomNavItem(R.id.ll_time);
        llTime.setOnClickListener(this);
        llRecords.setOnClickListener(this);
        llStatistic.setOnClickListener(this);

        ActivityTransitionRequest request = userActivityDetectionService.buildTransitionRequest();
        userActivityDetectionService.startActivityUpdates(request);
    }
    private void resetBottomNavItem() {
        ivTime.setSelected(false);
        tvTime.setTextColor(getColor(R.color.disable_color));
        ivRecords.setSelected(false);
        tvRecords.setTextColor(getColor(R.color.disable_color));
        ivStatistic.setSelected(false);
        tvStatistic.setTextColor(getColor(R.color.disable_color));
    }
    private void setActiveBottomNavItem(int id) {
        if (id == R.id.ll_time) {
            ivTime.setSelected(true);
            tvTime.setTextColor(getColor(R.color.active_color));
        } else if (id == R.id.ll_records) {
            ivRecords.setSelected(true);
            tvRecords.setTextColor(getColor(R.color.active_color));
        } else if (id == R.id.ll_statistic) {
            ivStatistic.setSelected(true);
            tvStatistic.setTextColor(getColor(R.color.active_color));
        }

    }

    public void onClick(View view) {

        int id = view.getId();
        resetBottomNavItem();

        if (id == R.id.ll_time) {
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // 第三个 arg 可以接受一个bundle可以用来传输数据， 然后由dynamicFragment.class. onCreate 接受并使用
            // Budle bundle = new budle();
            // budle.putString("param1" 跟class里面的变量一样的名字或者把变量改成private然后用name.class.ARG_PARAM1来决定，“text, message”)
            fragmentTransaction.replace(R.id.fcv_fragment, HomeFragment.class, null,"fragment_home")
                    .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
                    .setReorderingAllowed(true) // 用来辅助返回
                    .commit();
            setActiveBottomNavItem(id);
        }
        else if (id == R.id.ll_records) {
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.replace(R.id.fcv_fragment, HistoryFragment.class, null,"fragment_history")
                    .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
                    .setReorderingAllowed(true) // 用来辅助返回
                    .commit();
            setActiveBottomNavItem(id);
        }
        else if (id == R.id.ll_statistic) {
            fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fcv_fragment, StatisticFragment.class, null,"fragment_statistic")
                    .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
                    .setReorderingAllowed(true) // 用来辅助返回
                    .commit();
            setActiveBottomNavItem(id);
        }

    }
    private void initPeriodicNotification() {
        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(PeriodicNotificationWorker.class,
                        24, TimeUnit.HOURS)
                        .setInitialDelay(30, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).
                enqueueUniquePeriodicWork(
                        "notification_work",
                        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                        periodicWorkRequest);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Chrono activity on start","onStart in activity");
        switchChronometerService();
    }

    private void switchChronometerService() {
        Intent intent = getIntent();
        String action = intent.getAction();
        Log.d("Chrono switchChronometerService", "action ricevuto: "+action);
        // cercare se home_fragment esiste gia.
        HomeFragment existingFragment = (HomeFragment) fragmentManager.findFragmentByTag("fragment_home");

        if (action != null) {
            // caso trona in app da notification oppure alla creazione
            if ("UPDATE_SERVICE_FOREGROUND_STATE".equals(action)) {
                // se non esiste(alla creazione)
                // se non visibile(quando in bakcground, quando torna in foreground pero non e al fragment giusto)
                if (existingFragment == null ) {
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    Bundle bundle = new Bundle();
                    Log.d("Chrono activity on start", "apro home Fragment da notification");
                    bundle.putBoolean("notification_open", true);
                    bundle.putString("activity", intent.getStringExtra("activity"));
                    fragmentTransaction.replace(R.id.fcv_fragment, HomeFragment.class, bundle, "fragment_home")
                            .setReorderingAllowed(true) // 用来辅助返回
                            .commit();
                }
            }else if(action.equals("android.intent.action.MAIN")){
                if (existingFragment==null){
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.fcv_fragment, HomeFragment.class, null, "fragment_home")
                            .setReorderingAllowed(true) // 用来辅助返回
                            .commit();
                }

            }
        }
    }
    public void stopPeriodicNotification() {

        WorkManager wk = WorkManager.getInstance(this);
        try {
            List<WorkInfo> workInfos = wk.getWorkInfosForUniqueWork("notification_work").get();
            if (workInfos != null && !workInfos.isEmpty()) {
                WorkInfo.State state = workInfos.get(0).getState();
                if (state == WorkInfo.State.ENQUEUED || state == WorkInfo.State.RUNNING) {
                    wk.cancelUniqueWork("notification_work");
                }
            }
        } catch(ExecutionException | InterruptedException e){
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        userActivityDetectionService.stopActivityUpdates();
        stopPeriodicNotification();
    }


}