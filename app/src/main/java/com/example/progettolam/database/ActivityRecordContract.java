package com.example.progettolam.database;

import android.provider.BaseColumns;

public class ActivityRecordContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ActivityRecordContract() {}

    /* Inner class that defines the table contents */
    public static class RecordsEntry implements BaseColumns {
        public static final String TABLE_NAME = "records";
        public static final String COLUMN_NAME = "Activity_Name";
        public static final String COLUMN_DURATION = "Duration";
        public static final String COLUMN_STEP = "Step";
//        public static final String COLUMN_START = "Start";
        public static final String COLUMN_START_TIME = "Start_Time";
        public static final String COLUMN_START_DAY = "Start_Day";
//        public static final String COLUMN_END ="End";
        public static final String COLUMN_END_TIME ="End_Time";
        public static final String COLUMN_END_DAY ="End_Day";
    }
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RecordsEntry.TABLE_NAME + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY," +
                    RecordsEntry.COLUMN_NAME + " TEXT," +
                    RecordsEntry.COLUMN_DURATION + " INTEGER," +
                    RecordsEntry.COLUMN_STEP + " INTEGER," +
                    RecordsEntry.COLUMN_START_TIME + " INTEGER," +
                    RecordsEntry.COLUMN_END_TIME  + " INTEGER," +
                    RecordsEntry.COLUMN_START_DAY + " INTEGER," +
                    RecordsEntry.COLUMN_END_DAY + " INTEGER)" ;
//                    RecordsEntry.COLUMN_START + " TEXT," +
//                    RecordsEntry.COLUMN_END + " TEXT)" ;

    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RecordsEntry.TABLE_NAME;

}
