package com.knewto.milknote.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.knewto.milknote.data.NoteContract.NoteEntry;

/**
 * Created by willwallis on 4/9/16.
 * Using input from Udacity Weather project
 */
public class NoteDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "notes.db";

    public NoteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  See http://sqlite.org/datatype3.html for data types
        final String SQL_CREATE_NOTE_TABLE = "CREATE TABLE " + NoteEntry.TABLE_NAME + " (" +
                NoteEntry._ID + " INTEGER PRIMARY KEY," +
                NoteEntry.COLUMN_NOTE_TEXT + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_DATE_TEXT + " TEXT, " +
                NoteEntry.COLUMN_TIME_TEXT + " TEXT, " +
                NoteEntry.COLUMN_DAY_TEXT + " TEXT, " +
                NoteEntry.COLUMN_RAW_TIME + " REAL NOT NULL, " +
                NoteEntry.COLUMN_LAT + " REAL, " +
                NoteEntry.COLUMN_LONG + " REAL, " +
                NoteEntry.COLUMN_LOCATION_NAME + " TEXT, " +
                NoteEntry.COLUMN_FOLDER + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_EDIT_FLAG + " INTEGER NOT NULL " +
                " );";

        // No foreign keys set for this database
        // Create the database
        sqLiteDatabase.execSQL(SQL_CREATE_NOTE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + NoteEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
