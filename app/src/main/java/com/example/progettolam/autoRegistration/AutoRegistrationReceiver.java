package com.example.progettolam.autoRegistration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.progettolam.database.activityRecordDbHelper;
import com.example.progettolam.struct.Record;

import java.util.Calendar;

public class AutoRegistrationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        activityRecordDbHelper dbHelper = new activityRecordDbHelper(context);
        if (intent != null) {
            String action = intent.getAction();
            if (action.equals("com.example.chronometer.AUTO_REGISTRATION")){
                long duration = intent.getLongExtra("duration", 0);
                String activity = intent.getStringExtra("activity");
                long endTimeActivity = intent.getLongExtra("endTimeActivity",0);
                dbHelper.insertData(createRecord(duration, endTimeActivity, activity));
            }
        }
    }
    private Record createRecord(long duration, long endTimeActivity, String selectedActivity) {
        long startTimeActivity = endTimeActivity - duration*1000;
        Record record = new Record();
        record.setNameActivity(selectedActivity);
        record.setDuration((int) (duration));
        record.setStep(null);
        record.setStart_time(startTimeActivity % 86400000);
        record.setEnd_time(endTimeActivity % 86400000);
        record.setStart_day(setDay(startTimeActivity));
        record.setEnd_day(setDay(endTimeActivity));
        return record;
    }

    private Long setDay(Long  time) {

        // Crea un oggetto Calendar e imposta il tempo
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        // Azzerare le ore, i minuti, i secondi e i millisecondi
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Log.d("day",(int)(calendar.getTimeInMillis()) +"   "+calendar.getTimeInMillis());
        return calendar.getTimeInMillis();
    };

}
