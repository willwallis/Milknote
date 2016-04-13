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

public class DetailActivity extends AppCompatActivity {
    String noteID = "Error no note found";
    String noteText = "Error no note found";
    String dateText = "Error no note found";
    String timeText = "Error no note found";
    String dayText = "Error no note found";
    String rawTime = "Error no note found";
    String coordLat = "Error no note found";
    String coordLong = "Error no note found";
    String locationName = "Error no note found";
    String folder = "Error no note found";
    String edited = "Error no note found";

    EditText vNoteEditText;
    TextView vNoteText;
    TextView vDateText;
    TextView vTimeText;
    TextView vDayText;
    TextView vRawTime;
    TextView vCoordLat;
    TextView vCoordLong;
    TextView vLocationName;
    TextView vFolder;
    TextView vEdited;

    // FAB Variables
    FloatingActionButton fab;
    CoordinatorLayout.LayoutParams originalParams;

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

        currentLayout = Layout.READ;

        // SET FIELD VALUES BASED ON INCOMING INTENT
        // Map variables to text views
        mapViews();
        // Get values from intent and load fields
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("NoteText")) {
            loadIntent(intent);
        }
        // Set view text to intent variables
        setViewText();

        // Button listeners
        Button editButton = (Button) findViewById(R.id.button_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editRecord();
            }
        });
        Button saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveRecord();
            }
        });
        Button trashButton = (Button) findViewById(R.id.button_trash);
        trashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                trashRecord();
            }
        });
        Button deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
              deleteRecord();
            }
        });

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

        // FAB button that starts editing
        fab = (FloatingActionButton)findViewById(R.id.fab);
        originalParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editRecord();
            }
        });
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

    // SHARE INTENT METHODS
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

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, noteText);
        return shareIntent;
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

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    // Method to map fields in view
    private void mapViews(){
        vNoteText = (TextView) findViewById(R.id.noteText);
        vDateText = (TextView) findViewById(R.id.dateText);
        vTimeText = (TextView) findViewById(R.id.timeText);
        vDayText = (TextView) findViewById(R.id.dayText);
        vRawTime = (TextView) findViewById(R.id.rawTime);
        vCoordLat = (TextView) findViewById(R.id.lat_coord);
        vCoordLong = (TextView) findViewById(R.id.long_coord);
        vLocationName = (TextView) findViewById(R.id.locationName);
        vFolder = (TextView) findViewById(R.id.folder);
        vEdited = (TextView) findViewById(R.id.editBox);
        vNoteEditText = (EditText) findViewById(R.id.noteEditText);
    }

    // Method to load fields from intent
    private void loadIntent(Intent intent){
        noteID = intent.getStringExtra("ID");
        noteText = intent.getStringExtra("NoteText");
        dateText = intent.getStringExtra("DateText");
        timeText = intent.getStringExtra("TimeText");
        dayText = intent.getStringExtra("DayText");
        rawTime = intent.getStringExtra("RawTime");
        coordLat = intent.getStringExtra("CoordLat");
        coordLong = intent.getStringExtra("CoordLong");
        locationName = intent.getStringExtra("LocationName");
        folder = intent.getStringExtra("Folder");
        edited = intent.getStringExtra("Edited");
    }

    // Method to set view text values to variables values
    private void setViewText(){
        vNoteText.setText(noteText);
        vNoteEditText.setText(noteText);
        vDateText.setText(dateText);
        vTimeText.setText(timeText);
        vDayText.setText(dayText);
        vRawTime.setText(rawTime);
        vCoordLat.setText(coordLat);
        vCoordLong.setText(coordLong);
        vLocationName.setText(locationName);
        vFolder.setText(folder);
        vEdited.setText(edited);
    }

    // METHODS FOR BUTTON
    private void editRecord(){
        ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher);
        switcher.showNext(); // viewswitcher swaps textview for edittextview
        vNoteEditText.requestFocus(); // Focuses on edit text
        currentLayout = Layout.EDIT; // Sets variable to indicate which actions to show
        invalidateOptionsMenu(); // Reset menu to display correct buttons
        fabVisible(false); // Hide the FAB
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); // Show keyboard
    }

    private void saveRecord(){
        String modified_text = vNoteEditText.getText().toString(); // Get new values
        vNoteText.setText(modified_text); // Set read text to new value
        noteText = modified_text; // set value to new value for rotate
        // Update the content provider
        int numberUpdate = DataUtility.updateRecord(getApplicationContext(), noteID, modified_text);
        String recordUpdateYes = "Records updated: " + numberUpdate;
        // Switch back to read only mode
        ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher);
        switcher.showPrevious(); //or switcher.showPrevious();
        currentLayout = Layout.READ;
        invalidateOptionsMenu();
        fabVisible(true);
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
        String recordUpdateYes = "Records trashed: " + numberUpdate;
        // Navigate to Detail View
        // Show snack bar on detail view
    }

    private void restoreRecord(){
        String trashFolder = this.getResources().getString(R.string.default_note_folder);
        int numberUpdate = DataUtility.changeFolder(getApplicationContext(), noteID, trashFolder);
        String recordUpdateYes = "Records restored: " + numberUpdate;
        currentLayout = Layout.READ;
        invalidateOptionsMenu();
        fabVisible(true);
    }

    // Should be moved to main activity
    private void deleteRecord(){
        int numberUpdate = DataUtility.deleteRecord(getApplicationContext(), noteID);
        String recordUpdateYes = "Records deleted: " + numberUpdate;
        Toast.makeText(getApplicationContext(), recordUpdateYes, Toast.LENGTH_SHORT).show();
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
