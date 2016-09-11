package com.devmicheledonato.thesis;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ThesisApplication extends Application {

    private static ThesisApplication mThesisApplication;
    public static synchronized ThesisApplication getInstance(){
        return mThesisApplication;
    }

    private RequestQueue mRequestQueue;

    private SharedPreferences sharedPref;
    public static final String KEY_PREF_UPDATES = "pref_updates";

    @Override
    public void onCreate() {
        super.onCreate();
        mThesisApplication = this;

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }
    
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public boolean isKeyPrefUpdatesOn(){
        return sharedPref.getBoolean(KEY_PREF_UPDATES, false);
    }

    public boolean isKeyEnterTrue(){
        return sharedPref.getBoolean(LocationService.KEY_ENTER, false);
    }
}