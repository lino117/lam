package com.example.progettolam.fragment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.progettolam.R;
import com.example.progettolam.database.activityRecordDbHelper;
import com.example.progettolam.database.activityRecordContract.RecordsEntry;
import com.example.progettolam.struct.Filter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class historyFragment extends Fragment implements ModalBottomSheet.OnSentFilterListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private activityRecordDbHelper dpHelper;
    private SQLiteDatabase db;

    public historyFragment() {
        // Required empty public constructor
    }
    FragmentManager fragmentManager;
    private ListView lvHistory;
    private CursorAdapter adapter;
    private TextView txActivityDetails,txNameFeatures,txDuration,txTime;
    private ImageView ivFilter;
    private Filter filter;


    public static historyFragment newInstance(String param1, String param2) {
        historyFragment fragment = new historyFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initEvent(view);

    }

    private void initEvent(View view) {
        lvHistory.setAdapter(adapter);
        ivFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dopo click, compare il pezzo che fa scegliere i campi di filtro
                Fragment existingFragment = getParentFragmentManager().findFragmentByTag(ModalBottomSheet.TAG);

                if (existingFragment == null || !existingFragment.isVisible()) {
                    ModalBottomSheet modalBottomSheet = new ModalBottomSheet();
                    modalBottomSheet.setOnSentFilterListener(historyFragment.this);
                    modalBottomSheet.show(getParentFragmentManager(), ModalBottomSheet.TAG);
                }

            }
        });
    }


    private void initView(View view) {
        dpHelper = new activityRecordDbHelper(requireContext());
        db = dpHelper.getReadableDatabase();
        ivFilter=view.findViewById(R.id.iv_filter);
        lvHistory=view.findViewById(R.id.lv_history);

        if( dpHelper.getList(db).moveToNext()){
            adapter = new historyCursorAdapter(requireContext(), dpHelper.getList(db), 0);
            adapter.setFilterQueryProvider(constraint -> {
                if (constraint == null || constraint.length() == 0){
                    return dpHelper.getList(db);
                }else {
                    return dpHelper.getFilterCursor(db, filter);
                }
            });
        }else {
            Toast.makeText(requireContext(),"non dati presenti",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void SentFilter(Filter passedFilter, Boolean EmptyFilter) {
        adapter.getFilter().filter(passedFilter.toString());
        filter = passedFilter;
        ivFilter.setSelected(!EmptyFilter);
    }

    public class historyCursorAdapter extends CursorAdapter {

        public historyCursorAdapter(Context context, Cursor cursor, int flags) {
            super(context, cursor, flags);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // Infla il layout per ogni riga
            return LayoutInflater.from(context).inflate(R.layout.history_list_item_layout, parent, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy");
            txNameFeatures=view.findViewById(R.id.tx_name_features);
            txDuration=view.findViewById(R.id.tx_duration);
            txTime=view.findViewById(R.id.tx_time);
            // come vengono visualizzati i dati del cursor
            String activityName = cursor.getString(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_NAME));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_DURATION));
            int step = cursor.getInt(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_STEP));
            long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_START_TIME));
            long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_END_TIME));
            long startDay = cursor.getLong(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_START_DAY));
            long endDay = cursor.getLong(cursor.getColumnIndexOrThrow(RecordsEntry.COLUMN_END_DAY));
            Log.d("tag", startDay +" "+ startTime);
            if (activityName.equals("Walking")) {
                txNameFeatures.setText(String.format("%s - %d steps", activityName, step));
            } else {
                txNameFeatures.setText(activityName);
            }

            txTime.setText("Start at: " + timeFormat.format(new Date(startTime)) +
                    " " + dayFormat.format(new Date(startDay)) +
                    " - End at: " + timeFormat.format(new Date(endTime)) +
                    " " + dayFormat.format(new Date(endDay)));

            txDuration.setText(String.format("%02d:%02d:%02d",duration/3600,(duration%3600)/60,(duration%60)));
        }

    }

}