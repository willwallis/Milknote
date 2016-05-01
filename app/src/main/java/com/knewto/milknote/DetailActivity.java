package com.knewto.milknote;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

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
    String noteID = "";
    String noteText = "Error no note found";
    String noteEditText = "";
    String dateText = "";
    String timeText = "";
    String dayText = "";
    String locationName = "";
    String folder = "";

    String formattedTime;
    String formattedDate;
    String formattedLocation;

    EditText vNoteEditText;
    TextView vNoteText;
    TextView vDayDate;
    TextView vTimeText;
    TextView vLocationName;

    private ShareActionProvider mShareActionProvider;

    private Layout currentLayout;
    private enum Layout {
        READ,
        EDIT,
        RESTORE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        // Map variables to text views
        mapViews();

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            currentLayout = (Layout)savedInstanceState.get("layout");
            noteID = savedInstanceState.getString("noteID");
            noteText = savedInstanceState.getString("noteText");
            noteEditText = savedInstanceState.getString("noteEditText");
            formattedDate = savedInstanceState.getString("formattedDate");
            formattedTime = savedInstanceState.getString("formattedTime");
            formattedLocation = savedInstanceState.getString("formattedLocation");
            folder = savedInstanceState.getString("folder");
            if (currentLayout.equals(Layout.EDIT)){
                // Set up the edit view
                ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher);
                switcher.showNext(); // viewswitcher swaps textview for edittextview
                vNoteEditText.requestFocus(); // Focuses on edit text
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); // Show keyboard
            } else if (currentLayout.equals(Layout.RESTORE)){
                // Any changes for restore mode
            }
        }
        else {
            // First Load
            // Get values from intent and load fields
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("NoteText")) {
                loadIntent(intent);
            }
            // Set layout based on whether record is in Trash
            if(folder.equals(this.getResources().getString(R.string.trash_note_folder))){
                currentLayout = Layout.RESTORE;
            } else {
                currentLayout = Layout.READ;
            }
        }

        // Set view text to intent variables
        setViewText();

        // Load Ad
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch(currentLayout){
            case READ:
                getMenuInflater().inflate(R.menu.menu_detail_text, menu);
                // Locate MenuItem with ShareActionProvider
                MenuItem item = menu.findItem(R.id.menu_item_share);
                // Fetch and store ShareActionProvider
                mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                setShareIntent();
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

    // Call to update the share intent
    private void setShareIntent() {
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, noteText);
            String subjectLine = "Note from: " + dateText;
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subjectLine);
            mShareActionProvider.setShareIntent(shareIntent);
        }
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
        outState.putString("noteText", noteText);
        outState.putString("noteEditText", noteEditText);
        outState.putString("formattedDate", formattedDate);
        outState.putString("formattedTime", formattedTime);
        outState.putString("formattedLocation", formattedLocation);
        outState.putString("folder", folder);
        super.onSaveInstanceState(outState);
    }

    // Method to map fields in view
    private void mapViews(){
        vNoteText = (TextView) findViewById(R.id.noteText);
        vDayDate =  (TextView) findViewById(R.id.dayDateText);
        vTimeText = (TextView) findViewById(R.id.timeText);
        vLocationName = (TextView) findViewById(R.id.locationName);
        vNoteEditText = (EditText) findViewById(R.id.noteEditText);
    }

    // Method to load fields from intent
    private void loadIntent(Intent intent){
        noteID = intent.getStringExtra("ID");
        noteText = intent.getStringExtra("NoteText");
        dateText = intent.getStringExtra("DateText");
        timeText = intent.getStringExtra("TimeText");
        dayText = intent.getStringExtra("DayText");
        locationName = intent.getStringExtra("LocationName");
        folder = intent.getStringExtra("Folder");
        formattedTime = getString(R.string.label_time) +  " " + timeText;
        formattedDate = getString(R.string.label_date) +  " " + dayText + " - " + dateText;
        formattedLocation = getString(R.string.label_location) + " " + locationName;
    }

    // Method to set view text values to variables values
    private void setViewText(){
        vNoteText.setText(noteText);
        vNoteEditText.setText(noteText);
        vDayDate.setText(formattedDate);
        vTimeText.setText(formattedTime);
        vLocationName.setText(formattedLocation);
    }

    // METHODS FOR BUTTON
    private void editRecord(){
        ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher);
        switcher.showNext(); // viewswitcher swaps textview for edittextview
        vNoteEditText.requestFocus(); // Focuses on edit text
        currentLayout = Layout.EDIT; // Sets variable to indicate which actions to show
        invalidateOptionsMenu(); // Reset menu to display correct buttons
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); // Show keyboard
    }

    private void saveRecord(){
        String modified_text = vNoteEditText.getText().toString(); // Get new values
        vNoteText.setText(modified_text); // Set read text to new value
        noteText = modified_text; // set value to new value for rotate
        // Update the content provider
        int numberUpdate = DataUtility.updateRecord(getApplicationContext(), noteID, modified_text);
        String recordUpdateYes = getString(R.string.records_updated) + " " + numberUpdate;
        // Switch back to read only mode
        ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher);
        switcher.showPrevious(); //or switcher.showPrevious();
        currentLayout = Layout.READ;
        invalidateOptionsMenu();
        // Hide soft keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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
