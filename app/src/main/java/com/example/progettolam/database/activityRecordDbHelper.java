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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class activityRecordDbHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "ActivityRecords.db";

    public activityRecordDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(activityRecordContract.SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(activityRecordContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public long insertData(Record record){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(activityRecordContract.RecordsEntry.COLUMN_NAME, record.getNameActivity());
        values.put(activityRecordContract.RecordsEntry.COLUMN_DURATION, record.getDuration());
        values.put(activityRecordContract.RecordsEntry.COLUMN_STEP, record.getStep());
        values.put(activityRecordContract.RecordsEntry.COLUMN_START_DAY, record.getStart_day());
        values.put(activityRecordContract.RecordsEntry.COLUMN_START_TIME, record.getStart_time());
        values.put(activityRecordContract.RecordsEntry.COLUMN_END_DAY, record.getEnd_day());
        values.put(activityRecordContract.RecordsEntry.COLUMN_END_TIME, record.getEnd_time());
        long id = db.insert(activityRecordContract.RecordsEntry.TABLE_NAME,null,values);
        db.close();
        return id;
    }
    public Cursor getAll(SQLiteDatabase db){
        String orderBy = activityRecordContract.RecordsEntry.COLUMN_START_DAY + " DESC";
        return  db.query(
                activityRecordContract.RecordsEntry.TABLE_NAME,
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
                activityRecordContract.RecordsEntry.COLUMN_NAME,
               "SUM("+activityRecordContract.RecordsEntry.COLUMN_DURATION+") AS totale_tempo_speso"
        };

        String selection =  activityRecordContract.RecordsEntry.COLUMN_START_DAY + " >= ? " +
                "AND " + activityRecordContract.RecordsEntry.COLUMN_START_DAY + " <= ?";
        String[] selectionArgs = {
                Long.toString(getArgsDay("fMonth")),
                Long.toString(getArgsDay("lMonth"))
        };

        String groupBy = activityRecordContract.RecordsEntry.COLUMN_NAME;

        String orderBy = "totale_tempo_speso DESC";

        return db.query(
                activityRecordContract.RecordsEntry.TABLE_NAME,
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
                activityRecordContract.RecordsEntry.COLUMN_START_DAY,
                "SUM(" + activityRecordContract.RecordsEntry.COLUMN_STEP + ") AS total_pass",
        };

        String selection =  activityRecordContract.RecordsEntry.COLUMN_START_DAY + " >= ? " +
                "AND " + activityRecordContract.RecordsEntry.COLUMN_START_DAY + " <= ? " +
                "AND " + activityRecordContract.RecordsEntry.COLUMN_NAME + " = ? ";
        long fWeek = getArgsDay("fWeek");
        long lWeek = getArgsDay("lWeek");

        String[] selectionArgs = {
                Long.toString(fWeek),
                Long.toString(lWeek),
//                "27/01/2025",
//                "01/02/2025",
                "Walking"
        };

        String groupBy = activityRecordContract.RecordsEntry.COLUMN_START_DAY;

        String orderBy = activityRecordContract.RecordsEntry.COLUMN_START_DAY + " ASC";
        return db.query(
                activityRecordContract.RecordsEntry.TABLE_NAME,
                projection,       // SELECT
                selection,        // WHERE
                selectionArgs,    // Argomenti della clausola WHERE
                groupBy,          // GROUP BY
                null,             // HAVING
                orderBy           // ORDER BY
        );

    }
    private Long getArgsDay(String  flag) {
        // Ottieni l'istanza di Calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);

        switch (flag){
            case "fMonth":
                calendar.set(Calendar.DAY_OF_MONTH, 1);  // Imposta il giorno al primo del mese
                break;
            case "lMonth":
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case "fWeek":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                break;
            case "lWeek":
                // Il giorno parte da domenica....
                calendar.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                break;
        }
        // Imposta (00:00:00.000)
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date day = calendar.getTime();
        Log.d("barChart",flag + " "+ day);
        // ritorna una data con un ora in meno per fuso orario
        return day.getTime() - 3600000;
    };
    public Cursor getFilterCursor(SQLiteDatabase db, Filter filter) {
        ArrayList<String> selectionArray = new ArrayList<>();
        ArrayList<String> selectionArgsArray = new ArrayList<>();

        selectionArray.add( "("+activityRecordContract.RecordsEntry.COLUMN_NAME + "= ? "+
                "OR " + activityRecordContract.RecordsEntry.COLUMN_NAME +"= ? "+
                "OR " + activityRecordContract.RecordsEntry.COLUMN_NAME +"= ? )");

        selectionArgsArray.add(filter.getWalking());
        selectionArgsArray.add(filter.getDriving());
        selectionArgsArray.add(filter.getSitting());

        if (!filter.getStart().isEmpty()){
            selectionArray.add("AND "+activityRecordContract.RecordsEntry.COLUMN_START_DAY + " >= ? ");
            selectionArgsArray.add(filter.getStart());
        }
        if (!filter.getEnd().isEmpty()){
            selectionArray.add("AND "+activityRecordContract.RecordsEntry.COLUMN_END_DAY + " <= ? ");
            selectionArgsArray.add(filter.getEnd());
        }

        String[] selection = new String[selectionArray.size()];
        selectionArray.toArray(selection);

        String[] selectionArgs = new String[selectionArgsArray.size()];
        selectionArgsArray.toArray(selectionArgs);

        String orderBy = activityRecordContract.RecordsEntry.COLUMN_START_DAY + " DESC";
        return  db.query(
                activityRecordContract.RecordsEntry.TABLE_NAME,
                null,
                TextUtils.join(" ", selection),
                selectionArgs,
                null,
                null,
                orderBy,
                null
        );
    }
}
