package com.knewto.milknote;

import android.content.ContentValues;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.knewto.milknote.data.NoteContract;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by willwallis on 4/10/16.
 * Assist with data transactions
 * - insertRecord: inserts a new record
 * - getLocationName: creates user friendly name for a location
 * - updateRecord: updates record text
 * - changeFolder: used to assign records to trash and restore
 * - deleteRecord: delete a specific record
 * - emptyTrash: delete all records in trash
 */

public class DataUtility {
    private static final String TAG = "DataUtility";

    public static Uri insertRecord (Context context, String noteText, Location currentLocation){
        // Edit flag - 0 for new
        int editFlag = 0;

        // Folder - all new records go to Main folder
        String folder = context.getResources().getString(R.string.default_note_folder);

        // Get current time values
        Locale currentLocale = context.getResources().getConfiguration().locale;
        Calendar rightNow = Calendar.getInstance();
        String dateText = rightNow.get(Calendar.DAY_OF_MONTH) + " " +
                rightNow.getDisplayName(Calendar.MONTH, Calendar.LONG, currentLocale) + ", " +
                rightNow.get(Calendar.YEAR);
        String timeText = String.format("%02d:%02d", rightNow.get(Calendar.HOUR), rightNow.get(Calendar.MINUTE)) + " " +
                rightNow.getDisplayName(Calendar.AM_PM, Calendar.LONG, currentLocale);
        String dayText = rightNow.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, currentLocale);
        long rawTime = rightNow.getTimeInMillis();

        // Get location values
        double coordLat = 0;
        double coordLong = 0;
        String locationName = null;
        if (currentLocation != null){
            coordLat = currentLocation.getLatitude();
            coordLong = currentLocation.getLongitude();
            locationName = getLocationName(coordLat, coordLong, context, currentLocale);
        }


        Uri mNewUri;
        // Defines an object to contain the new values to insert
        ContentValues mNewValues = new ContentValues();
        // Set Values
        mNewValues.put(NoteContract.NoteEntry.COLUMN_NOTE_TEXT, noteText);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_DATE_TEXT, dateText);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_TIME_TEXT, timeText);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_DAY_TEXT, dayText);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_RAW_TIME, rawTime);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_LAT, coordLat);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_LONG, coordLong);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_LOCATION_NAME, locationName);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_FOLDER, folder);
        mNewValues.put(NoteContract.NoteEntry.COLUMN_EDIT_FLAG, editFlag);
        // Insert into content provider
        mNewUri = context.getContentResolver().insert(
                NoteContract.NoteEntry.CONTENT_URI,   // the data content URI
                mNewValues                          // the values to insert
        );
        return mNewUri;
    }

    public static String getLocationName(double lat, double lng, Context context, Locale currentLocale) {
        String locationName;
        Geocoder geocoder = new Geocoder(context, currentLocale);
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            locationName = obj.getLocality() + ", " +obj.getAdminArea() + ", " + obj.getCountryName();
            return locationName;
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, e.getMessage());
            return null;
        }
    }

    public static int updateRecord(Context context, String recordId, String newText){
        int mRowsUpdated = 0;
        // Defines an object to contain the updated values
        ContentValues mUpdateValues = new ContentValues();
        // Set Values
        mUpdateValues.put(NoteContract.NoteEntry.COLUMN_NOTE_TEXT, newText);
        mUpdateValues.put(NoteContract.NoteEntry.COLUMN_EDIT_FLAG, 1);

        // Defines selection criteria for the rows you want to update
        String mSelectionClause = NoteContract.NoteEntry._ID +  " = ?";
        String[] mSelectionArgs = {recordId};

        // Make the update
        mRowsUpdated = context.getContentResolver().update(
                NoteContract.NoteEntry.CONTENT_URI,   // the user dictionary content URI
                mUpdateValues,                       // the columns to update
                mSelectionClause,                    // the column to select on
                mSelectionArgs                      // the value to compare to
        );

        return mRowsUpdated;
    }

    public static int changeFolder(Context context, String recordId, String folderName){
        int mRowsUpdated = 0;
        // Defines an object to contain the updated values
        ContentValues mUpdateValues = new ContentValues();
        // Set Values
        //String folder = context.getResources().getString(R.string.trash_note_folder);
        mUpdateValues.put(NoteContract.NoteEntry.COLUMN_FOLDER, folderName);

        // Defines selection criteria for the rows you want to update
        String mSelectionClause = NoteContract.NoteEntry._ID +  " = ?";
        String[] mSelectionArgs = {recordId};

        // Make the update
        mRowsUpdated = context.getContentResolver().update(
                NoteContract.NoteEntry.CONTENT_URI,   // the user dictionary content URI
                mUpdateValues,                       // the columns to update
                mSelectionClause,                    // the column to select on
                mSelectionArgs                      // the value to compare to
        );

        return mRowsUpdated;
    }

    public static int deleteRecord(Context context, String recordId){
        int mRowsDeleted = 0;

        // Defines selection criteria for the rows you want to delete
        String mSelectionClause = NoteContract.NoteEntry._ID +  " = ?";
        String[] mSelectionArgs = {recordId};

        // Delete the records
        mRowsDeleted = context.getContentResolver().delete(
                NoteContract.NoteEntry.CONTENT_URI,   // the user dictionary content URI
                mSelectionClause,                    // the column to select on
                mSelectionArgs                      // the value to compare to
        );

        return mRowsDeleted;
    }

    public static int emptyTrash(Context context){
        int mRowsDeleted = 0;

        // Defines selection criteria for the rows you want to delete
        String mSelectionClause = NoteContract.NoteEntry.COLUMN_FOLDER +  " = ?";
        String folder = context.getResources().getString(R.string.trash_note_folder);
        String[] mSelectionArgs = {folder};

        // Delete the records
        mRowsDeleted = context.getContentResolver().delete(
                NoteContract.NoteEntry.CONTENT_URI,   // the user dictionary content URI
                mSelectionClause,                    // the column to select on
                mSelectionArgs                      // the value to compare to
        );

        return mRowsDeleted;
    }

}
