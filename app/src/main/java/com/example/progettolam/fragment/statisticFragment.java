package com.example.progettolam.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.util.ArrayList;

public class statisticFragment extends Fragment {


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private BarChart barChart;
    private PieChart pieChart;
    activityRecordDbHelper dpHelper;
    SQLiteDatabase db;
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        barChart = view.findViewById(R.id.bar_chart);
        pieChart = view.findViewById(R.id.pie_chart);

        dpHelper = new activityRecordDbHelper(requireContext());

        db = dpHelper.getReadableDatabase();

        initPieChart(dpHelper.getMonthlyActivity(db));

        initBarChart(dpHelper.getWeeklySteps(db));
    }

    private void initBarChart(Cursor cursor) {
        final String title = "Step Situation";
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<String> giorni = new ArrayList<>();
        ArrayList<Integer> valori = new ArrayList<>();
            while (cursor.moveToNext()) {
                String day = cursor.getString(cursor.getColumnIndexOrThrow("Start_Day"));
                int steps = cursor.getInt(cursor.getColumnIndexOrThrow("total_pass"));
                giorni.add(day);
                valori.add(steps);
            }
            cursor.close();

        for (int i = 0; i < giorni.size(); i++) {
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

        ArrayList<Integer> colors  = new ArrayList<>();
        colors.add(requireContext().getColor(R.color.purple));
        colors.add(requireContext().getColor(R.color.light_blue));
        colors.add(requireContext().getColor(R.color.grey));


        PieDataSet pieDataSet = new PieDataSet(pieEntries,"Monthly Activity");

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
}