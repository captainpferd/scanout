package org.asdnh.attendancescanner;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_REALM_URL = "pref_realm_url";
    public static final String KEY_PREF_REALM_USERNAME = "pref_realm_username";
    public static final String KEY_PREF_REALM_PASSWORD = "pref_realm_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Add fragment
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

}
