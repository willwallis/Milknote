package com.knewto.milknote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("NoteText")) {
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

        TextView vNoteText = (TextView) findViewById(R.id.noteText);
        TextView vDateText = (TextView) findViewById(R.id.dateText);
        TextView vTimeText = (TextView) findViewById(R.id.timeText);
        TextView vDayText = (TextView) findViewById(R.id.dayText);
        TextView vRawTime = (TextView) findViewById(R.id.rawTime);
        TextView vCoordLat = (TextView) findViewById(R.id.lat_coord);
        TextView vCoordLong = (TextView) findViewById(R.id.long_coord);
        TextView vLocationName = (TextView) findViewById(R.id.locationName);
        TextView vFolder = (TextView) findViewById(R.id.folder);
        TextView vEdited = (TextView) findViewById(R.id.editBox);

        vNoteText.setText(noteText);
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
}
