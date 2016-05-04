package com.knewto.milknote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.knewto.milknote.DetailFragment.Layout;

/**
 * Detail Activity
 * displays details of transcribed note and allows editing and trashing of note
 * - onCreate: load saved instance or intent, insert fragment, create toolbar, load ad
 * - onCreateOptionsMenu: N/A - moved to fragment
 * - onOptionsItemSelected: N/A - moved to fragment
 * - onSaveInstanceState: store layout and noteid
 * - trashNotify: implement for DetailFragment, send user to MainActivity with trash flag
 */

public class DetailActivity extends AppCompatActivity implements DetailFragment.ParentActivityResponse {

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
        } else {
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    // Save setup variables on rotation
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("layout", currentLayout);
        outState.putString("noteID", noteID);
        super.onSaveInstanceState(outState);
    }

    public void trashNotify(String trashNoteId) {
        // Navigate to List View & Show snack bar on List view
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("trashFlag", 1);
        mainIntent.putExtra("recordId", trashNoteId);
        startActivity(mainIntent);
    }

}
