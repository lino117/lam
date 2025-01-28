package com.example.progettolam.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.progettolam.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import com.example.progettolam.struct.Filter;

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
    private MaterialSwitch swWalking, swSitting, swDriving;
    private AppCompatTextView txFirstActivity,txSecondActivity,txThirdActivity;
    private TextInputEditText txStartFilter, txEndFilter;
    private Chip resetFilterButton, activeFilterButton;
    private Filter filter;

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
                txStartFilter.setText("");
                txEndFilter.setText("");
            }
        });
        activeFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initiFilter();

                if (onFilterSentListener !=null){

                    onFilterSentListener.SentFilter(filter,filter.isNotFiltered());
                }
                dismiss();
            }
        });
    };

    private void initiFilter() {
        String firstAct = txFirstActivity.getText().toString();
        String secondAct = txSecondActivity.getText().toString();
        String thirdAct = txThirdActivity.getText().toString();

        filter.setWalking(swWalking.isChecked() ? firstAct : "");
//        swWalking.setChecked(swWalking.isChecked());
        filter.setSitting(swSitting.isChecked() ? secondAct : "");
//        swDriving.setChecked(swDriving.isChecked());
        filter.setDriving(swDriving.isChecked() ? thirdAct : "");
//        swSitting.setChecked(swSitting.isChecked());

        filter.setStart(txStartFilter.getText().toString());
        filter.setEnd(txEndFilter.getText().toString());
    }

    private void initView(View view) {
        swWalking =view.findViewById(R.id.sw_walking);
        swDriving =view.findViewById(R.id.sw_Driving);
        swSitting =view.findViewById(R.id.sw_Sitting);
        swWalking.setChecked(true);
        swDriving.setChecked(true);
        swSitting.setChecked(true);
        txFirstActivity =view.findViewById(R.id.tx_first_activity);
        txSecondActivity =view.findViewById(R.id.tx_second_activity);
        txThirdActivity =view.findViewById(R.id.tx_third_activity);

        txFirstActivity.setText("Walking");
        txSecondActivity.setText("Sitting");
        txThirdActivity.setText("Driving");

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