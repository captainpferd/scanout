package org.asdnh.attendancescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class DestinationActivity extends AppCompatActivity {

    //Intent to hand back destination
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set activity title to student name
        Intent studentName = getIntent();

        intent = new Intent();

        String name = studentName.getStringExtra("name");

        intent.putExtra("name", name);


        try {
            this.setTitle(name);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //Set up button click listeners

        Button bathroomButton = findViewById(R.id.bathroom_button);
        bathroomButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO: Return "bathroom" as destination
                intent.putExtra("destination", "Bathroom");
                setResult(RESULT_OK, intent);
                finish();

            }
        });

        Button lockerButton = findViewById(R.id.locker_button);
        lockerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO: Return "locker" as destination
                intent.putExtra("destination", "Locker");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button officeButton = findViewById(R.id.office_button);
        officeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO: Return "office" as destination
                intent.putExtra("destination", "Office");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button nurseButton = findViewById(R.id.nurse_button);
        nurseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO: Return "nurse" as destination
                intent.putExtra("destination", "Nurse");
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        Button otherButton = findViewById(R.id.other_button);
        otherButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //TODO: Return "other" as destination
                intent.putExtra("destination", "Other");
                setResult(RESULT_OK, intent);
                finish();
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
