//Fragment that holds the settings in the settings activity
package org.asdnh.attendancescanner;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

import static org.asdnh.attendancescanner.RealmAddress.getAuthUrl;
import static org.asdnh.attendancescanner.RealmAddress.getRealmBaseUrl;


public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    //Preferences for use when logging into realm
    private SharedPreferences sharedPref;

    //Realm instance variables
    private Realm database;
    private SyncUser user;
    private boolean loginGood;

    //Permissions request code for external storage writing
    private static final int MY_PERMISSIONS_REQUEST_EXPORT = 2;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Use XML file to get pre-defined preferences
        addPreferencesFromResource(R.xml.pref_all);

        //Get shared preferences for database login
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //Get preference button listener
        Preference button = findPreference(SettingsActivity.KEY_PREF_CSV);

        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                //Check for permission to write to storage
                //Request/check for permission to use the camera
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    //Request permission if not already granted
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_EXPORT);

                }

                //Permission must be granted now, proceed with the program
                loginAndExport();

                //Return true when task ends
                return true;

            }

        });

    }


    //Initialize and download realm instance
    public boolean loginRealm() {

        //Initialize Realm
        Log.i("realm", "Calling realm.init");
        Realm.init(getActivity());

        //Set realm URL
        RealmAddress.setInstanceAddress(sharedPref.getString(SettingsActivity.KEY_PREF_REALM_URL, ""));

        Log.i("realm", "Realm instance address is: " + RealmAddress.getInstanceAddress());


        Log.i("realm", "Starting to configure realm");

        //Check for a valid user
        user = SyncUser.currentUser();

        //If user is null (expired), log in again
        if(user == null) {

            Log.i("realm", "User is not valid");

            Log.i("realm", "username: " + sharedPref.getString(SettingsActivity.KEY_PREF_REALM_USERNAME, "") + ", password: " + sharedPref.getString(SettingsActivity.KEY_PREF_REALM_PASSWORD, ""));

            //Create credentials to log in
            final SyncCredentials myCredentials = SyncCredentials.usernamePassword(sharedPref.getString(SettingsActivity.KEY_PREF_REALM_USERNAME, "jr_sr_client"),
                    sharedPref.getString(SettingsActivity.KEY_PREF_REALM_PASSWORD, "asdrocks"),
                    false);

            //Log in in a new thread so the program can be paused during its execution
            Thread t = new Thread(new Runnable() {

                @Override
                public void run() {

                    //Log into realm only if the current user is expired
                    Log.i("realm", "entered async thread - attempting to log in");

                    //Attempt to log in, catch an exception of there is no internet
                    try {

                        user = SyncUser.login(myCredentials, getAuthUrl());

                        //Assign the user created to the class variable
                        Log.i("realm", "logged in");

                        //Login is valid
                        loginGood = true;

                        //Occurs when the user's credentials have expired and internet is not available
                    } catch (ObjectServerError error) {

                        //Print an error message to the log and quit the application of the realm cannot be retrieved
                        error.printStackTrace();

                        Log.i("realm", "Failed to log in to realm");

                        //Show a message explaining the issue
                        Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), R.string.internet_warning, Snackbar.LENGTH_INDEFINITE);
                        snackbar.show();

                        //Set valid login to false
                        loginGood = false;

                    }

                }


            });

            //Run thread and wait for results
            t.start();

            //Try to wait for results, catch an exception if thread is interrupted
            try {

                t.join();

            } catch (InterruptedException e) {

                //Login was unsuccessful
                loginGood = false;
                e.printStackTrace();

            }

        } else {

            //Login is good
            loginGood = true;

        }

        //Continue with app only if login is good
        if(loginGood) {

            Log.i("realm", "user is valid");

            //Build realm database configuration
            SyncConfiguration config = new SyncConfiguration.Builder(user, getRealmBaseUrl() + "/~/log")
                    .disableSSLVerification()
                    .build();

            // Get the realm instance
            database = Realm.getInstance(config);
            Log.i("realm", "realm retrieved");

            return true;

        } else {

            Log.i("realm", "Login failed");
            return false;

        }
    }


    //Method to export the realm database contents to a CSV file
    public boolean exportToCSV() {

        Log.i("realm", "Starting CSV Export");

        //Base directory of the device
        String baseDirectory = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

        //Timestamp to append to the file
        LocalDateTime timestamp = LocalDateTime.now();

        //Format of the timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss MM-dd-yyyy");

        //Filename
        String fileName = "Realm_CSV_Export - " + timestamp.format(formatter) + ".csv";

        //Path to save the file at (in a folder called Realm CSV at the base directory of the device)
        String filePath = baseDirectory + File.separator + "Realm CSV Exports" + File.separator + fileName;

        File file = new File(filePath);

        //Make the directory if it does not exist
        file.getParentFile().mkdirs();

        //Initialize CSV writer
        CSVWriter writer;

        Log.i("realm", "Checking file");
        //Check if everything was created correctly
        if (file.exists() && !file.isDirectory()) {

            try {

                //Create CSV writer from file
                FileWriter fileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(fileWriter);

                Log.i("realm", "File already exists, will be added to");

            } catch (IOException e) {

                e.printStackTrace();

                return false;
            }

        } else {

            try {

                //Create writer without true parameter??
                writer = new CSVWriter(new FileWriter(filePath));

                Log.i("realm", "File does not exist, creating it");

            } catch (IOException e) {

                e.printStackTrace();

                return false;
            }

        }

        //Query to get list of strings

        RealmResults<Student> students = database.where(Student.class)
                .findAll();

        //Write the list of strings
        List<String[]> studentStrings = new ArrayList<>();

        //Add headers in spreadsheet
        studentStrings.add(new String[]{"Date", "Name", "Time Out", "Destination", "Time In"});

        //Iterate through all the students in the database
        for (Student s : students) {

            //Temporarily store the time in to check if it is null
            String timeIn = s.timeIn;

            //If the student has not signed in yet, timeIn will be null
            if (timeIn == null) {

                timeIn = "Not signed in at time of export";

            }

            //Create a temporary array of the student info
            String[] temp = {s.date, s.name, s.timeOut, s.destination, timeIn};

            //Add each student to the array of students
            studentStrings.add(temp);
        }

        try {

            //Write everything from the student-string array to a CSV
            writer.writeAll(studentStrings);

        } catch (NullPointerException e) {

            e.printStackTrace();
            return false;
        }

        //Close the writer
        try {

            writer.close();

        } catch (IOException | NullPointerException e) {

            e.printStackTrace();
            return false;

        }

        //Return true unless an exception is thrown
        return true;

    }


    //Call loginRealm and then exportToCSV to allow the permissions callback method to restart the task
    public void loginAndExport() {

        if(loginRealm()) {

            if (exportToCSV()) {

                Toast toast = Toast.makeText(getActivity(), "Export Successful", Toast.LENGTH_SHORT);
                toast.show();

                database.close();
                Log.i("realm", "Database closed");

            } else {

                Toast toast = Toast.makeText(getActivity(), "Export Failed", Toast.LENGTH_LONG);
                toast.show();

                database.close();
                Log.i("realm", "Database closed");

            }

        } else {

            Toast toast = Toast.makeText(getActivity(), "Database credentials invalid", Toast.LENGTH_SHORT);
            toast.show();

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @Nonnull String permissions[], @Nonnull int[] grantResults) {

        //Switch statement to check for each permission requested
        switch(requestCode) {

            //Camera request
            case MY_PERMISSIONS_REQUEST_EXPORT: {
                //If request was cancelled, the array is empty
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    loginAndExport();

                } else {

                    //Permission denied, tell the user the app will not work without it
                    Toast toast = Toast.makeText(getActivity(), "External storage permission required to export", Toast.LENGTH_SHORT);
                    toast.show();

                }

            }
        }
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