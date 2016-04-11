package com.knewto.milknote;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.NoteAdapterViewHolder> {
    private static final String TAG = "NoteListAdapter";
    
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;

    // Flag to determine if we want to use a separate view for "today".
    private boolean mUseTodayLayout = true;

    private Cursor mCursor;
    final private Context mContext;
    final private NoteAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;

    /**
     * Cache of the children views for a forecast list item.
     */
    public class NoteAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mNoteContent;

        public NoteAdapterViewHolder(View view) {
            super(view);
            Log.v(TAG, "View Holder");
            mNoteContent = (TextView) view.findViewById(R.id.content);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
//            String message = "Current Postion: " + adapterPosition;
//            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            Intent detailIntent = new Intent(mContext, DetailActivity.class);
            detailIntent.putExtra("ID", mCursor.getString(NoteListFragment.COL_NOTE_ID));
            detailIntent.putExtra("NoteText", mCursor.getString(NoteListFragment.COL_NOTE_TEXT));
            detailIntent.putExtra("DateText", mCursor.getString(NoteListFragment.COL_DATE_TEXT));
            detailIntent.putExtra("TimeText", mCursor.getString(NoteListFragment.COL_TIME_TEXT));
            detailIntent.putExtra("DayText", mCursor.getString(NoteListFragment.COL_DAY_TEXT));
            detailIntent.putExtra("RawTime", mCursor.getString(NoteListFragment.COL_RAW_TIME));
            detailIntent.putExtra("CoordLat", mCursor.getString(NoteListFragment.COL_LAT));
            detailIntent.putExtra("CoordLong", mCursor.getString(NoteListFragment.COL_LONG));
            detailIntent.putExtra("LocationName", mCursor.getString(NoteListFragment.COL_LOCATION_NAME));
            detailIntent.putExtra("Folder", mCursor.getString(NoteListFragment.COL_FOLDER));
            detailIntent.putExtra("Edited", mCursor.getString(NoteListFragment.COL_EDIT_FLAG));
            mContext.startActivity(detailIntent);

        }
    }

    public static interface NoteAdapterOnClickHandler {
        void onClick(Long date, NoteAdapterViewHolder vh);
    }

    public NoteListRecyclerViewAdapter(Context context, NoteAdapterOnClickHandler dh, View emptyView, int choiceMode) {
        Log.v(TAG, "Contructor");
        mContext = context;
        mClickHandler = dh;
        mEmptyView = emptyView;
    }

    @Override
    public NoteAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.v(TAG, "onCreateViewHolder");
        int layoutId = R.layout.fragment_item;
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
        view.setFocusable(true);
        return new NoteAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteAdapterViewHolder NoteAdapterViewHolder, int position) {
        Log.v(TAG, "onBindViewHolder");
        mCursor.moveToPosition(position);
        String noteText = mCursor.getString(NoteListFragment.COL_NOTE_TEXT);

        // Find TextView and set note text on it
        NoteAdapterViewHolder.mNoteContent.setText(noteText);
        }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        mICM.onRestoreInstanceState(savedInstanceState);
    }

    public void onSaveInstanceState(Bundle outState) {
  //      mICM.onSaveInstanceState(outState);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    public int getSelectedItemPosition() {
 //       return mICM.getSelectedItemPosition();
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        Log.v(TAG, "Item Count");
        if ( null == mCursor ) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        Log.v(TAG, "Swap Cursor");
        mCursor = newCursor;
        notifyDataSetChanged();
//        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public Cursor getCursor() {
        return mCursor;
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if ( viewHolder instanceof NoteAdapterViewHolder ) {
            NoteAdapterViewHolder vfh = (NoteAdapterViewHolder)viewHolder;
            vfh.onClick(vfh.itemView);
        }
    }
}