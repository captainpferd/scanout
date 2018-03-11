package org.asdnh.attendancescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    //Keys for the realm preferences, these constants can be changed to avoid any runtime errors if the XML keys change
    public static final String KEY_PREF_REALM_URL = "pref_realm_url";
    public static final String KEY_PREF_REALM_USERNAME = "pref_realm_username";
    public static final String KEY_PREF_REALM_PASSWORD = "pref_realm_password";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Add settings fragment
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    //Override on back pressed to force the main activity to be recreated when using device back button
    //This prevents the app from scanning once with invalid credentials
    @Override
    public void onBackPressed() {

        //Start the main activity
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);

        //Finish the settings activity
        finish();
    }

}
