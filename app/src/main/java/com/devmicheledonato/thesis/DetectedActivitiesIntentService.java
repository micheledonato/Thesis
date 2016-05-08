package com.devmicheledonato.thesis;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectedActivitiesIS";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        Intent localIntent = new Intent(LocationService.DETECTED_ACTIVITY_ACTION);

        // Get the most probable activity associated with the current state of the
        // device. The activity is associated with a confidence level, which is an int between
        // 0 and 100.
        DetectedActivity detectedActivity = result.getMostProbableActivity();

//        // Log each activity.
//        Log.i(TAG, "activity detected ");
//        Log.i(TAG, detectedActivity.getType() + " " + detectedActivity.getConfidence() + "%");

        // Broadcast the detected activity.
        localIntent.putExtra(LocationService.DETECTED_ACTIVITY_EXTRA, detectedActivity);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}