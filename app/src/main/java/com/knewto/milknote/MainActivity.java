package com.knewto.milknote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Code to run when Transcribe button is clicked
        final Button button = (Button) findViewById(R.id.button_id);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                doTranscribe();
            }
        });
    }

    private void doTranscribe(){
        Toast.makeText(getApplicationContext(), "Making Toast", Toast.LENGTH_SHORT).show();
    }

}
