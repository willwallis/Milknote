package com.knewto.milknote;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Main Activity for Milknote
 * - displays list of notes
 * - has nav drawer to display trash folder or settings
 * - fab begins recording and shows dialog fragment
 * - delete empty trash icon when trash folder open
 *
 * Methods
 * - setState: processes state updates from transcribe service and displays/hides recording dialog
 * - callRecognition: sends signal to transcribe service to start/stop recording
 * - showRecordingDialog: shows the record dialog with indeterminant progress bar
 * - hideRecordDialog: hides the record dialog
 * - onDialogPositiveClick: callRecognition - stops the recording (actually a toggle)
 * - onDialogNegativeClick: required for interface, does nothing
 * - fabVisible: toggle FAB visibility with true/false input
 * - onStateUpdate (broadcast receiver): updated state from transcribe service
 * - trashNotify: displays snackbar with undo when record is trashed and navigated from detailActivity
 * - emptyTrashDialog: confirms user wants to empty trash and deletes records
 * - onSaveInstanceState: stores key state variables (state, layout, and recording flag)
 * - onResume: restarts Service in case it was destoyed while Activity was not active
 *
 * Nav Drawer & Menu Methods
 * - onPrepareOptionsMenu: sets flag if drawer is open
 * - onCreateOptionsMenu: if trash displays delete icon else main menu
 * - onOptionsItemSelected: if click delete button do emptyTrashDialog
 * - DrawerItemClickListener: takes user to main notes, trash, or settings view
 * - refreshFragment: reloads fragment when user picks main notes or trash in drawer
 */

public class MainActivity extends AppCompatActivity implements RecordDialogFragment.RecordDialogListener{

    private static final String TAG = "MainActivity";
    private IntentFilter setStateFilter;

    // Navigation Drawer
    private ListView mDrawerList;
    private String[]  mNavOptions;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private final int MAINNOTE = 0;
    private final int TRASHNOTE = 1;
    private final int SETTINGS = 2;
    private String trashRecordId;

    // Service variables
    private static final String ACTION_TRANSCRIBE = "com.knewto.milknote.action.TRANSCRIBE";
    private static final String ACTION_SETSTATE = "com.knewto.milknote.action.SETSTATE";

    // FAB and recording dialog variables
    RecordDialogFragment recordDialog;
    FloatingActionButton fab;
    CoordinatorLayout.LayoutParams originalParams;

    // Setup Variables
    public String folderName;
    private Layout currentLayout;
    private Boolean recordingFlag;
    private State state = State.IDLE;

     /* State Logic: IDLE -> LISTENING -> PROCESSING -> repeat */
    private enum State {
        IDLE,
        LISTENING,
        PROCESSING
    }

