package com.example.progettolam.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.progettolam.R;
import com.example.progettolam.timeConvertitor.TimeConverter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import com.example.progettolam.struct.Filter;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ModalBottomSheet#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ModalBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ModalBottomSheet";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private MaterialSwitch swWalking, swSitting, swDriving,swUnknown;
    private AppCompatTextView txFirstActivity,txSecondActivity,txThirdActivity,txForthActivity;
    private TextInputEditText txStartFilter, txEndFilter;
    private Chip resetFilterButton, activeFilterButton;
    private Filter filter;
    private TimeConverter timeConvertitor;
    private long startTimeFilter, endTimeFilter;

    public ModalBottomSheet() {
        // Required empty public constructor
    }

    public static ModalBottomSheet newInstance(String param1, String param2) {
        ModalBottomSheet fragment = new ModalBottomSheet();
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
        return inflater.inflate(R.layout.filter_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);

        resetFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swWalking.setChecked(true);
                swDriving.setChecked(true);
                swSitting.setChecked(true);
                swUnknown.setChecked(true);
                txStartFilter.setText("");
                txEndFilter.setText("");
            }
        });
        activeFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiFilter();
                Log.d("DatePicker","filter in modal:"+filter.toString());
                if (onFilterSentListener !=null){

                    onFilterSentListener.SentFilter(filter,filter.isNotFiltered());
                }
                dismiss();
            }
        });
        txStartFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(txStartFilter,"start");
            }
        });
        txEndFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDatePicker(txEndFilter,"end");
            }
        });

    }

    private void openDatePicker(TextInputEditText txField,String txFieldFlag) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    if (txFieldFlag.equals("start")){
                        filter.setStart(timeConvertitor.convertToMillis(year1,month1,dayOfMonth));
                    }else {
                        filter.setEnd(timeConvertitor.convertToMillis(year1,month1,dayOfMonth));
                    }
                    txField.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();

    }

    ;

    private void initiFilter() {
        String firstAct = txFirstActivity.getText().toString();
        String secondAct = txSecondActivity.getText().toString();
        String thirdAct = txThirdActivity.getText().toString();
        String fourthAct = txForthActivity.getText().toString();

        filter.setWalking(swWalking.isChecked() ? firstAct : "");
//        swWalking.setChecked(swWalking.isChecked());
        filter.setSitting(swSitting.isChecked() ? secondAct : "");
//        swDriving.setChecked(swDriving.isChecked());
        filter.setDriving(swDriving.isChecked() ? thirdAct : "");
//        swSitting.setChecked(swSitting.isChecked());
        filter.setUnknown(swUnknown.isChecked() ? fourthAct : "");

    }

    private void initView(View view) {
        timeConvertitor=new TimeConverter();
        swWalking =view.findViewById(R.id.sw_walking);
        swDriving =view.findViewById(R.id.sw_Driving);
        swSitting =view.findViewById(R.id.sw_Sitting);
        swUnknown =view.findViewById(R.id.sw_Unknown);
        swWalking.setChecked(true);
        swDriving.setChecked(true);
        swSitting.setChecked(true);
        swUnknown.setChecked(true);
        txFirstActivity =view.findViewById(R.id.tx_first_activity);
        txSecondActivity =view.findViewById(R.id.tx_second_activity);
        txThirdActivity =view.findViewById(R.id.tx_third_activity);
        txForthActivity =view.findViewById(R.id.tx_forth_activity);

        txFirstActivity.setText("Walking");
        txSecondActivity.setText("Sitting");
        txThirdActivity.setText("Driving");
        txForthActivity.setText("Unknown");

        txStartFilter =view.findViewById(R.id.tx_start_filter);
        txEndFilter =view.findViewById(R.id.tx_end_filter);

        resetFilterButton =view.findViewById(R.id.reset_filter_button);
        activeFilterButton =view.findViewById(R.id.active_filter_button);

        filter = new Filter();
    }

    private OnSentFilterListener onFilterSentListener;
    public interface OnSentFilterListener {
        void SentFilter(Filter filter, Boolean flag);
    }
    public void setOnSentFilterListener(OnSentFilterListener sentFilterListener) {
        this.onFilterSentListener = sentFilterListener;
    }


}