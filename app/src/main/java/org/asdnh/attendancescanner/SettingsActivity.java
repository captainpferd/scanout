package org.asdnh.attendancescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    //Keys for the realm preferences, these constants can be changed to avoid any runtime errors if the XML keys change
    public static final String KEY_PREF_REALM_URL = "pref_realm_url";
    public static final String KEY_PREF_REALM_USERNAME = "pref_realm_username";
    public static final String KEY_PREF_REALM_PASSWORD = "pref_realm_password";
    public static final String KEY_PREF_CAMERA_DIRECTION = "pref_camera_direction";
    public static final String KEY_PREF_BUTTONS = "pref_button_grid";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Add settings fragment
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    //Override on back pressed to force the main activity to be recreated when using device back button
    //This prevents the app from allowing one QR code scan with bad DB credentials
    @Override
    public void onBackPressed() {

        Log.i("back button", "Calling onBackPressed");
        //Start the main activity
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);

        //Finish the settings activity
        finish();
    }

    //Override the software back button to match the behavior of the hardware back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Check if the back button was pressed
        if(item.getItemId() == android.R.id.home) {

            onBackPressed();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

}
