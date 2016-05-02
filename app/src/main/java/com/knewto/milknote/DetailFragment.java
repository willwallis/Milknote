package com.knewto.milknote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.knewto.milknote.data.NoteContract;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@lin DetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
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

    // Fied values
    String noteID;
    String noteText = "";
    String noteEditText;
    String dateText = "";
    String timeText = "";
    String dayText = "";
    String locationName = "";
    String folder = "";
    String formattedTime;
    String formattedDate;
    String formattedLocation;

    // Layout variables
    private Layout currentLayout;
    public enum Layout {
        READ,
        EDIT,
        RESTORE
    }
    boolean viewSwitcher = false;
    private ShareActionProvider mShareActionProvider;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
        // Allow sending values from Activity
        this.setArguments(new Bundle());
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String param1, String param2) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Check whether we're recreating a previously destroyed instance
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            noteEditText = savedInstanceState.getString("noteEditText");
            currentLayout = (Layout)savedInstanceState.get("layout");
            noteID = savedInstanceState.getString("noteID");
        }
        Log.v(TAG, "onCreate _ID: " + noteID);

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

        // Get values from bundle
        Bundle data = this.getArguments().getBundle("note_data");
        if(noteID == null){
            noteID = data.getString("noteID", "");}
        if(currentLayout == null){
            currentLayout = (Layout)data.get("layout");}
        queryData();

        // Set to edit mode if Layout is edit
        if(currentLayout == Layout.EDIT){
            viewSwitcher(1);
        }

        // Set Values
        setViewText();

        return view;
    }

    public void loadNote(String newNoteId){
        Log.v(TAG, "loadNote _ID: " + newNoteId);
        noteID = newNoteId;
        queryData();
        setViewText();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        switch(currentLayout){
            case READ:
                inflater.inflate(R.menu.menu_detail_share, menu);
                // Locate MenuItem with ShareActionProvider
                MenuItem item = menu.findItem(R.id.menu_item_share);
                // Fetch and store ShareActionProvider
                mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
                setShareIntent(noteText);
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
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
        vDayDate.setText(getString(R.string.label_date) +  " " + formattedDate);
        vTimeText.setText(getString(R.string.label_time) +  " " + formattedTime);
        vLocationName.setText(getString(R.string.label_location) + " " + formattedLocation);
    }

    // View switcher - 0 is read, 1 is edit
    public void viewSwitcher(int layoutFlag){
        if(layoutFlag == 1){
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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
