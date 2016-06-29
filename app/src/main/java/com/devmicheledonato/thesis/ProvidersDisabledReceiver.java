package com.devmicheledonato.thesis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.LocationRequest;

public class ProvidersDisabledReceiver extends BroadcastReceiver {

    private final String TAG = this.getClass().getSimpleName();
    private final ThesisApplication app;

    public ProvidersDisabledReceiver() {
        app = ThesisApplication.getInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Get providers");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
//        List<String> providers = mLocationManager.getProviders(true);

        boolean gpsProvider = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkProvider = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean networkConnected = false;

        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Network[] networks = mConnectivityManager.getAllNetworks();
            NetworkInfo networkInfo;
            for (Network network : networks) {
                networkInfo = mConnectivityManager.getNetworkInfo(network);
                if (networkInfo.isConnected()) {
                    Log.i(TAG, network + " Connected");
                    networkConnected = true;
                }
            }
        } else {
            NetworkInfo[] networkInfos = mConnectivityManager.getAllNetworkInfo();
            if (networkInfos != null) {
                for (NetworkInfo info : networkInfos) {
                    if (info.isConnected()) {
                        Log.i(TAG, info + " Connected");
                        networkConnected = true;
                    }
                }
            }
        }
        Log.i(TAG, "GPS " + gpsProvider + " Network " + networkProvider + " NetConn " + networkConnected);
        if (!gpsProvider && (!networkProvider || !networkConnected)) {
            Log.i(TAG, "No providers");
            // Stop Service
            Intent i = new Intent(context, LocationService.class);
            context.stopService(i);
        } else if (sharedPref.getBoolean(MainActivity.KEY_PREF_UPDATES, false)) {
            if(!app.isMyServiceRunning(LocationService.class)) {
                Intent i = new Intent(context, LocationService.class);
                i.setAction(LocationService.ACCURACY_ACTION);
                // Set the balanced power accuracy
                i.putExtra(LocationService.ACCURACY_EXTRA, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                context.startService(i);
            }
        }
    }
}