    private enum Layout {
        MAIN,
        TRASH
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set layout template
        setContentView(R.layout.activity_main_material);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            folderName = savedInstanceState.getString("folder");
            state = (State)savedInstanceState.get("state");
            currentLayout = (Layout)savedInstanceState.get("layout");
            recordingFlag = savedInstanceState.getBoolean("recording");
        }
        else {
            // Set new values
            folderName = getResources().getString(R.string.default_note_folder);
            state = State.IDLE;
            currentLayout = Layout.MAIN;
            recordingFlag = false;
            // Check if navigated after trashing record, show snackbar
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("trashFlag")) {
                if (intent.getIntExtra("trashFlag", 0) == 1){
                    trashRecordId = intent.getStringExtra("recordId");
                    trashNotify();
                }
            }
        }

        // Insert fragment
        if (findViewById(R.id.notelist_fragment) != null) {
            if (savedInstanceState != null) {} else {
                NoteListFragment noteListFragment = new NoteListFragment();
                // Set folder to load.
                Bundle data = new Bundle();
                data.putString("folder", folderName);
                noteListFragment.getArguments().putBundle("folder_data", data);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.notelist_fragment, noteListFragment).commit();
            }
        }

        // Start the Speech Transcription Service
        Intent transcribeServiceIntent = new Intent(this, TranscribeService.class);
        startService(transcribeServiceIntent);

        // Add FAB
        fab = (FloatingActionButton) findViewById(R.id.fab);
        originalParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callRecognition();
            }
        });

        // Broadcast listener for state changes
        setStateFilter = new IntentFilter(ACTION_SETSTATE);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(onStateUpdate, setStateFilter);

        // TOOLBAR
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call
        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(displayName(folderName));

        // NAVIGATION DRAWER
        mNavOptions = getResources().getStringArray(R.array.navOptions);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mNavOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(displayName(folderName));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getSupportActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
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
                    folderName = getResources().getString(R.string.default_note_folder);
                    currentLayout = Layout.MAIN;
                    refreshFragment();
                    break;
                case TRASHNOTE:
                    folderName = getResources().getString(R.string.trash_note_folder);
                    currentLayout = Layout.TRASH;
                    refreshFragment();
                    break;
                case SETTINGS:
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;
            }
        }
    }

    // Set label for folders
    private String displayName(String folderName){
        String displayName = getResources().getString(R.string.app_name);
        if(folderName.equals(getResources().getString(R.string.default_note_folder))){
            displayName = getResources().getString(R.string.default_note_display);
        } else if (folderName.equals(getResources().getString(R.string.trash_note_folder))) {
            displayName = getResources().getString(R.string.trash_note_display);
        }
        return displayName;
    }

    // Refresh list fragment if folder changes
    private void refreshFragment() {
        NoteListFragment noteListFragment = (NoteListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.notelist_fragment);
        if (noteListFragment != null) {
            noteListFragment.loadFolder(folderName);
        }
    }

    // Save setup variables on rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("state", state);
        outState.putBoolean("recording", recordingFlag);
        outState.putSerializable("layout", currentLayout);
        outState.putString("folder", folderName);
        super.onSaveInstanceState(outState);
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
        if(currentLayout == Layout.TRASH) {
            getMenuInflater().inflate(R.menu.menu_main_trash, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
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
            case R.id.action_delete:
                emptyTrashDialog();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
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

    // Broadcast receiver for State updates
    private BroadcastReceiver onStateUpdate = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            State newState = State.valueOf(intent.getStringExtra("newState"));
            setState(newState);
        }
    };

    // Displays snackbar and code for un-trash
    private void trashNotify(){
        final CoordinatorLayout cView = (CoordinatorLayout) findViewById(R.id.coord_layout);
        final String restoredMessage = this.getResources().getString(R.string.trash_restored);
        Snackbar snackBar1 = Snackbar.make(cView, this.getResources().getString(R.string.trash_notification), Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (restoreRecord(trashRecordId) > 0) {
                            Snackbar snackbar2 = Snackbar.make(cView, restoredMessage, Snackbar.LENGTH_SHORT);
                            View snackView= snackbar2.getView();
                            TextView tv = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
                            tv.setTextColor(Color.WHITE);
                            snackbar2.show();
                        }
                    }
                });
        View snackView= snackBar1.getView();
        TextView tv = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackBar1.show();

    }

    // Restores record from trash (triggered from snackbar trash confirmation)
    private int restoreRecord(String noteID){
        String defaultFolder = this.getResources().getString(R.string.default_note_folder);
        int numberUpdate = DataUtility.changeFolder(getApplicationContext(), noteID, defaultFolder);
        return numberUpdate;
    }

    // Deletes records in trash permanently
    private void emptyTrashDialog() {
        final String deleteMessage = this.getResources().getString(R.string.trash_deleted);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.empty_dialog_title));
        builder.setMessage(getString(R.string.empty_dialog_message));

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                        int numberDeleted = DataUtility.emptyTrash(getApplicationContext());
                        String trashEmptied = numberDeleted +  " " + deleteMessage;
                        CoordinatorLayout cView = (CoordinatorLayout) findViewById(R.id.coord_layout);
                        Snackbar snackbar3 = Snackbar.make(cView, trashEmptied, Snackbar.LENGTH_SHORT);
                        View snackView= snackbar3.getView();
                        TextView tv = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        snackbar3.show();
                    }
                });

        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // negative button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
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
        if(newState == State.LISTENING && recordingFlag == false){
            showRecordDialog();
        } else if (newState != State.LISTENING && recordingFlag == true){
            hideRecordDialog();
        }
    }

    // Shows recording dialog
    private void showRecordDialog(){
        recordingFlag = true;
        fabVisible(false);
        recordDialog = new RecordDialogFragment();
        recordDialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
    }

    // Hides recording dialog
    private void hideRecordDialog() {
        recordingFlag = false;
        fabVisible(true);
        // Using this method to dismiss as it works after rotation
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("NoticeDialogFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
    }

    // Record Dialog Listeners
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
        callRecognition();
    }

    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    // Set FAB visible or invisible
    private void fabVisible(Boolean visible){
        if(visible){
            // display FAB
            fab.setLayoutParams(originalParams);
            fab.setVisibility(View.VISIBLE);
        }
        else {
            //hide FAB
            CoordinatorLayout.LayoutParams currentLayoutParams = originalParams;
            currentLayoutParams.setAnchorId(View.NO_ID);
            fab.setLayoutParams(currentLayoutParams);
            fab.setVisibility(View.GONE);
        }
    }

}
