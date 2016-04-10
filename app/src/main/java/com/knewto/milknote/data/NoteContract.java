package com.knewto.milknote.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by willwallis on 4/9/16.
 */
public class NoteContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.knewto.milknote.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_NOTE = "note";

    /* Inner class that defines the table contents of the notes table */
    public static final class NoteEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTE;

        // Table name
        public static final String TABLE_NAME = "note";

        // Field names
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_DATE_TEXT = "date_text";
        public static final String COLUMN_TIME_TEXT = "time_text";
        public static final String COLUMN_DAY_TEXT = "day_text";
        public static final String COLUMN_RAW_TIME = "raw_time";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LONG = "long";
        public static final String COLUMN_LOCATION_NAME = "location_name";
        public static final String COLUMN_FOLDER = "folder";
        public static final String COLUMN_EDIT_FLAG = "edit_flag";

        public static Uri buildNoteUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


    /*
        Student: This is the buildWeatherLocation function you filled in.
     */
    public static Uri buildWeatherLocation(String locationSetting) {
        return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
    }

    public static Uri buildWeatherLocationWithStartDate(
            String locationSetting, long startDate) {
        long normalizedDate = normalizeDate(startDate);
        return CONTENT_URI.buildUpon().appendPath(locationSetting)
                .appendQueryParameter(COLUMN_DATE, Long.toString(normalizedDate)).build();
    }

    public static Uri buildWeatherLocationWithDate(String locationSetting, long date) {
        return CONTENT_URI.buildUpon().appendPath(locationSetting)
                .appendPath(Long.toString(normalizeDate(date))).build();
    }

    public static String getLocationSettingFromUri(Uri uri) {
        return uri.getPathSegments().get(1);
    }

    public static long getDateFromUri(Uri uri) {
        return Long.parseLong(uri.getPathSegments().get(2));
    }

    public static long getStartDateFromUri(Uri uri) {
        String dateString = uri.getQueryParameter(COLUMN_DATE);
        if (null != dateString && dateString.length() > 0)
            return Long.parseLong(dateString);
        else
            return 0;
    }
}
}
