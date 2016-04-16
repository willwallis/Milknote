package com.knewto.milknote;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.knewto.milknote.data.NoteContract;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link //OnListFragmentInteractionListener}
 * interface.
 */
public class NoteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final String TAG = "NoteListFragment";

    // REPLACE WITH DYNAMIC VALUE
    private String currentFolder;

    // Recycler view variables
    private NoteListRecyclerViewAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private Cursor emptyCursor;

    // Cursor Loader Variables
    private static final int LOADER_UNIQUE_ID = 0;
    private static final String[] NOTE_COLUMNS = {
            NoteContract.NoteEntry._ID,
            NoteContract.NoteEntry.COLUMN_NOTE_TEXT,
            NoteContract.NoteEntry.COLUMN_DATE_TEXT,
            NoteContract.NoteEntry.COLUMN_TIME_TEXT,
            NoteContract.NoteEntry.COLUMN_DAY_TEXT,
            NoteContract.NoteEntry.COLUMN_RAW_TIME,
            NoteContract.NoteEntry.COLUMN_LAT,
            NoteContract.NoteEntry.COLUMN_LONG,
            NoteContract.NoteEntry.COLUMN_LOCATION_NAME,
            NoteContract.NoteEntry.COLUMN_FOLDER,
            NoteContract.NoteEntry.COLUMN_EDIT_FLAG
    };

    // These indices are tied to NOTE_COLUMNS.  If NOTE_COLUMNS changes, these must change.
    static final int COL_NOTE_ID = 0;
    static final int COL_NOTE_TEXT = 1;
    static final int COL_DATE_TEXT = 2;
    static final int COL_TIME_TEXT = 3;
    static final int COL_DAY_TEXT = 4;
    static final int COL_RAW_TIME = 5;
    static final int COL_LAT = 6;
    static final int COL_LONG = 7;
    static final int COL_LOCATION_NAME = 8;
    static final int COL_FOLDER = 9;
    static final int COL_EDIT_FLAG = 10;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NoteListFragment() {
        // Allow sending folder from Activity
        this.setArguments(new Bundle());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // 1. initiate loader. Variables: loader_id, arguments, LoaderManager.LoaderCallbacks implementation
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(LOADER_UNIQUE_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);

        // Get folder from bundle
        Bundle data = this.getArguments().getBundle("folder_data");
        currentFolder = data.getString("folder", getResources().getString(R.string.default_note_folder));
        Log.v(TAG,"currentFolder is: " + currentFolder);

        //MainActivity activity = (MainActivity) getActivity();
        //currentFolder = activity.folderName;

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // ADD AN EMPTY VIEW
        // View emptyView = rootView.findViewById(R.id.recyclerview_forecast_empty);

        Cursor emptyCursor = getActivity().getContentResolver().query(
                NoteContract.NoteEntry.CONTENT_URI,
                NOTE_COLUMNS,
                null,
                null,
                null);

        View emptyView = view.findViewById(R.id.notelist_fragment);
        int mChoiceMode = 0;
        // specify an adapter (see also next example)
        mAdapter = new NoteListRecyclerViewAdapter(getActivity(), new NoteListRecyclerViewAdapter.NoteAdapterOnClickHandler() {
            @Override
            public void onClick(Long date, NoteListRecyclerViewAdapter.NoteAdapterViewHolder vh) {
//                String locationSetting = Utility.getPreferredLocation(getActivity());
//                ((Callback) getActivity())
//                        .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                        locationSetting, date),
//                                vh
//                        );
            }
        }, emptyView, mChoiceMode);

        //mAdapter = new NoteListRecyclerViewAdapter(emptyCursor);
        mRecyclerView.setAdapter(mAdapter);


        return view;
    }


    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri, NoteListRecyclerViewAdapter.NoteAdapterViewHolder vh);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnListFragmentInteractionListener) {
//            mListener = (OnListFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnListFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    // 2. Create and load a new cursor with Note data
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = NoteContract.NoteEntry._ID + " DESC";
        Uri noteInFolderURI = NoteContract.NoteEntry.buildNoteWithFolder(currentFolder);

        return new CursorLoader(getActivity(),
                noteInFolderURI,
                NOTE_COLUMNS,
                null,
                null,
                sortOrder);
    }

    // 3. Called when a loader has finished
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
    }

    // 4. Called when loader is no longer being used
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
    }

    public void loadFolder(String newFolder){
        // refresh the query
        currentFolder = newFolder;
        getLoaderManager().restartLoader(LOADER_UNIQUE_ID, null, this);
    }

}
