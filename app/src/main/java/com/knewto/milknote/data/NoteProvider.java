package com.knewto.milknote.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by willwallis on 4/9/16.
 */
public class NoteProvider extends ContentProvider {
    private static final String TAG = "NoteProvider";

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private NoteDbHelper mOpenHelper;

    static final int NOTE = 100;
    static final int NOTE_WITH_FOLDER = 101;

    @Override
    public boolean onCreate() {
        mOpenHelper = new NoteDbHelper(getContext());
        return true;
    }

    // Urimatcher used to work out which variation of query was asked
    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NoteContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, NoteContract.PATH_NOTE, NOTE);
        matcher.addURI(authority, NoteContract.PATH_NOTE + "/*", NOTE_WITH_FOLDER);
        return matcher;
    }

    ////////////////
    // Query Code
    ///////////////

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "note/*"
            case NOTE_WITH_FOLDER: {
                Log.v(TAG, "Query, Note with folder");
                // Get query parameter
                String folderName = uri.getPathSegments().get(1);

                // Construct query (could have multiple ?)
                selection = NoteContract.NoteEntry.TABLE_NAME+
                        "." + NoteContract.NoteEntry.COLUMN_FOLDER + " = ? ";
                // Construct query arguments (one per ? above)
                selectionArgs = new String[]{folderName};
                sortOrder = NoteContract.NoteEntry._ID + " DESC";

                retCursor =  mOpenHelper.getReadableDatabase().query(
                        NoteContract.NoteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "note"
            case NOTE: {
                Log.v(TAG, "Query, Note");
                sortOrder = NoteContract.NoteEntry._ID + " DESC";
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NoteContract.NoteEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        Log.v(TAG, "setNotificationUri: " + uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case NOTE:
                return NoteContract.NoteEntry.CONTENT_TYPE;
            case NOTE_WITH_FOLDER:
                return NoteContract.NoteEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    //////////////////////
    // Insert
    //////////////////////

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(TAG, "Insert - started");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case NOTE: {
                Log.v(TAG, "Insert - case NOTE");
                long _id = db.insert(NoteContract.NoteEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = NoteContract.NoteEntry.buildNoteUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Log.v(TAG, "Insert - returnURI: " + returnUri);
        getContext().getContentResolver().notifyChange(uri, null);
        Log.v(TAG, "Insert - uri: " + uri);
        return returnUri;
    }

    ////////////////////
    // Delete
    ///////////////////

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "Delete - started");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case NOTE:
                rowsDeleted = db.delete(
                        NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    ///////////////////
    // Update
    ///////////////////

    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.v(TAG, "Update - started");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;

        switch (match) {
            case NOTE:
                Log.v(TAG, "Update - NOTE match");
                rowsUpdated = db.update(NoteContract.NoteEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Log.v(TAG, "Update - rowsUpdated: " + rowsUpdated );
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.v(TAG, "Update - notifyChange: " + uri);
        }
        return rowsUpdated;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
