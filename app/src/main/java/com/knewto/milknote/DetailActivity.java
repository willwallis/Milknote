package com.knewto.milknote;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.knewto.milknote.DetailFragment.Layout;

/**
 * Detail Activity
 * displays details of transcribed note and allows editing and trashing of note
 * - onCreate: load intent, map and set text view values, load ad, create fab, set layout based on folder
 * - onCreateOptionsMenu: READ - edit, trash, share, EDIT - save, RESTORE - trashed record restore
 * - createShareIntent: Creates share intent used by share action provider
 * - onOptionsItemSelected: trash, save, or restore record based on selection
 * - mapViews: set view variables equal to textviews, etc.
 * - loadIntent: set value variables equal to content of intent
 * - setViewText: set view variables equal to value variables
 * - editRecord: switch to edit text, hide fab, change menu, and show keyboard.
 * - saveRecord: set text and value variable, update database, show fab, change menu, and hide keyboard.
 * - trashRecord: Update record folder, navigate to main activity with trash flag and note id set.
 * - restoreRecord: Update folder, change layout view, update menu, and show fab
 * - fabVisible: Make Fab visible based on boolean input.
 */

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    String noteID = "";
    String folder = "";

    // Layout variables, uses enum from DetailFragment
    private Layout currentLayout;

    private ShareActionProvider mShareActionProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            currentLayout = (Layout)savedInstanceState.get("layout");
            noteID = savedInstanceState.getString("noteID");
            if (currentLayout.equals(Layout.EDIT)){
                // Set up the edit view
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); // Show keyboard
            } else if (currentLayout.equals(Layout.RESTORE)){
                // Any changes for restore mode
            }
        }
        else {
            // First Load
            // Get values from intent and load fields
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("ID")) {
                noteID = intent.getStringExtra("ID");
                folder = intent.getStringExtra("Folder");
            }
            // Set layout based on whether record is in Trash
            if(folder.equals(this.getResources().getString(R.string.trash_note_folder))){
                currentLayout = Layout.RESTORE;
            } else {
                currentLayout = Layout.READ;
            }
        }

        // Insert fragment
        if (findViewById(R.id.detail_fragment) != null) {
            if (savedInstanceState != null) {} else {
                DetailFragment detailFragment = new DetailFragment();
                // Set folder to load.
                Bundle data = new Bundle();
                data.putString("noteID", noteID);
                Log.v(TAG, currentLayout.name());
                data.putSerializable("layout", currentLayout);
                detailFragment.getArguments().putBundle("note_data", data);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.detail_fragment, detailFragment).commit();
            }
        }

        // Create toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);                   // Setting toolbar as the ActionBar with setSupportActionBar() call
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setTitle("");

        // Load Ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch(currentLayout){
            case READ:
                getMenuInflater().inflate(R.menu.menu_detail_text, menu);
                break;
            case EDIT:
                getMenuInflater().inflate(R.menu.menu_detail_edit, menu);
                break;
            case RESTORE:
                getMenuInflater().inflate(R.menu.menu_detail_restore, menu);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_trash:
                trashRecord();
                return true;
            case R.id.action_save:
                saveRecord();
                return true;
            case R.id.action_restore:
                restoreRecord();
                return true;
            case R.id.action_edit:
                editRecord();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // Save setup variables on rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("layout", currentLayout);
        outState.putString("noteID", noteID);
        super.onSaveInstanceState(outState);
    }

    // METHODS FOR BUTTON
    private void editRecord(){
        currentLayout = Layout.EDIT; // Sets variable to indicate which actions to show
        switchFragment(currentLayout);
        invalidateOptionsMenu(); // Reset menu to display correct buttons
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); // Show keyboard
    }

    private void saveRecord(){
        currentLayout = Layout.READ;
        switchFragment(currentLayout);
        invalidateOptionsMenu();
        // Hide soft keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    // Refresh list fragment if folder changes
    private void switchFragment(Layout currentLayout) {
        int layoutFlag = 0;
        if(currentLayout == Layout.EDIT){
            layoutFlag = 1;
        }
        DetailFragment detailFragment = (DetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detail_fragment);
        if (detailFragment != null) {
            detailFragment.viewSwitcher(layoutFlag);
        }
    }


    private void trashRecord(){
        String trashFolder = this.getResources().getString(R.string.trash_note_folder);
        int numberUpdate = DataUtility.changeFolder(getApplicationContext(), noteID, trashFolder);
        String recordUpdateYes = getString(R.string.records_trashed) + " " + numberUpdate;
        // Navigate to Detail View & Show snack bar on detail view
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("trashFlag", 1);
        mainIntent.putExtra("recordId", noteID);
        startActivity(mainIntent);
    }

    private void restoreRecord(){
        String defaultFolder = this.getResources().getString(R.string.default_note_folder);
        int numberUpdate = DataUtility.changeFolder(getApplicationContext(), noteID, defaultFolder);
        String recordUpdateYes = getString(R.string.records_restored) + " " + numberUpdate;
        currentLayout = Layout.READ;
        invalidateOptionsMenu();
    }
}
