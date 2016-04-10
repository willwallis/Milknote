package com.knewto.milknote;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by willwallis on 4/9/16.
 */
public class OldSettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
