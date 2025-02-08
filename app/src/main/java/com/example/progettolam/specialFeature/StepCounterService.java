package com.example.progettolam.specialFeature;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import androidx.annotation.Nullable;

public class StepCounterService extends Service implements SensorEventListener {
    private static final String TAG = "StepCounterService";
    // parte sensor
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private long stepsAtStart, finalSteps, stepOffset;
    private StepListener stepListener;


    public interface StepListener {
        void onStepCountUpdated(int steps);
    }
    public void setStepListener(StepListener listener) {
        this.stepListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event != null && event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

                // stepsAtStart = 0 quindi assegno quello corrente, ovvero event.valus[0]
                if (stepsAtStart == 0) {
                    stepsAtStart = (long) event.values[0] - stepOffset;
                }
                // ogni volta che cambia, registra quanti passi son stati fatti
                finalSteps = (long) event.values[0] - stepsAtStart;
                if (stepListener != null) {
                    stepListener.onStepCountUpdated((int) finalSteps);
                }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "Sensor accuracy changed: " + i);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepCounterSensor != null) {
                stepsAtStart = 0;
                stepOffset = 0;
                registerSensor();
            }
        } else {
            Log.d(TAG, "Contapassi non e stato attivato corretamente");
        }

    }
    public void unregisterSensor(){
        sensorManager.unregisterListener(this);

    }
    public void registerSensor(){
        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
    }

    // parte del service
    @Nullable
    @Override
    public Binder onBind(Intent intent) {
        return new Binder();
    }

    public void onPause(){
        this.stepsAtStart=0;
    }
    public class Binder extends android.os.Binder {
        public StepCounterService getService() {
            return StepCounterService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterSensor();
    }
}
