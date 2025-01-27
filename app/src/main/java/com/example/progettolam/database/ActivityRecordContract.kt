package com.example.progettolam.database

import android.provider.BaseColumns

object ActivityRecordContract {
    const val SQL_CREATE_ENTRIES: String = "CREATE TABLE " + RecordsEntry.TABLE_NAME + " (" +
            BaseColumns._ID + " INTEGER PRIMARY KEY," +
            RecordsEntry.COLUMN_NAME + " TEXT," +
            RecordsEntry.COLUMN_DURATION + " INTEGER," +
            RecordsEntry.COLUMN_STEP + " INTEGER," +
            RecordsEntry.COLUMN_START_TIME + " INTEGER," +
            RecordsEntry.COLUMN_START_DAY + " INTEGER," +
            RecordsEntry.COLUMN_END_TIME + " INTEGER," +
            RecordsEntry.COLUMN_END_DAY + " INTEGER)"

    const val SQL_DELETE_ENTRIES: String = "DROP TABLE IF EXISTS " + RecordsEntry.TABLE_NAME

    /* Inner class that defines the table contents */
    object RecordsEntry : BaseColumns {
        const val TABLE_NAME: String = "records"
        const val COLUMN_NAME: String = "Activity_Name"
        const val COLUMN_DURATION: String = "Duration"
        const val COLUMN_STEP: String = "Step"
        const val COLUMN_START_TIME: String = "Start_Time"
        const val COLUMN_END_TIME: String = "End_Time"
        const val COLUMN_START_DAY: String = "Start_Day"
        const val COLUMN_END_DAY: String = "End_Day"
    }
}
