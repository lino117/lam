package com.example.progettolam.fragment

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.progettolam.R
import com.example.progettolam.database.ActivityRecordContract.RecordsEntry
import com.example.progettolam.database.ActivityRecordDbHelper

class HistoryFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var dpHelper: ActivityRecordDbHelper? = null
    private lateinit var db: SQLiteDatabase;

    private var lvHistory: ListView? = null
    private var adapter: CursorAdapter? = null
    private var ivFilter: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
        initEvent()
    }

    private fun initEvent() {
        lvHistory!!.adapter = adapter
        ivFilter!!.setOnClickListener {
            // dopo click, compare il pezzo che fa scegliere i campi di filtro
            // una volta scelto il campo e clicck attivaFilter
            // applicarlo al CursorAdpter
            // mettere selector false
        }
    }


    private fun initView(view: View) {
        dpHelper = ActivityRecordDbHelper(requireContext())
        db = dpHelper!!.readableDatabase
        ivFilter = view.findViewById(R.id.iv_filter)
        lvHistory = view.findViewById(R.id.lv_history)
        //        txActivityDetails =view.findViewById(R.id.tx_activity_details);
        adapter = historyCursorAdapter(requireContext(), dpHelper!!.getAll(db), 0)
    }

    inner class historyCursorAdapter(context: Context?, cursor: Cursor?, flags: Int) :
        CursorAdapter(context, cursor, flags) {
        override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
            // Infla il layout per ogni riga
            return LayoutInflater.from(context)
                .inflate(R.layout.history_list_item_layout, parent, false)
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            val txNameFeatures = view.findViewById<TextView>(R.id.tx_name_features)
            val txDuration = view.findViewById<TextView>(R.id.tx_duration)
            val txTime = view.findViewById<TextView>(R.id.tx_time)
            // come vengono visualizzati i dati del cursor
            val activityName =
                cursor.getString(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_NAME))
            val duration = cursor.getInt(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_DURATION))
            val step = cursor.getInt(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_STEP))
            val startTime =
                cursor.getString(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_START_TIME))
            val endTime =
                cursor.getString(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_END_TIME))
            val startDay =
                cursor.getString(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_START_DAY))
            val endDay = cursor.getString(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_END_DAY))
            Log.d("tag", "$startDay $startTime")
            if (activityName == "Walking") {
                txNameFeatures.setText(String.format("%s - %d steps", activityName, step))
            } else {
                txNameFeatures.setText(activityName)
            }
            txTime.setText("Start at: $startTime $startDay - End at: $endTime $endDay")
            txDuration.setText(
                String.format(
                    "%02d:%02d:%02d",
                    duration / 3600,
                    (duration % 3600) / 60,
                    (duration % 60)
                )
            )
        }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"
        fun newInstance(param1: String?, param2: String?): HistoryFragment {
            val fragment = HistoryFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}