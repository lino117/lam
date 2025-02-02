package com.example.progettolam.recognition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class recognitionService extends BroadcastReceiver {
        private static final String TAG = "ActivityRecognition";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ActivityRecognitionResult.hasResult(intent)) {
                ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
                List<DetectedActivity> activities = result.getProbableActivities();

                for (DetectedActivity activity : activities) {
                    Log.d(TAG, "Attività: " + getActivityName(activity.getType()) + " - Confidenza: " + activity.getConfidence() + "%");
                    Toast.makeText(context, "Sei in attività: " + getActivityName(activity.getType()), Toast.LENGTH_SHORT).show();
                }
            }
        }

        private String getActivityName(int type) {
            switch (type) {
                case DetectedActivity.IN_VEHICLE: return "In Veicolo";
                case DetectedActivity.ON_BICYCLE: return "In Bici";
                case DetectedActivity.RUNNING: return "Correndo";
                case DetectedActivity.STILL: return "Fermo";
                case DetectedActivity.WALKING: return "Camminando";
                default: return "Sconosciuto";
            }
        }

}
