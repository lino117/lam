package com.example.progettolam.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.progettolam.R;
import com.example.progettolam.database.ActivityRecordDbHelper;
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
import java.util.TimeZone;

public class StatisticFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private BarChart barChart;
    private PieChart pieChart;
    ActivityRecordDbHelper dpHelper;
    SQLiteDatabase db;
    NotificationManagerCompat nm;
    private String mParam1;
    private String mParam2;

    public StatisticFragment() {
        // Required empty public constructor
    }   


    public static StatisticFragment newInstance(String param1, String param2) {
        StatisticFragment fragment = new StatisticFragment();
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
        dpHelper = new ActivityRecordDbHelper(requireContext());
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

        dpHelper = new ActivityRecordDbHelper(requireContext());

        db = dpHelper.getReadableDatabase();

        initPieChart(dpHelper.getMonthlyActivity(db));

        initBarChart(dpHelper.getWeeklySteps(db));
    }
    private void initBarChart(Cursor cursor) {
        if (cursor!=null){
        final String title = "Step Situation";
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<Integer> valori = new ArrayList<>();
        final ArrayList<Long> dayInMs = fillWeekDates();
        int counter = 0;
        while (cursor.moveToNext()) {

            long day = cursor.getLong(cursor.getColumnIndexOrThrow("Start_Day"));

            while (counter < dayInMs.size() && dayInMs.get(counter) != day) {
                Log.d("chart","db "+day +" statistic "+dayInMs.get(counter)+" "+counter);
                valori.add(0);
                counter++;
                Log.d("chart",valori.toString());
            }

            int steps = cursor.getInt(cursor.getColumnIndexOrThrow("total_pass"));
            valori.add(steps);
            counter++;
        }
        cursor.close();
        while (counter<dayInMs.size()){
            valori.add(0);
            counter++;
        }
        ArrayList<String> giorni = convertLongToString(dayInMs);

        Log.d("barChart", String.valueOf(valori.size()) );
        Log.d("barChart", valori.toString());


        for (int i = 0; i < dayInMs.size(); i++) {
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
        }else {
            Toast.makeText(requireContext(),"db vuoto",Toast.LENGTH_SHORT).show();
        }


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
        if (cursor!=null){

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
        }else {
            Toast.makeText(requireContext(),"db vuoto",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dpHelper.close();
        db.close();
    }
    public ArrayList<Long> fillWeekDates() {
        ArrayList<Long> giorni = new ArrayList<>();

        // Ottieni l'oggetto Calendar impostato alla data corrente
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        // Trova il primo giorno della settimana (Lunedì)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        // Imposta (00:00:00.000)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // Formattatore per la data
//        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Riempie l'array con le date della settimana
        for (int i = 0; i < 7; i++) {
            giorni.add(calendar.getTime().getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return giorni;
    }
}