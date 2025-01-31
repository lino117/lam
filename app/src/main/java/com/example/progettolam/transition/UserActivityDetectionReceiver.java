package com.example.progettolam.transition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.progettolam.R;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.example.progettolam.database.activityRecordDbHelper;
import com.example.progettolam.struct.Record;

public class UserActivityDetectionReceiver extends BroadcastReceiver {
    private activityRecordDbHelper dpHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        dpHelper = new activityRecordDbHelper(context);
        SQLiteDatabase db = dpHelper.getWritableDatabase();
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            int type = mostProbableActivity.getType();
            int confidence = mostProbableActivity.getConfidence();

            dpHelper.insertData(setRecord());
            String activityType = getActivityType(type);
            Toast.makeText(context.getApplicationContext(), "Activity: " + activityType + " Confidence: " + confidence, Toast.LENGTH_LONG).show();
            Log.d("ActivityRecognition", "Activity: " + activityType + " Confidence: " + confidence);
        }else {
            Log.d("ActivityRecognition","detection has no result");
            Toast.makeText(context.getApplicationContext(), "nulla ce ", Toast.LENGTH_LONG).show();

        }
    }
    private Record setRecord(){
        Record record = new Record();
        record.setNameActivity(getActivityType(1));
        record.setStep(117);
        return record;
    }
    private String getActivityType(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE: return "In Vehicle";
            case DetectedActivity.STILL: return "Still";
            case DetectedActivity.WALKING: return "Walking";
            case DetectedActivity.UNKNOWN: return "Unknown";
            default: return "Unrecognized";
        }
    }
}
