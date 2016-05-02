package com.knewto.milknote;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * NoteListRecyclerViewAdapter
 * Adapter for Notes RecyclerView in NoteListFragment
 * - NoteListRecyclerViewAdapter: constructor set context, clickhandler, and emptyview.
 * - onCreateViewHolder: Inflates the list item
 * - onBindViewHolder: Maps the data to the list item
 * - getItemCount: Number of items, used to show empty view if zero
 * - swapCursor: Does a notify to swap cursor
 * - NoteAdapterOnClickHandler - interface: used by NoteListFragment to handle clikcks
 * - NoteAdapterViewHolder - maps viewholder to layout and creates onclick intent.
 */

public class NoteListRecyclerViewAdapter extends RecyclerView.Adapter<NoteListRecyclerViewAdapter.NoteAdapterViewHolder> {
    private static final String TAG = "NoteListAdapter";
    final private Context mContext;
    final private NoteAdapterOnClickHandler mClickHandler;
    final private View mEmptyView;
    private Cursor mCursor;

    public NoteListRecyclerViewAdapter(Context context, NoteAdapterOnClickHandler dh, View emptyView, int choiceMode) {
        Log.v(TAG, "Constructor");
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

        // Set bitmap
        final Resources res = mContext.getResources();
        final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

        final LetterTileProvider tileProvider = new LetterTileProvider(mContext);
        final Bitmap letterTile = tileProvider.getLetterTile(noteText, noteText, tileSize, tileSize);

        NoteAdapterViewHolder.mNoteImage.setImageBitmap(letterTile);

        // Find TextView and set note text on it
        NoteAdapterViewHolder.mNoteContent.setText(noteText);
    }

    @Override
    public int getItemCount() {
        Log.v(TAG, "Item Count");
        if (null == mCursor) return 0;
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        Log.v(TAG, "Swap Cursor");
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public static interface NoteAdapterOnClickHandler {
        void onClick(Long date, NoteAdapterViewHolder vh);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class NoteAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mNoteContent;
        public final ImageView mNoteImage;

        public NoteAdapterViewHolder(View view) {
            super(view);
            Log.v(TAG, "View Holder");
            mNoteContent = (TextView) view.findViewById(R.id.content);
            mNoteImage = (ImageView) view.findViewById(R.id.letter_image);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            String newNoteId =  mCursor.getString(NoteListFragment.COL_NOTE_ID);
            String newFolder = mCursor.getString(NoteListFragment.COL_FOLDER);

            // Call MainActivity method to update fragment or start Detail Activity
            if(mContext instanceof MainActivity){
                ((MainActivity)mContext).refreshDetailFragment(newNoteId, newFolder, 1);
            }

        }
    }
}