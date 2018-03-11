package org.asdnh.attendancescanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_realm);

    }

    @Override
    public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        //TODO: Edit this to also force a relog when the username or password is changed
        if(key.equals(SettingsActivity.KEY_PREF_REALM_URL) || key.equals(SettingsActivity.KEY_PREF_REALM_USERNAME)
                || key.equals(SettingsActivity.KEY_PREF_REALM_PASSWORD)) {

            Log.i("realm", "Logging user out");
            if(MainActivity.user != null) {

                MainActivity.user.logout();

            }

            if(key.equals(SettingsActivity.KEY_PREF_REALM_URL)) {

                Log.i("realm", "resetting instance address");

                RealmAddress.setInstanceAddress(sharedPreferences.getString(SettingsActivity.KEY_PREF_REALM_URL, ""));
            }

        }
    }

}
