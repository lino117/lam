package com.example.progettolam.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import com.example.progettolam.struct.Record
import java.text.SimpleDateFormat
import java.util.Calendar

class ActivityRecordDbHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ActivityRecordContract.SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(ActivityRecordContract.SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    fun insertData(record: Record): Long {
        val db = writableDatabase

        val values = ContentValues()
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_NAME, record.nameActivity)
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_DURATION, record.duration)
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_STEP, record.step)
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_START_DAY, record.startDay)
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_START_TIME, record.startTime)
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_END_DAY, record.endDay)
        values.put(ActivityRecordContract.RecordsEntry.COLUMN_END_TIME, record.endTime)
        val id = db.insert(ActivityRecordContract.RecordsEntry.TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getAll(db: SQLiteDatabase): Cursor {
        val projection = arrayOf(
            BaseColumns._ID + " AS _id",  // Alias per `_ID`
            ActivityRecordContract.RecordsEntry.COLUMN_NAME,
            ActivityRecordContract.RecordsEntry.COLUMN_DURATION,
            ActivityRecordContract.RecordsEntry.COLUMN_STEP,
            ActivityRecordContract.RecordsEntry.COLUMN_START_TIME,
            ActivityRecordContract.RecordsEntry.COLUMN_START_DAY,
            ActivityRecordContract.RecordsEntry.COLUMN_END_TIME,
            ActivityRecordContract.RecordsEntry.COLUMN_END_DAY
        )
        val orderBy = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " DESC"
        return db.query(
            ActivityRecordContract.RecordsEntry.TABLE_NAME,
            projection,
            null,
            null,
            null,
            null,
            orderBy
        )
    }

    fun getMonthlyActivity(db: SQLiteDatabase): Cursor {
        // voglio visuallizare tempo totale speso
        // per ogni attivita
        // mensilmente
        val projection = arrayOf(
            ActivityRecordContract.RecordsEntry.COLUMN_NAME,
            "SUM(" + ActivityRecordContract.RecordsEntry.COLUMN_DURATION + ") AS totale_tempo_speso"
        )

        val selection = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " >= ? " +
                "AND " + ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " <= ?"
        val selectionArgs = arrayOf(
            getArgsDay("fMonth"),
            getArgsDay("lMonth")
        )

        val groupBy = ActivityRecordContract.RecordsEntry.COLUMN_NAME

        val orderBy = "totale_tempo_speso DESC"

        return db.query(
            ActivityRecordContract.RecordsEntry.TABLE_NAME,
            projection,  // SELECT
            selection,  // WHERE
            selectionArgs,  // Argomenti della WHERE
            groupBy,  // GROUP BY
            null,  // HAVING
            orderBy // ORDER BY
        )
    }

    fun getWeeklySteps(db: SQLiteDatabase): Cursor {
        val projection = arrayOf(
            ActivityRecordContract.RecordsEntry.COLUMN_START_DAY,
            "SUM(" + ActivityRecordContract.RecordsEntry.COLUMN_STEP + ") AS total_pass",
        )

        val selection = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " >= ? " +
                "AND " + ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " <= ? " +
                "AND " + ActivityRecordContract.RecordsEntry.COLUMN_NAME + " = ? "
        val selectionArgs = arrayOf(
            getArgsDay("fWeek"),
            getArgsDay("lWeek"),  //                "20/01/2025",
            //                "26/01/2025",
            "Walking"
        )

        val groupBy = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY

        val orderBy = ActivityRecordContract.RecordsEntry.COLUMN_START_DAY + " ASC"
        return db.query(
            ActivityRecordContract.RecordsEntry.TABLE_NAME,
            projection,  // SELECT
            selection,  // WHERE
            selectionArgs,  // Argomenti della clausola WHERE
            groupBy,  // GROUP BY
            null,  // HAVING
            orderBy // ORDER BY
        )
    }

    private fun getArgsDay(flag: String): String {
        // Ottieni l'istanza di Calendar
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val sdf = SimpleDateFormat("dd/MM/yyyy")

        when (flag) {
            "fMonth" -> calendar[Calendar.DAY_OF_MONTH] = 1 // Imposta il giorno al primo del mese
            "lMonth" -> calendar[Calendar.DAY_OF_MONTH] =
                calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            "fWeek" -> calendar[Calendar.DAY_OF_WEEK] = calendar.firstDayOfWeek
            "lWeek" ->                 // Il giorno parte da domenica....
                calendar.add(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        val day = calendar.time
        Log.d("tag", flag + " " + sdf.format(day))
        return sdf.format(day)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION: Int = 1
        const val DATABASE_NAME: String = "ActivityRecords.db"
    }
}
