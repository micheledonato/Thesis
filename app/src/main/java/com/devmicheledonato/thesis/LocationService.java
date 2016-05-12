package com.devmicheledonato.thesis;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<LocationSettingsResult> {

    // Variable for understand if Service is running or not
    private static boolean mRunning;

    // TAG for debug
    private final String TAG = this.getClass().getSimpleName();

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static long seconds = 1000;
    private static long minutes = 60 * seconds;

    private static final float DISTANCE_RADIUS = 50;

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10 * seconds;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS;

    public static final String PACKAGE_NAME = "com.devmicheledonato.thesis.LocationService";

    public static final String DETECTED_ACTIVITY_ACTION = PACKAGE_NAME + ".DETECTED_ACTIVITY_ACTION";

    public static final String DETECTED_ACTIVITY_EXTRA = PACKAGE_NAME + ".DETECTED_ACTIVITY_EXTRA";

    public static final String ACCURACY_ACTION = PACKAGE_NAME + ".ACCURACY_ACTION";
    public static final String ACCURACY_EXTRA = PACKAGE_NAME + ".ACCURACY_EXTRA";
    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life and a real
     * app may prefer to request less frequent updates.
     */
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;

    // Json file
    File file;
    // Writer for json file
    FileWriter fileWriter;
    // Buffer for FileWriter
    BufferedWriter bufferedWriter;
    // To print on file
    PrintWriter printWriter;

    // Looper
//    private Looper mServiceLooper;
//    private ServiceHandler mServiceHandler;


    // A receiver for DetectedActivity objects broadcast by the
    // {@code ActivityDetectionIntentService}.
    protected UpdatesBroadcastReceiver mBroadcastReceiver;

    // Provides the entry point to Google Play services.
    protected GoogleApiClient mGoogleApiClient;

    // Stores parameters for requests to the FusedLocationProviderApi.
    protected LocationRequest mLocationRequest;

    // Stores the types of location services the client is interested in using. Used for checking
    // settings to determine if the device has optimal location settings.
    protected LocationSettingsRequest mLocationSettingsRequest;

    // Represents a geographical location.
    protected static Location mCurrentLocation;

    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private GeofencingRequest mGeofencingRequest;

    private boolean first;
    private LocationFile locationFile;
    private SimpleGeofence simpleGeofence;
    private boolean startGeofencing;
    private CountDownTimer timer;
    private Location enterPoint;

    public LocationService() {
        Log.i(TAG, "MyService");
    }

//    private final class ServiceHandler extends Handler{
//        public ServiceHandler(Looper looper){
//            super(looper);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    }

    public static boolean isRunning() {
        return mRunning;
    }

    public static Location getLocation() {
        return mCurrentLocation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

//        first = true;
//        highAccuracy = true;

        mRunning = false;
        mCurrentLocation = null;

        startGeofencing = false;
        simpleGeofence = null;

        // Get a receiver for broadcasts from ActivityDetectionIntentService.
        mBroadcastReceiver = new UpdatesBroadcastReceiver();

        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();

//        HandlerThread thread = new HandlerThread("ServiceStartArguments",
//                Process.THREAD_PRIORITY_BACKGROUND);
//        thread.start();
//        mServiceLooper = thread.getLooper();
//        mServiceHandler = new ServiceHandler(mServiceLooper);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

//        startForeground();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "stopSelf");
            // If we don't have permission, stop the service
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent != null) {
            Log.i(TAG, "Intent not null");
            boolean highAccuracy = intent.getBooleanExtra(ACCURACY_EXTRA, false);
            if (highAccuracy) {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            } else {
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            }
//            if (intent.getBooleanExtra("Json", false)) {
//                fileToJson();
//            }
        }

        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "GoogleApiClient Connected");
            checkLocationSettings();
//            startLocationUpdates();
//            return START_STICKY;
        } else if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            Log.i(TAG, "GoogleApiClient not Connected");
            mGoogleApiClient.connect();
        }

        // Create a new intentFilter and add 2 action that BroadcastReceiver can receive
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DETECTED_ACTIVITY_ACTION);
        intentFilter.addAction(ACCURACY_ACTION);
        // Register the broadcast receiver that informs this activity of the DetectedActivity
        // object broadcast sent by the intent service.
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, intentFilter);


        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();

        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
            mGoogleApiClient.disconnect();
        }

        // Unregister the broadcast receiver that was registered during onResume().
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

//        stopForeground();
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();
    }

    protected void createLocationRequest() {
        Log.i(TAG, "createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        // Always show the dialog, without the "Never" option to suppress future dialogs from this app
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }

    protected void checkLocationSettings() {
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        mLocationSettingsRequest
                );
        result.setResultCallback(this);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // All location settings are satisfied. The client can
                // initialize location requests here.
                Log.i(TAG, "All location settings are satisfied.");
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to " +
                        "upgrade location settings ");
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(MainActivity.getInstance(), REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                    Log.i(TAG, "PendingIntent unable to execute request.");
                }
                break;
            // The status will never be SETTINGS_CHANGE_UNAVAILABLE if use builder.setAlwaysShow(true);
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    protected void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates");
//        try {
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
//
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }

        /**
         * Registers for activity recognition updates using
         * {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates} which
         * returns a {@link com.google.android.gms.common.api.PendingResult}. Since this activity
         * implements the PendingResult interface, the activity itself receives the callback, and the
         * code within {@code onResult} executes. Note: once {@code requestActivityUpdates()} completes
         * successfully, the {@code DetectedActivitiesIntentService} starts receiving callbacks when
         * activities are detected.
         */

        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        );
        // DEVO IMPLEMENTARE onResult se voglio usare il setResultCallback
