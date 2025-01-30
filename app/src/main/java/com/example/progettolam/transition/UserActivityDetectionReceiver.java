package com.example.progettolam.transition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class UserActivityDetectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            int type = mostProbableActivity.getType();
            int confidence = mostProbableActivity.getConfidence();

            String activityType = getActivityType(type);
            Log.d("ActivityRecognition", "Activity: " + activityType + " Confidence: " + confidence);
        }
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
