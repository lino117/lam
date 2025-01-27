package com.example.progettolam.fragment

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.progettolam.R
import com.example.progettolam.database.ActivityRecordDbHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter

class statisticFragment : Fragment() {
    private var barChart: BarChart? = null
    private var pieChart: PieChart? = null
    private var dpHelper: ActivityRecordDbHelper? = null
    private lateinit var  db: SQLiteDatabase
    private var mParam1: String? = null
    private var mParam2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
        dpHelper = ActivityRecordDbHelper(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        barChart = view.findViewById(R.id.bar_chart)
        pieChart = view.findViewById(R.id.pie_chart)

        dpHelper = ActivityRecordDbHelper(requireContext())

        db = dpHelper!!.readableDatabase

        initPieChart(dpHelper!!.getMonthlyActivity(db))

        initBarChart(dpHelper!!.getWeeklySteps(db))
    }

    private fun initBarChart(cursor: Cursor) {
        val title = "Step Situation"
        val barEntries = ArrayList<BarEntry>()
        val giorni = ArrayList<String>()
        val valori = ArrayList<Int>()
        while (cursor.moveToNext()) {
            val day = cursor.getString(cursor.getColumnIndexOrThrow("Start_Day"))
            val steps = cursor.getInt(cursor.getColumnIndexOrThrow("total_pass"))
            giorni.add(day)
            valori.add(steps)
        }
        cursor.close()

        for (i in giorni.indices) {
            barEntries.add(BarEntry(i.toFloat(), valori[i].toFloat()))
        }
        val xAixs = barChart!!.xAxis

        xAixs.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return giorni[value.toInt()]
            }
        }
        xAixs.position = XAxis.XAxisPosition.BOTTOM
        xAixs.granularity = 1f
        val barDataSet = BarDataSet(barEntries, title)
        val barData = BarData(barDataSet)
        barChart!!.data = barData
        barChart!!.invalidate()
    }

    private fun initPieChart(cursor: Cursor) {
        val pieEntries = ArrayList<PieEntry>()
        val etichette = ArrayList<String>()
        val valori = ArrayList<Int>()
        while (cursor.moveToNext()) {
            val nomeAttivita = cursor.getString(cursor.getColumnIndexOrThrow("Activity_Name"))
            val totaleTempoSpeso = cursor.getInt(cursor.getColumnIndexOrThrow("totale_tempo_speso"))
            etichette.add(nomeAttivita)
            valori.add(totaleTempoSpeso)
        }

        cursor.close()

        for (i in etichette.indices) {
            // Crea un PieEntry usando il valore e l'etichetta corrispondente
            pieEntries.add(PieEntry(valori[i].toFloat(), etichette[i]))
        }

        val colors = ArrayList<Int>()
        colors.add(requireContext().getColor(R.color.purple))
        colors.add(requireContext().getColor(R.color.light_blue))
        colors.add(requireContext().getColor(R.color.grey))


        val pieDataSet = PieDataSet(pieEntries, "Monthly Activity")

        pieDataSet.setDrawIcons(false)
        pieDataSet.colors = colors
        val pieData = PieData(pieDataSet)

        pieChart!!.data = pieData
        pieChart!!.invalidate()
    }


    override fun onDestroy() {
        super.onDestroy()
        dpHelper!!.close()
        db!!.close()
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): statisticFragment {
            val fragment = statisticFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}