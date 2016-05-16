package com.devmicheledonato.thesis;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

public class LocationChangedIntentService extends IntentService {

    private static final String TAG = "LocationChangedIS";
    public static final String LOCATION_CHANGE_ACTION = "LOCATION_CHANGE_ACTION";
    public static final String LOCATION_CHANGE_EXTRA = "LOCATION_CHANGE_EXTRA";

    public LocationChangedIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && LocationResult.hasResult(intent)) {
            Log.d(TAG, "LocationResult");
            LocationResult result = LocationResult.extractResult(intent);
            Location location = result.getLastLocation();
            Intent i = new Intent(this, LocationService.class);
            i.setAction(LOCATION_CHANGE_ACTION);
            i.putExtra(LOCATION_CHANGE_EXTRA, location);
            startService(i);
        }
    }
}
