package org.asdnh.attendancescanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ViewFlipper;

public class DestinationActivity extends AppCompatActivity {

    //Intent to hand back destination
    Intent intent;

    //Settings for the buttons
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Default configuration
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the flipper to change button appearance
        ViewFlipper vf = findViewById(R.id.viewFlipper);

        //Get saved settings
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //Set activity title to student name
        Intent studentName = getIntent();

        //Create a new intent to hand back information to the main activity in
        intent = new Intent();

        //Get the name from the intent passed in, and put it in the new one
        String name = studentName.getStringExtra("name");

        intent.putExtra("name", name);

        //Set the title of the activity to the student name
        try {

            //Set activity title to student name
            this.setTitle(name);

        } catch (NullPointerException e) {

            //This should never happen
            e.printStackTrace();
        }

        if(false) {

            vf.showNext();

            //Create bathroom button
            Button bathroomButton = findViewById(R.id.gridbathroom_button);

            //Create click listener
            bathroomButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Bathroom");
                    setResult(RESULT_OK, intent);
                    finish();

                }
            });


            //Create locker button
            Button lockerButton = findViewById(R.id.gridlocker_button);

            //Create click listener
            lockerButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Locker");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });


            //Create office button
            Button officeButton = findViewById(R.id.gridoffice_button);

            //Create click listener
            officeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Office");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });


            //Create nurse button
            Button nurseButton = findViewById(R.id.gridnurse_button);

            //Create click listener
            nurseButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Nurse");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });


            //Create 'other' button
            Button otherButton = findViewById(R.id.gridother_button);

            //Create click listener
            otherButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Other");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });


            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        } else {

            //Create bathroom button
            Button bathroomButton = findViewById(R.id.bathroom_button);

            //Create click listener
            bathroomButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Bathroom");
                    setResult(RESULT_OK, intent);
                    finish();

                }
            });


            //Create locker button
            Button lockerButton = findViewById(R.id.locker_button);

            //Create click listener
            lockerButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Locker");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });


            //Create office button
            Button officeButton = findViewById(R.id.office_button);

            //Create click listener
            officeButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Office");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });


            //Create nurse button
            Button nurseButton = findViewById(R.id.nurse_button);

            //Create click listener
            nurseButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Nurse");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });


            //Create 'other' button
            Button otherButton = findViewById(R.id.other_button);

            //Create click listener
            otherButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    intent.putExtra("destination", "Other");
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });


            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}
