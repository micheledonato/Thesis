package com.devmicheledonato.thesis.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import com.devmicheledonato.thesis.LocationService;
import com.devmicheledonato.thesis.MainActivity;
import com.devmicheledonato.thesis.R;
import com.devmicheledonato.thesis.SignInActivity;


public class SettingsFragment extends PreferenceFragment {


    public static final String TAG = "SETFRAGMENT_TAG";
    private MainActivity mainActivity;
    private SharedPreferences sharedPref;
    private SwitchPreference prefUpdates;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance() {

        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mainActivity = MainActivity.getInstance();
        prefUpdates = (SwitchPreference) findPreference(MainActivity.KEY_PREF_UPDATES);
        if(mainActivity.isMyServiceRunning(LocationService.class)){
            prefUpdates.setChecked(true);
        }else{
            prefUpdates.setChecked(false);
        }

        findPreference(MainActivity.KEY_PREF_LOGOUT).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Logout")
                                .setMessage("Are you sure want to logout?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i(TAG, "LOGOUT");
                                        startSigning(SignInActivity.LOGOUT);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // nothing
                                    }
                                }).show();
                return false;
            }
        });
        findPreference(MainActivity.KEY_PREF_DISCONNECT).
                setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Disconnect")
                                .setMessage("Are you sure want to disconnect and delete all files?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.i(TAG, "LOGOUT");
                                        startSigning(SignInActivity.DISCONNECT);
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // nothing
                                    }
                                }).show();
                return false;
            }
        });
    }

    public void startSigning(String sign){
        Intent i = new Intent(getActivity(), MainActivity.class);
        i.putExtra(SignInActivity.SIGNING, sign);
        startActivity(i);
    }
}