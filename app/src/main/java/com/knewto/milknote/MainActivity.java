package com.knewto.milknote;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.knewto.milknote.data.NoteContract;

public class MainActivity extends AppCompatActivity {

    // Nuance functionality variables
    // NOTE - Nuance requires targetting 22, 23 won't work!
    private State state = State.IDLE;

    // Layout variables
    private TextView statusText;
    private TextView transcription;
    private Button recordNote;
    private static final String TAG = "MainActivity";

    // Navigation Drawer
    private ListView mDrawerList;
    private String[]  mNavOptions;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    // Service variables
    private static final String ACTION_TOAST = "com.knewto.milknote.action.TOAST";
    private static final String ACTION_TRANSCRIBE = "com.knewto.milknote.action.TRANSCRIBE";
    private static final String ACTION_UIUPDATE = "com.knewto.milknote.action.UIUPDATE";
    private static final String ACTION_SETSTATE = "com.knewto.milknote.action.SETSTATE";
    private static final String ACTION_STATUS = "com.knewto.milknote.action.STATUS";


    private IntentFilter uiUpdateFilter;
    private IntentFilter setStateFilter;
    private IntentFilter setStatusFilter;

    private final int RECOGNIZE = 100;
    private final int STOP = 200;
    private final int CANCEL = 300;

    private final int MAINNOTE = 0;
    private final int TRASHNOTE = 1;
    private final int SETTINGS = 2;
    public String folderName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folderName = getResources().getString(R.string.default_note_folder);

        setContentView(R.layout.activity_main_material);

        // Insert fragment
        if (findViewById(R.id.notelist_fragment) != null) {
            if (savedInstanceState != null) {} else {
                NoteListFragment noteListFragment = new NoteListFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.notelist_fragment, noteListFragment).commit();
            }
        }

        // FAB button that starts recording
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        //        Snackbar.make(view, "Hello Snackbar", Snackbar.LENGTH_LONG).show();
                callRecognition();
            }
        });

        // Start the Speech Transcription Service
        Intent transcribeServiceIntent = new Intent(this, TranscribeService.class);
        startService(transcribeServiceIntent);

        // Refresh the UI in case transcription occurred from notification/widget
        updateUI();

        // Create toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call

        // Create Broadcast receiver for UI and State updates from Service
        uiUpdateFilter = new IntentFilter(ACTION_UIUPDATE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onUIUpdate, uiUpdateFilter);
        setStateFilter = new IntentFilter(ACTION_SETSTATE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onStateUpdate, setStateFilter);
        setStatusFilter = new IntentFilter(ACTION_STATUS);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onStatusUpdate, setStatusFilter);

        // Set up navigation drawer
        mTitle = mDrawerTitle = getTitle();
        mNavOptions = getResources().getStringArray(R.array.navOptions);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mNavOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // End navigation drawer
    }

    // Methods for Navigation Drawer
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
            switch(position){
                case MAINNOTE:
                    setTitle(getResources().getString(R.string.default_note_display));
                    folderName = getResources().getString(R.string.default_note_folder);
                    refreshFragment();
                    break;
                case TRASHNOTE:
                    setTitle(getResources().getString(R.string.trash_note_display));
                    folderName = getResources().getString(R.string.trash_note_folder);
                    refreshFragment();
                    break;
                case SETTINGS:
                    Intent settingsIntent = new Intent(MainActivity.this, OldSettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
            }
        }
    }

    private void refreshFragment() {
        NoteListFragment noteListFragment = (NoteListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.notelist_fragment);
        if (noteListFragment != null) {
            noteListFragment.loadFolder(folderName);
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        //menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_trash:
                // User chose the "Trash" item, show the app settings UI...
                Toast.makeText(getApplicationContext(), "Trash it!", Toast.LENGTH_SHORT).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    ///// End of navigation methods

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure service is running and recreates notification when app is repopened
        // Start the Speech Transcription Service
        Intent transcribeServiceIntent = new Intent(this, TranscribeService.class);
        startService(transcribeServiceIntent);
    }

    // Storage Methods - Task 1 uses Shared Preference
    private void setSharedPreference(String string){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.pref_result_key), string);
        editor.commit();
    }

    // Broadcast receiver for UI updates
    private BroadcastReceiver onUIUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "onUIUpdate");
            updateUI();
        }
    };

    // Broadcast receiver for State updates
    private BroadcastReceiver onStateUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            State newState = State.valueOf(intent.getStringExtra("newState"));
            setState(newState);
        }
    };

    // Broadcast receiver for Status updates
    private BroadcastReceiver onStatusUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String status = intent.getStringExtra("Status");
            //statusText.setText(status);
        }
    };


    // Updates UI with value from Shared Preference
    private void updateUI(){
        Log.v(TAG, "updateUI");
        String resultString = "No Notes";
        Cursor noteCursor = this.getContentResolver().query(
                NoteContract.NoteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        if (noteCursor != null) {
            noteCursor.moveToFirst();
            resultString = noteCursor.getString(noteCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_TEXT));
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String textString = sharedPref.getString(getString(R.string.pref_result_key), "No notes");
        //transcription.setText(resultString);
    }





    // NUANCE SPEECHKIT METHODS
     // State - defines states
     // setstate - sets state to new state (was used to update UI in sample)
    // All other methods moved to TranscribeService.java

    /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */

    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

     /* Reco transactions */

    private void callRecognition() {
        Intent transcribeIntent = new Intent(ACTION_TRANSCRIBE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(transcribeIntent);
    }

    /**
     * Set the state and update the button text <-- removed for now.
     */
    private void setState(State newState) {
        Log.v(TAG,"setState: " + newState);
        state = newState;
    }

}
