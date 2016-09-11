package com.devmicheledonato.thesis;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeofenceTransitionsIntentService extends IntentService {

    private final String TAG = this.getClass().getSimpleName();
    public static final String GEOFENCE_TRANSITION_ACTION = "GEOFENCE_TRANSITION_ACTION";
    public static final String GEOFENCE_TRANSITION_ENTER_EXTRA = "GEOFENCE_TRANSITION_ENTER_EXTRA";
    public static final String GEOFENCE_TRANSITION_EXIT_EXTRA = "GEOFENCE_TRANSITION_EXIT_EXTRA";

    private ThesisApplication app;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
        app = ThesisApplication.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // if the preference updates on settings is off then return
        if (!app.isKeyPrefUpdatesOn()) {
            return;
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + errorCode);
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        // Get the geofences that were triggered. A single event can trigger multiple geofences.
        List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
        // Suppose there is only one geofence that triggered
        Geofence geofence = triggeringGeofences.get(0);
        Intent i = new Intent(this, LocationService.class);
        i.setAction(GEOFENCE_TRANSITION_ACTION);
        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.i(TAG, "ENTER");
            i.putExtra(GEOFENCE_TRANSITION_ENTER_EXTRA, geofence.getRequestId());
            startService(i);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i(TAG, "EXIT");
            i.putExtra(GEOFENCE_TRANSITION_EXIT_EXTRA, geofence.getRequestId());
            startService(i);
        } else {
            // Log the error.
            Log.e(TAG, "Invalid type of Transition: " + geofenceTransition);
        }
    }
}
