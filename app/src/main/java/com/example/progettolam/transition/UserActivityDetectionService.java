package com.example.progettolam.transition;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class UserActivityDetectionService {
    private final ActivityRecognitionClient activityRecognitionClient;
    private final PendingIntent pendingIntent;
    private final String Costum_Intent_Action = "com.example.progettolam.transition.TRANSITIONS_RECEIVER_ACTION";

    @SuppressLint("MutableImplicitPendingIntent")
    public UserActivityDetectionService(Context context) {
        activityRecognitionClient = ActivityRecognition.getClient(context);
        Intent intent = new Intent(context, UserActivityDetectionReceiver.class);
        intent.setAction(Costum_Intent_Action);
//        Intent intent = new Intent(Costum_Intent_Action);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT|PendingIntent.FLAG_MUTABLE);
    }
    public ActivityTransitionRequest buildTransitionRequest() {
        List<ActivityTransition> transitions = new ArrayList<>();
        Integer[] activities = {
                DetectedActivity.STILL,
                DetectedActivity.WALKING,
                DetectedActivity.ON_FOOT,
                DetectedActivity.RUNNING,
                DetectedActivity.ON_BICYCLE,
                DetectedActivity.IN_VEHICLE
        };
        for (int activity:
             activities) {
            transitions.add(new ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    .build());
            transitions.add(new ActivityTransition.Builder()
                    .setActivityType(activity)
                    .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                    .build());
        }
        return new ActivityTransitionRequest(transitions);
    }

    @SuppressLint("MissingPermission")
    public void startActivityUpdates(ActivityTransitionRequest request) {
        activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntent)
        .addOnSuccessListener(
               new OnSuccessListener<Void>() {
                   @Override
                   public void onSuccess(Void result) {
                        Log.d("ActivityRecognition", "Updates started");
                   }
               }
        )

        .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ActivityRecognition", "Failed to start updates---->"+e.getMessage());
                    }
                }
        );
    }

    @SuppressLint("MissingPermission")
    public void stopActivityUpdates() {
        activityRecognitionClient.removeActivityTransitionUpdates(pendingIntent)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                pendingIntent.cancel();
                                Log.d("ActivityRecognition", "Updates stopped");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("ActivityRecognition", "Failed to stop updates"+e.getMessage());
                            }
                        }
                );

    }
}
