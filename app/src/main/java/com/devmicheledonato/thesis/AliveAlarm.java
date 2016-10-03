package com.devmicheledonato.thesis;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class AliveAlarm extends BroadcastReceiver {
    
    private String TAG = this.getClass().getSimpleName();

    private static final String ERROR_ID = "error_id";
    
    private final RESTcURL mRESTcURL;
    private final JSONObject jsonAlive;
    private AlarmManager alarmManager;

    public AliveAlarm(){
        Log.d(TAG, "AliveAlarm constructor");

        mRESTcURL = new RESTcURL();
        jsonAlive = new JSONObject();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        String personID = intent.getStringExtra(SignInActivity.PERSON_ID);
        
        try {
            jsonAlive.put("userID", personID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRESTcURL.postAlive(jsonAlive);
    }

    public void setAlarm(Context context){
        Log.d(TAG, "setAlarm");
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                getPendingIntent(context));
    }

    public void cancelAlarm(Context context) {
        Log.d(TAG, "cancelAlarm");
        if (alarmManager != null) {
            alarmManager.cancel(getPendingIntent(context));
        }
    }

    private PendingIntent getPendingIntent(Context context){
        Intent intent = new Intent(context, AliveAlarm.class);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String personID = sharedPref.getString(SignInActivity.PERSON_ID, ERROR_ID);
        
        intent.putExtra(SignInActivity.PERSON_ID, personID);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
