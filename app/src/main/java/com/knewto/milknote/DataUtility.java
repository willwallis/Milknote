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
 * Assist with creating meta-data for transcription inserts
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
        String timeText = rightNow.get(Calendar.HOUR) + ":" +
                rightNow.get(Calendar.MINUTE) + " " +
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
}
