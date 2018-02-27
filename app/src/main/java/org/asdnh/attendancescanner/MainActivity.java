package org.asdnh.attendancescanner;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.*;

import static org.asdnh.attendancescanner.Constants.*;
import org.asdnh.attendancescanner.Student;

public class MainActivity extends AppCompatActivity {

    //TODO: Fix variable names/comment for labeling
    //Global Variables??
    private CameraSource cameraPreview;
    private BarcodeDetector barcodeDetector;
    private SurfaceView cameraStream;
    private TextView qrCodeContents;
    private Realm database;
    private SyncCredentials myCredentials;
    private SyncUser user;
    private SyncConfiguration config;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    private final int DESTINATION_REQUEST = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Unnecessary Button - was here by default

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        //Assign the surface view to camera stream
        cameraStream = findViewById(R.id.surfaceView);

        //Assign the qr code value to textview
        qrCodeContents = findViewById(R.id.codeContents);

        //Request/check for permission to use the camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            //Request permission if not already granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);

        } else {

            Log.i("realm", "Calling realm.init");
            Realm.init(this);

            Log.i("realm", "Starting to configure realm");

            //TODO: Add Realm Database
            myCredentials = SyncCredentials.usernamePassword("jr_sr_client", "asdrocks", false);

            //Run the network tasks in a separate thread
            Thread t = new Thread(new Runnable() {
                public void run() {

                    Log.i("realm", "entered async thread - attempting to log in");
                    user = SyncUser.login(myCredentials, AUTH_URL);

                    Log.i("realm", "logged in");
                    config = new SyncConfiguration.Builder(user, REALM_BASE_URL + "/~/log")
                            .disableSSLVerification()
                            .build();

                    Log.i("realm", "config built - leaving async");

                }

            });

            //Run the new thread
            t.start();

            //Wait for the thread to finish
            try {
                t.join();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Get the realm instance
            database = Realm.getInstance(config);
            Log.i("realm", "realm retrieved");

            //Permission granted, start the QR scanner
            startQRCodeScanner();
        }

    }

    /* Method to handle result of permissions request
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        //Switch statement to check for each permission requested
        switch(requestCode) {

            //Camera request
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                //If request was cancelled, the array is empty
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission granted
                    startQRCodeScanner();

                } else {

                    //Permission denied, tell the user the app will not work without it
                    qrCodeContents.setText(getString(R.string.camera_denied));
                }

                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Display camera and scan for QR codes in method
    public void startQRCodeScanner() {

        Log.i("realm", "Start QR scanner running");
        //Barcode Detector to receive images from the camera and check for QR codes
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        //TODO: Deal with preview size
        //Camera object to show a preview in the app as well as for use by the barcode detector
        cameraPreview = new CameraSource.Builder(this, barcodeDetector)
                //.setRequestedPreviewSize(500, 300)
                .setAutoFocusEnabled(true)
                .build();

        //Attempt to begin streaming the camera preview to the SurfaceView
        cameraStream.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {

                //TODO: Fix permissions testing
                try {
                    cameraPreview.start(cameraStream.getHolder());
                } catch (IOException | SecurityException ex) {
                    ex.printStackTrace();
                    Log.e(getString(R.string.perm_error), ex.toString());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                //Left as default
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //Stop the camera
                cameraPreview.stop();

            }
        });

        //Process QR codes
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            //Get detected QR codes
            @Override
            public void receiveDetections(Detector.Detections detections) {

                //Store found codes
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                //If any codes are found, set their contents to display in the text view
                if (barcodes.size() != 0) {

                    //Stop the camera to prevent multiple scans?
                    Log.i("realm", "releasing barcode detector");
                    barcodeDetector.release();

                    Log.i("realm", "Entering .post() method");

                    qrCodeContents.post(new Runnable() {
                        @Override
                        public void run() {

                            Log.i("realm", "Entered .post() method");

                            final String studentName = barcodes.valueAt(0).displayValue;
                            //Set textview to show the QR code contents

                            Log.i("realm", "Found a QR code, setting TextView to it");
                            qrCodeContents.setText(studentName);

                            //Start destination activity to get destination as result
                            Intent getDestinationIntent = new Intent(getApplicationContext(), DestinationActivity.class);

                            getDestinationIntent.putExtra("name", studentName);

                            Log.i("realm", "Starting destination activity");
                            startActivityForResult(getDestinationIntent, DESTINATION_REQUEST);


                        }

                    });
                    Log.i("realm", "Thread ends when another QR code is scanned?");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {

        //Release resources
        super.onDestroy();
        cameraPreview.release();
        barcodeDetector.release();
        database.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent getDestinationIntent) {
        Log.i("realm", "Made it back to onActivityResult");

        super.onActivityResult(requestCode, resultCode, getDestinationIntent);

        if(requestCode == DESTINATION_REQUEST) {

            if(resultCode == RESULT_OK) {
                final String destination = getDestinationIntent.getStringExtra("destination");


                Log.i("realm", "This is the destination received" + destination);

                database.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm database) {

                        Log.i("realm", "Creating student object");
                        Student student = database.createObject(Student.class);

                        student.date = new SimpleDateFormat("MM-dd-yyyy", Locale.US).format(new Date());

                        student.timeOut = new SimpleDateFormat("HH-mm-ss", Locale.US).format(new Date());

                        student.name = "Destination Debug name";

                        student.destination = destination;

                        Log.i("realm", "added all properties to object - Object is fully committed?");

                    }

                });

            }
        }

        Log.i("realm", "Calling QR scan to rebuilt detector");

        startQRCodeScanner();
        Log.i("realm", "Leaving onActivityResult");

    }
}
