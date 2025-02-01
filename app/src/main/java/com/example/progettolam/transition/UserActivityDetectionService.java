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

    public UserActivityDetectionService(Context context) {
        activityRecognitionClient = ActivityRecognition.getClient(context);
        Intent intent = new Intent(Costum_Intent_Action);
//        intent.setAction("com.google.android.gms.location.ACTIVITY_TRANSITION");
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
    public ActivityTransitionRequest buildTransitionRequest() {
        List<ActivityTransition> transitions = new ArrayList<>();
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.IN_VEHICLE)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
//        transitions.add(new ActivityTransition.Builder()
//                .setActivityType(DetectedActivity.TILTING)
//                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
//                .build());
//        transitions.add(new ActivityTransition.Builder()
//                .setActivityType(DetectedActivity.TILTING)
//                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
//                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build());
        transitions.add(new ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build());
        return new ActivityTransitionRequest(transitions);
    }

    @SuppressLint("MissingPermission")
    public void startActivityUpdates(ActivityTransitionRequest request, PendingIntent pendingIntents) {
        activityRecognitionClient.requestActivityTransitionUpdates(request, pendingIntents)

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
