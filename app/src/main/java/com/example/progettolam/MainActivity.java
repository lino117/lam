package com.example.progettolam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.progettolam.fragment.HomeFragment;
import com.example.progettolam.fragment.HistoryFragment;
import com.example.progettolam.fragment.StatisticFragment;
import com.example.progettolam.recognitionTransition.UserActivityDetectionService;
import com.google.android.gms.location.ActivityTransitionRequest;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout llTime, llRecords, llStatistic;
    private ImageView ivTime, ivRecords, ivStatistic;
    private TextView tvTime, tvRecords, tvStatistic;
    private UserActivityDetectionService userActivityDetectionService;
    FragmentManager fragmentManager;


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
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // 第三个 arg 可以接受一个bundle可以用来传输数据， 然后由dynamicFragment.class. onCreate 接受并使用
        // Budle bundle = new budle();
        // budle.putString("param1" 跟class里面的变量一样的名字或者把变量改成private然后用name.class.ARG_PARAM1来决定，“text, message”)
        fragmentTransaction.replace(R.id.fcv_fragment, HomeFragment.class, null,"fragment_home")
                .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
                .setReorderingAllowed(true) // 用来辅助返回
                .commit();
        initView();
        initEvent();
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

    @Override
    protected void onStart() {
        Intent intent = getIntent();
        if (intent.getAction() != null){
            if (intent.getAction().equals("UPDATE_SERVICE_FOREGROUND_STATE")){
                Intent stopForegroundIntent = new Intent("com.example.chronometer.STOP_FOREGROUND_SERVICE");
                this.sendBroadcast(stopForegroundIntent);
                Bundle bundle = new Bundle();
                bundle.putBoolean(
                        "notification_open",
                        intent.getBooleanExtra("notification_open",false)
                );
                bundle.putString("activity", intent.getStringExtra("activity"));
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                // 第三个 arg 可以接受一个bundle可以用来传输数据， 然后由dynamicFragment.class. onCreate 接受并使用
                // Budle bundle = new budle();
                // budle.putString("param1" 跟class里面的变量一样的名字或者把变量改成private然后用name.class.ARG_PARAM1来决定，“text, message”)
                fragmentTransaction.replace(R.id.fcv_fragment, HomeFragment.class, bundle,"fragment_home")
                        .addToBackStack("nameFragment") // 用来加入到fragment的stack里面 通过返回来获取上一个fragment
                        .setReorderingAllowed(true) // 用来辅助返回
                        .commit();
            }
        }else {
            Log.d("Chrono activity on start","non e arrivato da notification");
            HomeFragment homeFragment = (HomeFragment) fragmentManager.findFragmentByTag("fragment_home");
            if (homeFragment!= null && homeFragment.isVisible()){
                Log.d("Chrono","Fragment visible in activity allora stop foreground service");
                Intent stopForegroundIntent = new Intent("com.example.chronometer.STOP_FOREGROUND_SERVICE");
                this.sendBroadcast(stopForegroundIntent);
            }
        }


        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        userActivityDetectionService.stopActivityUpdates();
    }


}