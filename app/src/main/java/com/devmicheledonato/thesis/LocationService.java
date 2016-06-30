package com.devmicheledonato.thesis;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.devmicheledonato.thesis.simplegeofence.SimpleGeofence;
import com.devmicheledonato.thesis.simplegeofence.SimpleGeofenceBuilder;
import com.devmicheledonato.thesis.simplegeofence.SimpleGeofenceStore;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    // TAG for debug
    private final String TAG = this.getClass().getSimpleName();

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static long seconds = 1000;
    private static long minutes = 60 * seconds;

    private static final float DISTANCE_RADIUS = 50; // meters

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

    public static final String START_LOCATION_ACTION = "START_LOCATION_ACTION";

    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life and a real
     * app may prefer to request less frequent updates.
     */
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;

    private static final String START_GEOFENCING = "START_GEOFENCING";
    private static final String DISPLACEMENT = "DISPLACEMENT";
    private static final String COUNTDOWN = "COUNTDOWN";
    private static final String STR_LOCATION_FILE = "STR_LOCATION_FILE";
    private static final String LOCATION_UPDATES = "LOCATION_UPDATES";

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
//    protected UpdatesBroadcastReceiver mBroadcastReceiver;

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

    private LocationFile locationFile;
    private String str_location_file;

    private SimpleGeofenceBuilder simpleGeofenceBuilder;
    private SimpleGeofence simpleGeofence;
    private SimpleGeofenceStore simpleGeofenceStore;

    private CountDownTimer timer;
    private Location enterPoint;
    private SharedPreferences sharedPref;
    private RESTcURL mRESTcURL;
    private String personID;
    private static final String ERROR_ID = "error_id";

    private boolean startGeofencing;
    private boolean displacement;
    private boolean countDownFinished;
    private boolean boolGeofence;
    private boolean boolCheckLocationSettings;
    private boolean boolRemoveRequest;
    private boolean boolStartLocationUpdates;

    private boolean locationUpdates;

    public LocationService() {
        Log.i(TAG, "MyService");
    }

    public static Location getLocation() {
        return mCurrentLocation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    private void saveStateInSharedPref() {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(START_GEOFENCING, startGeofencing);
        editor.putBoolean(DISPLACEMENT, displacement);
        editor.putBoolean(COUNTDOWN, countDownFinished);
        editor.putBoolean(LOCATION_UPDATES, locationUpdates);

        // save the name of location file (timeLocation)
        editor.putString(STR_LOCATION_FILE, str_location_file);

        editor.apply();
    }

    private void restoreStateFromSharedPref() {
        startGeofencing = sharedPref.getBoolean(START_GEOFENCING, false);
        displacement = sharedPref.getBoolean(DISPLACEMENT, false);
        countDownFinished = sharedPref.getBoolean(COUNTDOWN, false);
        locationUpdates = sharedPref.getBoolean(LOCATION_UPDATES, false);

        // restore the name of location file
        str_location_file = sharedPref.getString(STR_LOCATION_FILE, null);
        if (str_location_file != null) {
            locationFile = new LocationFile(this, str_location_file);
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        mCurrentLocation = null;
        simpleGeofenceBuilder = new SimpleGeofenceBuilder(this, false);
        simpleGeofenceStore = new SimpleGeofenceStore(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        restoreStateFromSharedPref();

        mRESTcURL = new RESTcURL();

        personID = sharedPref.getString(SignInActivity.PERSON_ID, ERROR_ID);

        // Get a receiver for broadcasts from ActivityDetectionIntentService.
//        mBroadcastReceiver = new UpdatesBroadcastReceiver();

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

//        Toast.makeText(getApplicationContext(), "StartCommand", Toast.LENGTH_SHORT).show();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // If we don't have permission, stop the service
            stopSelf();
            Log.i(TAG, "stopSelf");
            return START_NOT_STICKY;
        }

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(LocationChangedIntentService.LOCATION_CHANGE_ACTION)) {
                    if (intent.hasExtra(LocationChangedIntentService.LOCATION_CHANGE_EXTRA)) {
                        Location location = intent.getParcelableExtra(LocationChangedIntentService.LOCATION_CHANGE_EXTRA);
                        locationChanged(location);
                        return START_NOT_STICKY;
                    }
                }
                // Receives a DetectedActivity objects associated with the current state of the device.
                if (action.equals(DetectedActivitiesIntentService.DETECTED_ACTIVITY_ACTION)) {
                    if (intent.hasExtra(DetectedActivitiesIntentService.DETECTED_ACTIVITY_EXTRA)) {
                        DetectedActivity updatedActivity = intent.getParcelableExtra(DetectedActivitiesIntentService.DETECTED_ACTIVITY_EXTRA);
                        updateDetectedActivity(updatedActivity);
                        return START_NOT_STICKY;
                    }
                }
                if (action.equals(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_ACTION)) {
                    if (intent.hasExtra(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_ENTER_EXTRA)) {
                        String id = intent.getStringExtra(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_ENTER_EXTRA);
//                        Log.i(TAG, "TRANSITION_ENTER");
                        geofenceTransitionEnter(id);
                        return START_NOT_STICKY;
                    }
                    if (intent.hasExtra(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_EXIT_EXTRA)) {
                        String id = intent.getStringExtra(GeofenceTransitionsIntentService.GEOFENCE_TRANSITION_EXIT_EXTRA);
//                        Log.i(TAG, "TRANSITION_EXIT");
                        geofenceTransitionExit(id);
                        return START_NOT_STICKY;
                    }
                }
                // Receives the Accuracy for location updates
                if (ACCURACY_ACTION.equals(action)) {
                    if (intent.hasExtra(ACCURACY_EXTRA) && locationUpdates) {
                        int mAccuracy = intent.getIntExtra(ACCURACY_EXTRA, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        updateAccuracy(mAccuracy);
                        return START_NOT_STICKY;
                    }
                }
                if (START_LOCATION_ACTION.equals(action)) {
                    if (mGoogleApiClient.isConnected()) {
                        Log.i(TAG, "GoogleApiClient Connected");
                        checkLocationSettings();
                    } else if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                        Log.i(TAG, "GoogleApiClient not Connected");
                        boolCheckLocationSettings = true;
                        mGoogleApiClient.connect();
                    }
                    return START_NOT_STICKY;
                }
            } else {
                Log.i(TAG, "Action is null");
            }
        } else {
            Log.i(TAG, "Intent is null");
        }

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
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        if (mGoogleApiClient.isConnected()) {
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,
                    DETECTION_INTERVAL_IN_MILLISECONDS,
                    getActivityDetectionPendingIntent()
            );

            locationUpdates = true;

        } else {
            boolStartLocationUpdates = true;
            mGoogleApiClient.connect();
        }
        // DEVO IMPLEMENTARE onResult se voglio usare il setResultCallback
