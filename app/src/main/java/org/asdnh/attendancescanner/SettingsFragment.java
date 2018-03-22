//Fragment that holds the settings in the settings activity
package org.asdnh.attendancescanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Use XML file to get pre-defined preferences
        addPreferencesFromResource(R.xml.pref_all);

        //Get preference button listener
        Preference button = findPreference(SettingsActivity.KEY_PREF_CSV);

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                //Export the data in the realm database to a csv when pressed

                /*boolean successful = MainActivity.exportToCSV();

                if(successful) {
                    Toast toast = Toast.makeText(getActivity(), "CSV Export Successful", Toast.LENGTH_SHORT);
                    toast.show();
                    return true;

                } else {
                    Toast toast = Toast.makeText(getActivity(), "CSV Export Failed", Toast.LENGTH_SHORT);
                    toast.show();
                    return false;

                }*/

                Toast toast = Toast.makeText(getActivity(), "Work in Progress", Toast.LENGTH_SHORT);
                toast.show();
                return true;

            }

        });

    }

    @Override
    public void onResume() {
        super.onResume();

        //Register preference change listener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        //Unregister the listener
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        //If any realm information is changed, log the current user out to force a re-log
        if(key.equals(SettingsActivity.KEY_PREF_REALM_URL) || key.equals(SettingsActivity.KEY_PREF_REALM_USERNAME)
                || key.equals(SettingsActivity.KEY_PREF_REALM_PASSWORD)) {

            //Log the user out if they are still valid
            Log.i("realm", "Logging user out");
            if(MainActivity.user != null) {

                MainActivity.user.logout();

            }

            //If the URL was changed, then set the instance address to the new URL
            if(key.equals(SettingsActivity.KEY_PREF_REALM_URL)) {

                Log.i("realm", "resetting instance address");

                //Set the new URL
                RealmAddress.setInstanceAddress(sharedPreferences.getString(SettingsActivity.KEY_PREF_REALM_URL, ""));
            }

        }

    }


}