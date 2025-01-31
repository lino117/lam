package com.example.progettolam.fragment;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.progettolam.R;
import com.example.progettolam.database.activityRecordDbHelper;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class statisticFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String CHANNEL_ID = "periodic_messsage";
    private static final int PERMISSION_REQUEST_POST_NOTIFICATIONS = 1;
    private BarChart barChart;
    private PieChart pieChart;
    activityRecordDbHelper dpHelper;
    SQLiteDatabase db;
    NotificationManagerCompat nm;
    private String mParam1;
    private String mParam2;

    public statisticFragment() {
        // Required empty public constructor
    }   


    public static statisticFragment newInstance(String param1, String param2) {
        statisticFragment fragment = new statisticFragment();
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
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        dpHelper = new activityRecordDbHelper(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistic, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = view.findViewById(R.id.bar_chart);
        pieChart = view.findViewById(R.id.pie_chart);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            initNotification();
        } else {
            // Richiedi il permesso
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_POST_NOTIFICATIONS);
        }

        dpHelper = new activityRecordDbHelper(requireContext());

        db = dpHelper.getReadableDatabase();

        initPieChart(dpHelper.getMonthlyActivity(db));

        initBarChart(dpHelper.getWeeklySteps(db));
    }

    private void initNotification() {
        nm = NotificationManagerCompat.from(requireContext());
        createNotificationChannel();
        periodicNotification();
    }
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void periodicNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle("ciaos")
                .setContentText("textContent")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    PERMISSION_REQUEST_POST_NOTIFICATIONS);
            return;
        }
        nm.notify(666,builder.build());
    }

    private void initBarChart(Cursor cursor) {
        final String title = "Step Situation";
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Integer> valori = new ArrayList<>();
        final ArrayList<Long> giorniMsec = fillWeekDates();
        int counter = 0;
        while (cursor.moveToNext()) {
            long day = cursor.getLong(cursor.getColumnIndexOrThrow("Start_Day"));
            while (counter < giorniMsec.size() && giorniMsec.get(counter) < day) {
                valori.add(0);
                counter++;
            }
            int steps = cursor.getInt(cursor.getColumnIndexOrThrow("total_pass"));
            valori.add(steps);
        }
        cursor.close();
        while (counter<giorniMsec.size()){
            valori.add(0);
            counter++;
        }
        ArrayList<String> giorni = convertLongToString(giorniMsec);

//        Log.d("barChart", String.valueOf(valori.size()) + " valore counter"+counter);
//        Log.d("barChart", valori.toString());
//        Log.d("barChart",giorniMsec.toString());

        for (int i = 0; i < giorniMsec.size(); i++) {
            barEntries.add(new BarEntry(i, valori.get(i)));
        }
        XAxis xAixs = barChart.getXAxis();
        xAixs.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return giorni.get((int) value);
            }
        });
        xAixs.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAixs.setGranularity(1f);
        BarDataSet barDataSet = new BarDataSet(barEntries, title);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();


    }

    private ArrayList<String> convertLongToString(ArrayList<Long> giorniMsec) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
        ArrayList<String> giorni = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            giorni.add(dateFormat.format(new Date(giorniMsec.get(i))));
        }
        return giorni;
    }

    private void initPieChart(Cursor cursor) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<String> etichette = new ArrayList<>();
        ArrayList<Integer> valori = new ArrayList<>();
        while (cursor.moveToNext()) {
            String nomeAttivita = cursor.getString(cursor.getColumnIndexOrThrow("Activity_Name"));
            int totaleTempoSpeso = cursor.getInt(cursor.getColumnIndexOrThrow("totale_tempo_speso"));
            etichette.add(nomeAttivita);
            valori.add(totaleTempoSpeso);
        }

        cursor.close();

        for (int i = 0; i < etichette.size(); i++) {
            // Crea un PieEntry usando il valore e l'etichetta corrispondente
            pieEntries.add(new PieEntry(valori.get(i), etichette.get(i)));
        }

        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(requireContext().getColor(R.color.purple));
        colors.add(requireContext().getColor(R.color.light_blue));
        colors.add(requireContext().getColor(R.color.grey));


        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Monthly Activity");

        pieDataSet.setDrawIcons(false);
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);

        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dpHelper.close();
        db.close();
    }
    public ArrayList<Long> fillWeekDates() {
        ArrayList<Long> giorni = new ArrayList<>();
        // Ottieni l'oggetto Calendar impostato alla data corrente
        Calendar calendar = Calendar.getInstance();

        // Trova il primo giorno della settimana (Lunedì)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // Formattatore per la data
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");

        // Riempie l'array con le date della settimana
        for (int i = 0; i < 7; i++) {
            giorni.add(calendar.getTime().getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return giorni;
    }
}