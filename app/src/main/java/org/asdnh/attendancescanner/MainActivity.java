package org.asdnh.attendancescanner;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.Duration;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.annotation.Nonnull;

import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;

import static org.asdnh.attendancescanner.RealmAddress.getAuthUrl;
import static org.asdnh.attendancescanner.RealmAddress.getRealmBaseUrl;

public class MainActivity extends AppCompatActivity {

    //QR code scanner resources
    private CameraSource cameraPreview;
    private BarcodeDetector barcodeDetector;

    //Views for text and the camera feed
    private SurfaceView cameraStream;
    private TextView qrCodeContents;
    private CoordinatorLayout coordinatorLayout;

    //Realm resources
    protected Realm database;
    protected static SyncUser user;
    boolean loginGood;
    protected SharedPreferences sharedPref;

    //Permissions request
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;

    //Request code for destination activity
    private final int DESTINATION_REQUEST = 1;

    //Create a progress bar to show realm loading progress
    protected ProgressBar realmProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Default configuration
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize date/time library
        AndroidThreeTen.init(this);

        //Initialize and show the loading bar
        realmProgress = findViewById(R.id.realmLoadingBar);

        //Set defaults
        PreferenceManager.setDefaultValues(this, R.xml.pref_all, false);

        //Get shared preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        //Assign the surface view to camera stream
        cameraStream = findViewById(R.id.surfaceView);

        //Assign the qr code value to textview
        qrCodeContents = findViewById(R.id.codeContents);

        //Get the coordinator layout
        coordinatorLayout = findViewById(R.id.coordinator_layout);

        //Request/check for permission to use the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            //Request permission if not already granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);


