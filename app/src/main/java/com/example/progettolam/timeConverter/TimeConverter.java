package com.example.progettolam.timeConverter;

import android.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeConverter {
    private Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));

    // funzioni che convertono solo
    public long toLocalTimeZone(long timestamp) {
        calendar.setTimeInMillis(timestamp);
        return calendar.getTimeInMillis();
    };
    public long setDayTimeToZero(long  time) {
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    };
    public long convertToMillis(int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Log.d("DatePicker","convertToMillis"+calendar.getTimeInMillis()+"");
        return calendar.getTimeInMillis();
    }}