//      .setResultCallback(this);

    }

    protected void stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates");
        // The service doesn't update the location anymore
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        // so I put mRunning to false
        mRunning = false;

        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mGoogleApiClient,
                getActivityDetectionPendingIntent()
        );
        // VEDI SOPRA
        //.setResultCallback(this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to GoogleApiClient");

        try {
            Log.i(TAG, "getLastLocation");
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        checkLocationSettings();
//        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged");
        mCurrentLocation = location;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ITALY);
        Date date = calendar.getTime();
        String dateString = simpleDateFormat.format(date);
        Long timeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "Location " + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude() + " Time " + dateString);
//
//        if (!startGeofencing) {
//            Log.i(TAG, "startGeofencing");
//            // Create new file with name dateString
//            locationFile = new LocationFile(this, dateString);
//            startGeofencing = true;
//
//            // Set countdown
//            setCountdown();
//            // Save initial point
//            simpleGeofence = new SimpleGeofence(mCurrentLocation, timeInMillis);
//            enterPoint = mCurrentLocation;
//        } else {
//            float distance = enterPoint.distanceTo(mCurrentLocation);
//            Log.i(TAG, "Distance: " + distance);
//            if (distance > DISTANCE_RADIUS) {
//                Log.i(TAG, "Distance greater");
//                // Reset countdown
//                setCountdown();
//                // Save the new initial point
//                simpleGeofence = new SimpleGeofence(mCurrentLocation, timeInMillis);
//                enterPoint = mCurrentLocation;
//            } else {
//                simpleGeofence.addLocation(mCurrentLocation);
//            }
//        }
//        // Save location into file
//        locationFile.writeFile(timeInMillis + "," + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
    }

    private void setCountdown() {
        Log.i(TAG, "setCountdown");
        if (timer != null) {
            Log.i(TAG, "Timer cancelled");
            timer.cancel();
        }
        timer = new CountDownTimer(3 * minutes, 1 * minutes) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, Long.toString(millisUntilFinished));
            }

            @Override
            public void onFinish() {
//                timer = null;
                Log.i(TAG, "Timer finished");
                addGeofence();
            }
        };
        timer.start();
    }

    private void addGeofence() {
        Log.i(TAG, "addGeofence");
        stopLocationUpdates();
        Geofence geofence = simpleGeofence.prepareGeofence();
        // Get the PendingIntent for the geofence monitoring request.
        // Send a request to add the current geofence.
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        mGeofencingRequest = getGeofencingRequest(geofence);
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofencingRequest, mGeofenceRequestIntent);
        } catch (SecurityException securityException) {
            securityException.printStackTrace();
        }
        startGeofencing = false;
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * UpdatesBroadcastReceiver
     */
    public class UpdatesBroadcastReceiver extends BroadcastReceiver {
        protected static final String TAG = "ActivityDetectionBR";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Receives a DetectedActivity objects associated with the current state of the device.
            if (DETECTED_ACTIVITY_ACTION.equals(action)) {
                DetectedActivity updatedActivity = intent.getParcelableExtra(DETECTED_ACTIVITY_EXTRA);
                updateDetectedActivity(updatedActivity);
            }
            // Receives the Accuracy for location updates
            if (ACCURACY_ACTION.equals(action)) {
                int mAccuracy = intent.getIntExtra(ACCURACY_EXTRA, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                updateAccuracy(mAccuracy);
            }
        }
    }

    /**
     * Processes the detected activity and update the interval of location updates
     */
    protected void updateDetectedActivity(DetectedActivity detectedActivity) {
        Log.i(TAG, "activity detected " + detectedActivity.getType() + " " + detectedActivity.getConfidence() + "%");
        int detectedActivityType = detectedActivity.getType();
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                Log.i(TAG, "IN_VEHICLE");
                setUpdateInterval(10 * seconds, 10 * seconds);
                break;
            case DetectedActivity.ON_BICYCLE:
                Log.i(TAG, "ON_BICYCLE");
                setUpdateInterval(15 * seconds, 15 * seconds);
                break;
            case DetectedActivity.RUNNING:
                Log.i(TAG, "RUNNING");
                setUpdateInterval(20 * seconds, 20 * seconds);
                break;
            case DetectedActivity.ON_FOOT:
                Log.i(TAG, "ON_FOOT");
                // The device is on a user who is walking or running.
                setUpdateInterval(35 * seconds, 35 * seconds);
                break;
            case DetectedActivity.WALKING:
                Log.i(TAG, "WALKING");
                setUpdateInterval(1 * minutes, 1 * minutes);
                break;
            case DetectedActivity.STILL:
                Log.i(TAG, "STILL");
                // The device is still (not moving).
//                setUpdateInterval(5 * minutes, 5 * minutes);
                setUpdateInterval(30 * seconds, 30 * seconds);
                break;
            case DetectedActivity.TILTING:
                Log.i(TAG, "TILTING");
                break;
            case DetectedActivity.UNKNOWN:
                Log.i(TAG, "UNKNOWN");
                break;
            default:
                Log.i(TAG, "UNIDENTIFIABLE_ACTIVITY");
                break;
        }
    }

    protected void updateAccuracy(int mAccuracy) {
        mLocationRequest.setPriority(mAccuracy);
        removeRequestLocationUpdates();
    }

    private void setUpdateInterval(long updateInterval, long updateFastestInterval) {
        // Set the Interval and the FastestInterval for location updates
        mLocationRequest.setInterval(updateInterval);
        mLocationRequest.setFastestInterval(updateFastestInterval);
        removeRequestLocationUpdates();
    }

    // Remove the previous updates and request the new location updates
    private void removeRequestLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            mGoogleApiClient.connect();
        }
    }
}