            //Permission is granted, proceed with the program
        } else {

            //Show progress bar
            Log.i("realm", "Showing progress bar");
            realmProgress.setVisibility(ProgressBar.VISIBLE);

            //Log into realm and get the instance, setting loginGood variable appropriately
            loginRealm();

            Log.i("realm", "Past login function, loginGood = " + loginGood);

            //Continue with app only if login is good
            if (loginGood) {

                Log.i("realm", "user is valid");

                //Build realm database configuration
                SyncConfiguration config = new SyncConfiguration.Builder(user, getRealmBaseUrl() + "/~/log")
                        .disableSSLVerification()
                        .build();

                // Get the realm instance
                database = Realm.getInstance(config);
                Log.i("realm", "realm retrieved");

                Log.i("realm", "Realm Path: " + database.getPath());

                Log.i("realm", "Hiding progress bar");
                realmProgress.setVisibility(ProgressBar.INVISIBLE);

                //Start scanning QR codes
                startQRCodeScanner();

            }

        }


    }


    //Method to log in to and download the realm database
    public void loginRealm() {

        //Initialize Realm
        Log.i("realm", "Calling realm.init");
        Realm.init(this);

        //Set realm URL
        RealmAddress.setInstanceAddress(sharedPref.getString(SettingsActivity.KEY_PREF_REALM_URL, ""));

        Log.i("realm", "Realm instance address is: " + RealmAddress.getInstanceAddress());

        Log.i("realm", "Starting to configure realm");

        //Check for a valid user
        user = SyncUser.currentUser();

        //If user is null (expired), log in again
        if (user == null) {

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
                    Log.i("realm", "entered async thread in main activity - attempting to log in");

                    //Attempt to log in, catch an exception of there is no internet
                    try {


                        user = SyncUser.login(myCredentials, getAuthUrl());

                        //Assign the user created to the class variable
                        Log.i("realm", "logged in");

                        //Login is valid
                        loginGood = true;

                        //Recreate the main activity (must be run on the UI thread)
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                //Recreate main activity after the login is good
                                Log.i("realm", "Recreating activity");
                                recreate();

                            }
                        });

                        //Occurs when the user's credentials have expired and internet is not available
                        //Illegal argument happens when the URL is in a bad format
                    } catch (ObjectServerError | IllegalArgumentException error) {

                        //Print an error message to the log and quit the application of the realm cannot be retrieved
                        error.printStackTrace();

                        Log.i("realm", "Failed to log in to realm");

                        //Show a message explaining the issue
                        Snackbar snackbar = Snackbar.make(coordinatorLayout, R.string.internet_warning, Snackbar.LENGTH_INDEFINITE);
                        snackbar.show();

                        //Set valid login to false
                        loginGood = false;

                    }

                }


            });

            //Run thread and wait for results
            t.start();

            //Try to wait for results, catch an exception if thread is interrupted
            /*try {

                Log.i("realm", "Calling t.join");
                t.join();
                Log.i("realm", "Past t.join");

            } catch (InterruptedException e) {

                //Login was unsuccessful
                loginGood = false;
                e.printStackTrace();

            } */


        } else {

            //Login is good
            loginGood = true;

        }

    }


    //Method to handle result of permissions request
    @Override
    public void onRequestPermissionsResult(int requestCode, @Nonnull String permissions[], @Nonnull int[] grantResults) {

        //Switch statement to check for each permission requested
        switch (requestCode) {

            //Camera request
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                //If request was cancelled, the array is empty
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission granted, recreate activity
                    recreate();

                } else {

                    //Permission denied, tell the user the app will not work without it
                    qrCodeContents.setText(getString(R.string.camera_denied));
                }

            }
        }
    }


    //Automatically generated
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }


    //Automatically generated
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    //Display camera and scan for QR codes in method
    public void startQRCodeScanner() {

        Log.i("realm", "Start QR scanner");

        //Barcode Detector to receive images from the camera and check for QR codes
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        //Store variable representing camera direction
        int cameraDirection = CameraSource.CAMERA_FACING_BACK;

        //Get camera direction from shared preferences, default to back
        if (sharedPref.getBoolean(SettingsActivity.KEY_PREF_CAMERA_DIRECTION, true)) {

            cameraDirection = CameraSource.CAMERA_FACING_FRONT;

        }

        //Camera object to show a preview in the app as well as for use by the barcode detector
        cameraPreview = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(cameraDirection)
                .build();

        //Attempt to begin streaming the camera preview to the SurfaceView
        cameraStream.getHolder().addCallback(new SurfaceHolder.Callback() {


            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                //In reality, if this point in the program has been reached
                //camera permission was granted, so an exception should never be thrown
                //nonetheless, the compiler thinks otherwise

                //Start the camera preview, catching a security exception if there is not permission to use the camera
                try {

                    cameraPreview.start(cameraStream.getHolder());

                } catch (IOException | SecurityException ex) {

                    ex.printStackTrace();
                    Log.e(getString(R.string.perm_error), ex.toString());

                }
            }


            //Automatically generated
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }


            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                //Stop the camera
                cameraPreview.stop();

            }

        });


        //Process QR codes
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            //Automatically generated
            @Override
            public void release() {

            }


            //Get detected QR codes
            @Override
            public void receiveDetections(Detector.Detections detections) {

                //Store found codes
                final SparseArray<?> barcodes = detections.getDetectedItems();

                //If any codes are found, proceed with the log entry
                if (barcodes.size() != 0) {

                    //Stop the camera to prevent multiple scans
                    Log.i("realm", "releasing barcode detector");
                    barcodeDetector.release();

                    //New task to access the Realm and text view from the UI thread
                    qrCodeContents.post(new Runnable() {

                        //Code to run in the task
                        @Override
                        public void run() {

                            //Cast contents of scanned code as a barcode-type object
                            Barcode b = (Barcode) barcodes.valueAt(0);

                            final String studentName = b.displayValue;

                            //Check for a fake name
                            if (studentName.contains("Liam")) {
                                Toast toast = Toast.makeText(getApplicationContext(), "Hi Liam", Toast.LENGTH_LONG);
                                toast.show();
                                recreate();

                            } else if (!studentName.contains("Student")) {

                                Toast toast = Toast.makeText(getApplicationContext(), "Nice try", Toast.LENGTH_LONG);
                                toast.show();
                                recreate();

                            } else {

                                signInOut(studentName);

                            }
                        }


                    });

                }
            }


        });

    }


    //Function to handle the sign in/sign out process
    public void signInOut(String studentName) {

        /* Construct the realm query
         * Find entries in the database where the student name matches the QR code
         * and timeIn is null (no sign in time)
         * If none are found, object returned is null
         * Otherwise, a live reference to that entry in the database is passed
         */

        Log.i("realm", "Checking to see if the student is already signed out");
        RealmQuery<Student> query = database.where(Student.class);

        query.equalTo("name", studentName);

        query.isNull("timeIn");

        //Execute the query
        Student tempStudent = query.findFirst();

        //If the query returns null, create a new student object to sign out
        if (tempStudent == null) {

            //Start destination activity to get destination as result
            Intent getDestinationIntent = new Intent(getApplicationContext(), DestinationActivity.class);

            //Add the student name to set the title of the destination activity
            getDestinationIntent.putExtra("name", studentName);

            //Start the destination activity and call onActivityResult when it finishes
            Log.i("realm", "Starting destination activity");
            startActivityForResult(getDestinationIntent, DESTINATION_REQUEST);


            //If an object was found by the query, then this student needs to sign in
        } else {

            //Get time difference between sign out and present
            long timeDifference = compareTimes(tempStudent.date, tempStudent.timeOut);
            Log.i("realm", "Difference in time: " + timeDifference);

            //Start a transaction so changes to the object are reflected in the database
            Log.i("realm", "Temp student not null, checking time difference");
            database.beginTransaction();

            //Check to see if this sign in is close enough to the departure they probably misclicked
            if (timeDifference <= 10) {

                Log.i("realm", "Time difference is < 10 seconds, assuming an accident");
                //Remove the student object from realm
                tempStudent.deleteFromRealm();

                Toast toast = Toast.makeText(getApplicationContext(), "Looks like you mis-clicked? Removing that entry from the log.", Toast.LENGTH_SHORT);
                toast.show();

                database.commitTransaction();

            } else {

                Log.i("realm", "Time difference is > 20 seconds, assigning timeIn");

                //Check if the person was gone for over 2 hours
                if (timeDifference > 7200) {

                    //Calculate time gone in hours, rounded down
                    int hoursGone = (int) timeDifference / 3600;

                    //Show a message informing them that they may have forgotten to sign in
                    Toast toast = Toast.makeText(getApplicationContext(), "Looks like you forgot to sign in earlier? You were gone for around " + hoursGone + " hours", Toast.LENGTH_LONG);
                    toast.show();
                }

                if (timeDifference > 43200) {

                    tempStudent.timeIn = getString(R.string.missed_sign_in) + ": " + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).format(new Date());

                } else {

                    //Set the time in to the current time
                    tempStudent.timeIn = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());

                }
                //Finish the transaction
                database.commitTransaction();

                //Set a welcome back message so the student knows they have been signed in
                Toast toast = Toast.makeText(getApplicationContext(), "Welcome back, " + studentName, Toast.LENGTH_LONG);
                toast.show();

            }

            //The barcode detector was released to stop duplicate scans, so refresh the activity to rebuild it
            recreate();

        }

    }


    @Override
    protected void onDestroy() {

        //Release resources
        super.onDestroy();

        //Try to release resouces, if they are null they may throw an exception
        try {
            cameraPreview.release();

            barcodeDetector.release();

            //Close the realm database
            database.close();

        } catch (NullPointerException e) {
            //Do nothing if those resources are null
        }

    }


    //Method to create a new student object to sign out after they pick their destination
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent getDestinationIntent) {

        Log.i("realm", "Made it back to onActivityResult");
        super.onActivityResult(requestCode, resultCode, getDestinationIntent);

        //If this method was called after the destination request, then sign the student out if there was no error
        if (requestCode == DESTINATION_REQUEST) {

            //Check to make sure the destination activity worked
            if (resultCode == RESULT_OK) {

                //Set destination and student name from the intent returned
                final String destination = getDestinationIntent.getStringExtra("destination");

                final String studentName = getDestinationIntent.getStringExtra("name");

                Log.i("realm", "This is the destination received" + destination);

                //Execute a transaction to the database to store the new object
                database.executeTransaction(new Realm.Transaction() {

                    @Override
                    public void execute(@Nonnull Realm database) {

                        //Create a new student object
                        Log.i("realm", "Creating student object");
                        Student student = database.createObject(Student.class);

                        //Store the date in the date field
                        student.date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());

                        //Store the time in the time field
                        student.timeOut = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date());

                        //Store the name in the name field using the name returned from the destination activity
                        student.name = studentName;

                        //Store the destination returned by the destination activity
                        student.destination = destination;

                        Log.i("realm", "added all properties to object - Object is fully committed");

                    }


                });

            }
        }

        Log.i("realm", "Calling QR scan to rebuild detector");

        //Restart the QR code scanner
        startQRCodeScanner();

        Log.i("realm", "Leaving onActivityResult");

    }


    //Method to compare a date and time passed to the current date and time
    public static long compareTimes(String date1, String time1) {

        //Format the dates and times will be in
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");

        //Store dates and times in an object that can be compared
        LocalDateTime dateTime1 = LocalDateTime.parse(date1 + " " + time1, formatter);

        LocalDateTime now = LocalDateTime.now();

        //Return the difference in seconds between the two times
        return (Duration.between(dateTime1, now).toMillis() / 1000);

    }


}