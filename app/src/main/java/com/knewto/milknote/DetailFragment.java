package com.knewto.milknote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.knewto.milknote.data.NoteContract;

/**
 * Detail Fragment
 * ----
 */

public class DetailFragment extends Fragment {

    private static final String TAG = "DetailFragment";

    // Fields to be mapped
    View view;
    EditText vNoteEditText;
    TextView vNoteText;
    TextView vDayDate;
    TextView vTimeText;
    TextView vLocationName;

    // Field values
    String noteID;
    String noteText;
    String noteEditText;
    String dateText;
    String timeText;
    String dayText;
    String locationName;
    String folder;
    String formattedTime;
    String formattedDate;
    String formattedLocation;

    // Layout variables
    private Layout currentLayout;
    boolean viewSwitcher = false;
    public enum Layout {
        EMPTY,
        READ,
        EDIT,
        RESTORE
    }

    // Button Variables
    private ShareActionProvider mShareActionProvider;
    ParentActivityResponse mCallback;

    // Container Activity must implement this interface
    public interface ParentActivityResponse {
        public void trashNotify(String trashNoteId);
    }

    // Required empty public constructor
    public DetailFragment() {
        // Allow sending values from Activity
        this.setArguments(new Bundle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initiate fields
        formattedTime = "";
        formattedDate = "";
        formattedDate = "";
        noteText = getString(R.string.no_record_selected);
        folder = "Empty";
        currentLayout = Layout.EMPTY;

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            noteEditText = savedInstanceState.getString("noteEditText");
            currentLayout = (Layout)savedInstanceState.get("layout");
            noteID = savedInstanceState.getString("noteID");
        } else {
            // Get values from bundle
            Bundle data = this.getArguments().getBundle("note_data");
            noteID = data.getString("noteID", "");
        }

        // Add Share Action Provider to Toolbar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_detail, container, false);

        // Maps Views
        mapViews(view);

        // Load and Set Values
        loadNote(noteID);

        return view;
    }

    public void loadNote(String newNoteId){
        Log.v(TAG, "loadNote _ID: " + newNoteId);
        noteID = newNoteId;
        // if noteId is 0 length clear text values
        if(noteID.length() > 0){
            queryData();
            if(currentLayout == Layout.EMPTY){
                currentLayout = Layout.READ;
            }
        } else {
            formattedTime = "";
            formattedDate = "";
            formattedDate = "";
            noteText = getString(R.string.no_record_selected);
            folder = "Empty";
            currentLayout = Layout.EMPTY;
        }
        // Map new values to fields
        setViewText();
        // Set layout based on whether record is in Trash
        if(folder.equals(this.getResources().getString(R.string.trash_note_folder))){
            currentLayout = Layout.RESTORE;
        }

        // Set to edit mode if Layout is edit
        if(currentLayout == Layout.EDIT){
            viewSwitcher(currentLayout);
        }
        // Reset menu based on layout
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch(currentLayout){
            case READ:
                inflater.inflate(R.menu.menu_detail_text, menu);
                // Locate MenuItem with ShareActionProvider
                MenuItem item = menu.findItem(R.id.menu_item_share);
                // Fetch and store ShareActionProvider
                mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                setShareIntent(noteText);
                break;
            case EDIT:
                inflater.inflate(R.menu.menu_detail_edit, menu);
                break;
            case RESTORE:
                inflater.inflate(R.menu.menu_detail_restore, menu);
                break;
        }
    }

    // Call to update the share intent
    public void setShareIntent(String shareNoteText) {
        Log.v(TAG, "setShareIntent");
        if (mShareActionProvider != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareNoteText);
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (ParentActivityResponse) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement ParentActivityResponse");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("noteEditText", noteEditText);
        outState.putSerializable("layout", currentLayout);
        outState.putString("noteID", noteID);
        Log.v(TAG, "onSaveInstanceState _ID: " + noteID);
    }

    // Method to map fields in view
    private void mapViews(View view){
        vNoteText = (TextView) view.findViewById(R.id.noteText);
        vDayDate =  (TextView) view.findViewById(R.id.dayDateText);
        vTimeText = (TextView) view.findViewById(R.id.timeText);
        vLocationName = (TextView) view.findViewById(R.id.locationName);
        vNoteEditText = (EditText) view.findViewById(R.id.noteEditText);
    }

