package com.knewto.milknote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

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

        // Button listeners
        Button editButton = (Button) findViewById(R.id.button_edit);
        editButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Edit", Toast.LENGTH_SHORT).show();
                ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher);
                switcher.showNext(); //or switcher.showPrevious();
            }
        });
        Button saveButton = (Button) findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String modified_text = vNoteEditText.getText().toString();
                vNoteText.setText(modified_text);
                noteText = modified_text;
                int numberUpdate = DataUtility.updateRecord(getApplicationContext(), noteID, modified_text);
                String recordUpdateYes = "Records updated: " + numberUpdate;
                Toast.makeText(getApplicationContext(), recordUpdateYes, Toast.LENGTH_SHORT).show();
                ViewSwitcher switcher = (ViewSwitcher) findViewById(R.id.my_switcher);
                switcher.showPrevious(); //or switcher.showPrevious();
            }
        });
        Button trashButton = (Button) findViewById(R.id.button_trash);
        trashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numberUpdate = DataUtility.trashRecord(getApplicationContext(), noteID);
                String recordUpdateYes = "Records trashed: " + numberUpdate;
                Toast.makeText(getApplicationContext(), recordUpdateYes, Toast.LENGTH_SHORT).show();
            }
        });
        Button deleteButton = (Button) findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int numberUpdate = DataUtility.deleteRecord(getApplicationContext(), noteID);
                String recordUpdateYes = "Records deleted: " + numberUpdate;
                Toast.makeText(getApplicationContext(), recordUpdateYes, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
