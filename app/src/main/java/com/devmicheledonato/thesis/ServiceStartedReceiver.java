package com.devmicheledonato.thesis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ServiceStartedReceiver extends BroadcastReceiver {
    public ServiceStartedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        if(sharedPref.getBoolean(MainActivity.KEY_PREF_UPDATES, false)){
            Intent i = new Intent(context, LocationService.class);
            // Set the balanced power accuracy
            i.putExtra(LocationService.ACCURACY_EXTRA, false);
            context.startService(i);
        }
    }
}