    // Get Field Values
    private void queryData(){
        Log.v(TAG, "queryData _ID: " + noteID);
        Cursor mCursor = DataUtility.getNoteRecord(getActivity(), noteID);

        int indexText = mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NOTE_TEXT);
        int indexDate = mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_DATE_TEXT);
        int indexTime = mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_TIME_TEXT);
        int indexDay = mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_DAY_TEXT);
        int indexLocation = mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_LOCATION_NAME);
        int indexFolder = mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_FOLDER);

        if (mCursor != null) {
        /*
         * Moves to the next row in the cursor. Before the first movement in the cursor, the
         * "row pointer" is -1, and if you try to retrieve data at that position you will get an
         * exception.
         */
            while (mCursor.moveToNext()) {
                // Gets the value from the column.
                noteText = mCursor.getString(indexText);
                dateText = mCursor.getString(indexDate);
                timeText = mCursor.getString(indexTime);
                dayText = mCursor.getString(indexDay);
                locationName = mCursor.getString(indexLocation);
                folder = mCursor.getString(indexFolder);

                formattedTime = timeText;
                formattedDate = dayText + " - " + dateText;
                formattedLocation = locationName;
            }
        } else {
            // Insert code here to report an error if the cursor is null or the provider threw an exception.
            noteText = "";
            formattedTime = "";
            formattedDate = "";
            formattedLocation = "";
        }
        if(formattedTime == null){formattedTime = "";}
        if(formattedDate == null){formattedDate = "";}
        if(formattedLocation == null){formattedLocation = "";}

        setShareIntent(noteText);
    }


    // Method to set view text values to variables values
    private void setViewText(){
        vNoteText.setText(noteText);
        vNoteEditText.setText(noteText);
        vDayDate.setText(getString(R.string.label_date) + " " + formattedDate);
        vTimeText.setText(getString(R.string.label_time) + " " + formattedTime);
        vLocationName.setText(getString(R.string.label_location) + " " + formattedLocation);
    }

    // View switcher - 0 is read, 1 is edit
    public void viewSwitcher(Layout newLayout){
        if(newLayout == Layout.EDIT){
            // switch next
            ViewSwitcher switcher = (ViewSwitcher) view.findViewById(R.id.my_switcher);
            switcher.setDisplayedChild(1); // viewswitcher swaps textview for edittextview
            vNoteEditText.requestFocus(); // Focuses on edit text
            viewSwitcher = true;
            currentLayout = Layout.EDIT; // Sets variable to indicate which actions to show
        } else {
            // switch prev
            String modified_text = vNoteEditText.getText().toString(); // Get new values
            vNoteText.setText(modified_text); // Set read text to new value
            noteText = modified_text; // set value to new value for rotate
            // Update the content provider
            int numberUpdate = DataUtility.updateRecord(getActivity(), noteID, modified_text);
            String recordUpdateYes = getString(R.string.records_updated) + " " + numberUpdate;
            // Switch back to read only mode
            ViewSwitcher switcher = (ViewSwitcher) view.findViewById(R.id.my_switcher);
            switcher.setDisplayedChild(0); //or switcher.showPrevious();
            currentLayout = Layout.READ;
            viewSwitcher = false;
        }
    }

    // METHODS FOR BUTTON
    private void editRecord(){
        currentLayout = Layout.EDIT; // Sets variable to indicate which actions to show
        viewSwitcher(currentLayout);
        getActivity().invalidateOptionsMenu(); // Reset menu to display correct buttons
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0); // Show keyboard
    }

    private void saveRecord(){
        currentLayout = Layout.READ;
        viewSwitcher(currentLayout);
        getActivity().invalidateOptionsMenu();
        // Hide soft keyboard
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void trashRecord(){
        String trashFolder = this.getResources().getString(R.string.trash_note_folder);
        int numberUpdate = DataUtility.changeFolder(getActivity(), noteID, trashFolder);
        String recordUpdateYes = getString(R.string.records_trashed) + " " + numberUpdate;
        // Navigate to Detail View & Show snack bar on detail view
        mCallback.trashNotify(noteID);
    }

    private void restoreRecord(){
        String defaultFolder = this.getResources().getString(R.string.default_note_folder);
        int numberUpdate = DataUtility.changeFolder(getActivity(), noteID, defaultFolder);
        String recordUpdateYes = getString(R.string.records_restored) + " " + numberUpdate;
        currentLayout = Layout.READ;
        getActivity().invalidateOptionsMenu();
    }
}