//      .setResultCallback(this);

    }

    protected void stopLocationUpdates() {
        Log.i(TAG, "stopLocationUpdates");
        if (mGoogleApiClient.isConnected()) {
            // The service doesn't update the location anymore
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getLocationChangePendingIntent());

            // Remove all activity updates for the PendingIntent that was used to request activity
            // updates.
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    getActivityDetectionPendingIntent()
            );
            // VEDI SOPRA
            //.setResultCallback(this);
        }

        locationUpdates = false;
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

        if (boolGeofence) {
            addGeofence();
            boolGeofence = false;
        }

        if (boolRemoveRequest) {
            removeRequestLocationUpdates();
            boolRemoveRequest = false;
        }

        if (boolCheckLocationSettings) {
            checkLocationSettings();
            boolCheckLocationSettings = false;
        }

        if (boolStartLocationUpdates) {
            startLocationUpdates();
            boolStartLocationUpdates = false;
        }
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

    private void locationChanged(Location location) {
        Log.i(TAG, "locationChanged");
        countDownFinished = false;
        // if the countdown is over, I do nothing
        // this is for prevent if a location update should come after the end of the countdown
        if (countDownFinished) {
            Log.i(TAG, "countDownFinished");
            // countDownFinished will reset when a geofence transition will arrive.
            return;
        }

        mCurrentLocation = location;
//        Calendar calendar = Calendar.getInstance();
//        Date date = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ITALY);
        Long timeLocation = mCurrentLocation.getTime();
        String dateString = simpleDateFormat.format(timeLocation);
        Log.d(TAG, "Location " + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude() + " Time " + dateString);

        if (!startGeofencing) {
            Log.i(TAG, "startGeofencing");
            // Create new file with name dateString
            locationFile = new LocationFile(this, Long.toString(timeLocation));
            str_location_file = Long.toString(timeLocation);
            // Start the geofencing
            startGeofencing = true;
            // Set countdown
            setCountdown();
            // Save initial point
            simpleGeofenceBuilder = new SimpleGeofenceBuilder(this, true);
            simpleGeofenceBuilder.addLocation(mCurrentLocation);

        } else {
            float distance = simpleGeofenceBuilder.getEnterPoint().distanceTo(mCurrentLocation);
            Log.i(TAG, "Distance: " + distance);
            if (distance > DISTANCE_RADIUS) {
                Log.i(TAG, "Distance greater");
                // Means there are displacement locations
                displacement = true;
                // Reset countdown
                setCountdown();
                // Save the new initial point
                simpleGeofenceBuilder = new SimpleGeofenceBuilder(this, true);
                simpleGeofenceBuilder.addLocation(mCurrentLocation);
            } else {
                // insert new location in the geofence's list
                simpleGeofenceBuilder.addLocation(mCurrentLocation);
            }
        }
        // Save location into file
        locationFile.writeFile(mCurrentLocation.getTime() + "," + mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude());
    }

    private void setCountdown() {
        Log.i(TAG, "setCountdown");
        if (timer != null) {
            Log.i(TAG, "Timer cancelled");
            timer.cancel();
        }
        timer = new CountDownTimer(1 * minutes, 1 * minutes) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, Long.toString(millisUntilFinished));
            }

            @Override
            public void onFinish() {
//                timer = null;
                Log.i(TAG, "Timer finished");
                stopLocationUpdates();
                createRequestGeofence();

                countDownFinished = true;
                saveStateInSharedPref();
            }
        };
        timer.start();
    }

    private void createRequestGeofence() {
        // Create a SimpleGeofence
        simpleGeofence = simpleGeofenceBuilder.prepareGeofence();
        // Save SimpleGeofence in the store
        simpleGeofenceStore.setGeofence(simpleGeofence.getId(), simpleGeofence);
        // Get the PendingIntent for the geofence monitoring request.
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        // Send a request to add the current geofence.
        mGeofencingRequest = getGeofencingRequest(simpleGeofence.toGeofence());
        Log.i(TAG, "addGeofence G-" + simpleGeofence.getId());
        addGeofence();
        startGeofencing = false;
    }

    private void addGeofence() {
        if (mGoogleApiClient.isConnected()) {
            try {
                LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, mGeofencingRequest, mGeofenceRequestIntent);

            } catch (SecurityException securityException) {
                securityException.printStackTrace();
            }
        } else {
            boolGeofence = true;
            mGoogleApiClient.connect();
        }
    }

    private void geofenceTransitionEnter(String id) {
        Log.i(TAG, "ENTER G-" + id);

        // stop previous countdown
        if (timer != null) {
            Log.i(TAG, "Timer cancelled");
            timer.cancel();
        }
        // it's possible to restart a new countdown
        countDownFinished = true;
        // stop location updates
        stopLocationUpdates();

        // if there are displacement locations, I send data to server
        if (displacement) {
            Log.i(TAG, "There is displacement locations");
            mRESTcURL.postDisplacement(locationFile.fileToJson());
            displacement = false;
        }
        // delete file
        locationFile.deleteFile();

        SimpleGeofence sgfence = simpleGeofenceStore.getGeofence(id);
        // if the geofence has not dateEnter, I set now as dateEnter
        if (sgfence.getEnterDate() == SimpleGeofenceStore.INVALID_LONG_VALUE) {
            Calendar calendar = Calendar.getInstance();
            Long dateEnter = calendar.getTimeInMillis();
            sgfence.setEnterDate(dateEnter);
            simpleGeofenceStore.setGeofence(id, sgfence);
        }
    }

    private void geofenceTransitionExit(String id) {
        Log.i(TAG, "EXIT G-" + id);

        // when I leave the geofence, I get date of exit and send all geofence's information to server
        Calendar calendar = Calendar.getInstance();
        Long dateExit = calendar.getTimeInMillis();

        SimpleGeofence sgfence = simpleGeofenceStore.getGeofence(id);
        try {
            Log.i(TAG, "postPlace");
            JSONObject jsonPlace = new JSONObject();
            jsonPlace.put("userID", personID);
            jsonPlace.put("lat", sgfence.getLatitude());
            jsonPlace.put("lng", sgfence.getLongitude());
            jsonPlace.put("dateEnter", sgfence.getEnterDate());
            jsonPlace.put("dateExit", dateExit);
            mRESTcURL.postPlace(jsonPlace);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ITALY);
            String dateENTERString = simpleDateFormat.format(sgfence.getEnterDate());
            String dateEXITString = simpleDateFormat.format(dateExit);

//            Log.i(TAG, personID + " "
//                    + sgfence.getLatitude() + " "
//                    + sgfence.getLongitude() + " "
//                    + dateENTERString + " "
//                    + dateEXITString);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        sgfence.setEnterDate(SimpleGeofenceStore.INVALID_LONG_VALUE);
        simpleGeofenceStore.setGeofence(id, sgfence);

        simpleGeofenceBuilder.deleteFile();

        // it's possible to restart a new countdown
        countDownFinished = false;
        // restart location updates
        startLocationUpdates();
    }

    private GeofencingRequest getGeofencingRequest(Geofence geofence) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geofence);
        return builder.build();
    }

    /**
     * Gets a PendingIntent to be sent for each location change.
     */
    private PendingIntent getLocationChangePendingIntent() {
        Intent intent = new Intent(this, LocationChangedIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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
                setUpdateInterval(10 * seconds, 10 * seconds);
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
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getLocationChangePendingIntent());
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, getLocationChangePendingIntent());
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            boolRemoveRequest = true;
            mGoogleApiClient.connect();
        }
    }
}