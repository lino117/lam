package com.example.progettolam.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.example.progettolam.struct.Filter;
import com.example.progettolam.struct.Record;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class ActivityRecordDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ActivityRecords.db";

    public ActivityRecordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ActivityRecordContract.SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(ActivityRecordContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public long insertData(Record record){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_NAME, record.getNameActivity());
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_DURATION, record.getDuration());
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_STEP, record.getStep());
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_START_DAY, record.getStart_day());
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_START_TIME, record.getStart_time());
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_END_DAY, record.getEnd_day());
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_END_TIME, record.getEnd_time());
        long id = db.insert(ActivityRecordContract.RecordsEntry.TABLE_NAME,null,values);
        db.close();
        return id;
    }
    public Cursor getList(SQLiteDatabase db){
        String orderBy = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " DESC,"
                + ActivityRecordContract.RecordsEntry.COLUMN_START_TIME + " Desc";
        return  db.query(
                ActivityRecordContract.RecordsEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                orderBy
        );

    }
    public Cursor getMonthlyActivity(SQLiteDatabase db){
        // voglio visuallizare tempo totale speso
        // per ogni attivita
        // mensilmente
        String[] projection = {
                ActivityRecordContract.RecordsEntry.COLUMN_NAME,
               "SUM("+ ActivityRecordContract.RecordsEntry.COLUMN_DURATION+") AS totale_tempo_speso"
        };

        String selection =  ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " >= ? " +
                "AND " + ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " <= ?";
        String[] selectionArgs = {
                Long.toString(getFlagDay("fMonth")),
                Long.toString(getFlagDay("lMonth"))
        };

        String groupBy = ActivityRecordContract.RecordsEntry.COLUMN_NAME;

        String orderBy = "totale_tempo_speso DESC";

        return db.query(
                ActivityRecordContract.RecordsEntry.TABLE_NAME,
                projection,       // SELECT
                selection,        // WHERE
                selectionArgs,    // Argomenti della WHERE
                groupBy,          // GROUP BY
                null,             // HAVING
                orderBy           // ORDER BY
        );



    }
    public Cursor getWeeklySteps(SQLiteDatabase db) {
        String[] projection = {
                ActivityRecordContract.RecordsEntry.COLUMN_START_DAY,
                "SUM(" + ActivityRecordContract.RecordsEntry.COLUMN_STEP + ") AS total_pass",
        };

        String selection =  ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " >= ? " +
                "AND " + ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " <= ? " +
                "AND " + ActivityRecordContract.RecordsEntry.COLUMN_NAME + " = ? ";
        long fWeek = getFlagDay("fWeek");
        long lWeek = getFlagDay("lWeek");
        String[] selectionArgs = {
                Long.toString(fWeek),
                Long.toString(lWeek),
                "Walking"
        };

        String groupBy = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY;

        String orderBy = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " ASC";
        return db.query(
                ActivityRecordContract.RecordsEntry.TABLE_NAME,
                projection,       // SELECT
                selection,        // WHERE
                selectionArgs,    // Argomenti della clausola WHERE
                groupBy,          // GROUP BY
                null,             // HAVING
                orderBy           // ORDER BY
        );

    }
    private Long getFlagDay(String  flag) {
        // Ottieni l'istanza di Calendar
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"));
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        switch (flag){
            case "fMonth":
                calendar.set(Calendar.DAY_OF_MONTH, 1);  // Imposta il giorno al primo del mese
                break;
            case "lMonth":
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case "fWeek":
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;
            case "lWeek":
                // Il giorno parte da domenica....
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendar.add(Calendar.DAY_OF_WEEK, 6);
                break;
            case "yesterday":
                calendar.add(Calendar.DAY_OF_MONTH, -1);
                break;
        }
        // Imposta (00:00:00.000)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date day = calendar.getTime();
        Log.d("barChart",flag + " "+ day+" in millis "+(day.getTime()));
        // ritorna una data con un ora in meno per fuso orario
        return day.getTime();
    };
    public Cursor getFilterCursor(SQLiteDatabase db, Filter filter) {
        ArrayList<String> selectionArray = new ArrayList<>();
        ArrayList<String> selectionArgsArray = new ArrayList<>();
//        Log.d("DatePicker","filter in dbhelper: "+filter.toString());
        selectionArray.add( "("+ ActivityRecordContract.RecordsEntry.COLUMN_NAME + "= ? "+
                "OR " + ActivityRecordContract.RecordsEntry.COLUMN_NAME +"= ? "+
                "OR " + ActivityRecordContract.RecordsEntry.COLUMN_NAME +"= ? "+
                "OR " + ActivityRecordContract.RecordsEntry.COLUMN_NAME +"= ? )");

        selectionArgsArray.add(filter.getWalking());
        selectionArgsArray.add(filter.getDriving());
        selectionArgsArray.add(filter.getSitting());
        selectionArgsArray.add(filter.getUnknown());

//        String start = Long.toString(filter.getStart());
//        String end = Long.toString(filter.getEnd());
        long start = filter.getStart();
        long end = filter.getEnd();

        if (start!=0){
            selectionArray.add("AND " + ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " >= ? ");
            selectionArgsArray.add(Long.toString(start));
        }
        if (end!=0){
            selectionArray.add("AND " + ActivityRecordContract.RecordsEntry.COLUMN_END_DAY + " <= ? ");
            selectionArgsArray.add(Long.toString(end));
        }
//        Log.d("DatePicker","selectionArray "+selectionArray.toString());
//        Log.d("DatePicker","selectionArgsArray "+selectionArgsArray.toString());

        String[] selection = new String[selectionArray.size()];
        selectionArray.toArray(selection);

        String[] selectionArgs = new String[selectionArgsArray.size()];
        selectionArgsArray.toArray(selectionArgs);

        String orderBy = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " DESC,"+
                 ActivityRecordContract.RecordsEntry.COLUMN_START_TIME + " DESC";
        return  db.query(
                ActivityRecordContract.RecordsEntry.TABLE_NAME,
                null,
                TextUtils.join(" ", selection),
                selectionArgs,
                null,
                null,
                orderBy,
                null
        );
    }

    public Cursor getYesterdaySteps(SQLiteDatabase db) {
        String[] projection ={
                "SUM(" + ActivityRecordContract.RecordsEntry.COLUMN_STEP + ") AS total_pass",
        };
        String selection =
                ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " = ? " +
                "AND " + ActivityRecordContract.RecordsEntry.COLUMN_NAME +" = ?";
        String[] selectionArgs = {
            Long.toString(getFlagDay("yesterday")),
                "Walking"
        };
        return db.query(
                ActivityRecordContract.RecordsEntry.TABLE_NAME,
                projection,       // SELECT
                selection,        // WHERE
                selectionArgs,    // Argomenti della clausola WHERE
                null,          // GROUP BY
                null,             // HAVING
                null           // ORDER BY
        );
    }
}
