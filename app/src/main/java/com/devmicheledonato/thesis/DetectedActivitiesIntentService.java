package com.devmicheledonato.thesis;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectedActivitiesIS";
    public static final String DETECTED_ACTIVITY_ACTION = "DETECTED_ACTIVITY_ACTION";
    public static final String DETECTED_ACTIVITY_EXTRA = "DETECTED_ACTIVITY_EXTRA";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent");
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
//        Intent localIntent = new Intent(LocationService.DETECTED_ACTIVITY_ACTION);

        // Get the most probable activity associated with the current state of the
        // device. The activity is associated with a confidence level, which is an int between
        // 0 and 100.
        DetectedActivity detectedActivity = result.getMostProbableActivity();

//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//        builder.setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("ActivityDetected");
//        // Dismiss notification once the user touches it.
//        builder.setAutoCancel(true);
//
//        // Get an instance of the Notification manager
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Issue the notification
//        mNotificationManager.notify(0, builder.build());

//        // Log each activity.
//        Log.i(TAG, "activity detected ");
//        Log.i(TAG, detectedActivity.getType() + " " + detectedActivity.getConfidence() + "%");

        // Broadcast the detected activity.
//        localIntent.putExtra(LocationService.DETECTED_ACTIVITY_EXTRA, detectedActivity);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);



        Intent i = new Intent(this, LocationService.class);
        i.setAction(DETECTED_ACTIVITY_ACTION);
        i.putExtra(DETECTED_ACTIVITY_EXTRA, detectedActivity);
        startService(i);
    }
}