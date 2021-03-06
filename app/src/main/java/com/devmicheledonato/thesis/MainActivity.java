package com.devmicheledonato.thesis;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.devmicheledonato.thesis.fragments.AboutFragment;
import com.devmicheledonato.thesis.fragments.DataFragment;
import com.devmicheledonato.thesis.fragments.MapsFragment;
import com.devmicheledonato.thesis.fragments.SettingsFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.LocationRequest;

import java.io.File;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = this.getClass().getSimpleName();
    private static MainActivity instance;
    private Intent intent;
    private boolean mServiceRunning;

    private static final String SERVICE_RUNNING = "service_running";
    private static final String LOGIN_DONE = "login_done";

    // Id to identify a location permission request from settingsFragment
    public static final int REQUEST_SETTINGS_LOCATION = 0;
    // Id to identify a location permission request from mapsFragment
    public static final int REQUEST_MAPS_LOCATION = 1;

    public static final String KEY_PREF_UPDATES = "pref_updates";
    public static final String KEY_PREF_LOGOUT = "pref_logout";
    public static final String KEY_PREF_DISCONNECT = "pref_disconnect";

    private static final String TITLE_TOOLBAR = "title_toolbar";
    private static final String MENU_ITEM_ID = "menu_item_id";

    // Fragment TAGS
    private static final String DATA_TAG = "DATA_TAG";
    private static final String MAPS_TAG = "MAPS_TAG";
    private static final String SETTINGS_TAG = "SETTINGS_TAG";
    private static final String ABOUT_TAG = "ABOUT_TAG";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Permissions required to get position.
     */
    private static final String[] PERMISSION_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    /**
     * Constant used in the location settings dialog.
     */
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private CharSequence mTitle;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private View headerLayout;
    private int mMenuItemId;

    private SharedPreferences sharedPref;
    FragmentManager fragmentManager;


    public static MainActivity getInstance() {
        Log.i("MainActivity", "getInstance");
        return instance;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

//           if(checkPlayServices()){
//            // TODO
//        }

        // Initialized with default settings
        // When false, the system sets the default values only if this method has never been called in the past
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // Obtain the default shared preferences
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean updates = sharedPref.getBoolean(KEY_PREF_UPDATES, false);
//        Log.i(TAG, "updates: " + updates);

//        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), MODE_PRIVATE);

        setContentView(R.layout.activity_main);
        // Layout of the MainActivity
        mLayout = findViewById(R.id.activity_main);
        // Instance of the MainActivity
        instance = this;
        // Intent for start and stop LocationService
        intent = new Intent(this, LocationService.class);
        // Set to false cause LocationService doesn't running
//        mServiceRunning = false;
        // Obtain fragment manager
        fragmentManager = getFragmentManager();

//        if (savedInstanceState != null) {
//            mServiceRunning = savedInstanceState.getBoolean(SERVICE_RUNNING);
//        }

        if (!sharedPref.contains(SignInActivity.PERSON_ID)) {
            login();
        }
    }

    private void login() {
        Intent intentFromSettings = getIntent();
        if (intentFromSettings.hasExtra(SignInActivity.SIGNING)) {
//            if (sharedPref.getBoolean(KEY_PREF_UPDATES, false) && mServiceRunning) {
            if (isMyServiceRunning(LocationService.class)) {
                Log.i(TAG, "startSigning.stopUpdates");
                stopUpdates();
            }
            String sign = intentFromSettings.getStringExtra(SignInActivity.SIGNING);
            Intent signIntent = new Intent(this, SignInActivity.class);
            signIntent.putExtra(SignInActivity.SIGNING, sign);
            startActivity(signIntent);
        } else {
            startActivity(new Intent(this, SignInActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Initialization of Navigation Drawer
        initNavDrawer();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

//        if (sharedPref.getBoolean(KEY_PREF_UPDATES, false) && mServiceRunning) {
        if (isMyServiceRunning(LocationService.class)) {
            Intent localIntent = new Intent(LocationService.ACCURACY_ACTION);
            localIntent.putExtra(LocationService.ACCURACY_EXTRA, LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();

//        if (sharedPref.getBoolean(KEY_PREF_UPDATES, false) && mServiceRunning) {
        if (isMyServiceRunning(LocationService.class)) {
            Intent localIntent = new Intent(LocationService.ACCURACY_ACTION);
            localIntent.putExtra(LocationService.ACCURACY_EXTRA, LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        }

        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    private void initNavDrawer() {
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set title of toolbar
        mTitle = toolbar.getTitle();

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);

        // Tie DrawerLayout events to the ActionBarToggle
//        mDrawer.addDrawerListener(drawerToggle);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Set the menu item
        mMenuItemId = nvDrawer.getMenu().getItem(0).getItemId();
        // Setup drawer view
        nvDrawer.setNavigationItemSelectedListener(this);
        // Setup header view
        setupHeaderView(nvDrawer);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectDrawerItem(item);
        return true;
//        return false;
    }

    private void setupHeaderView(NavigationView navigationView) {
        // There is usually only 1 header view.
        // Multiple header views can technically be added at runtime.
        // We can use navigationView.getHeaderCount() to determine the total number.
        headerLayout = navigationView.getHeaderView(0);
        TextView name_header = (TextView) headerLayout.findViewById(R.id.name_header);
        String user_name = sharedPref.getString(SignInActivity.PERSON_NAME, "Name");
        name_header.setText(user_name);
        TextView email_header = (TextView) headerLayout.findViewById(R.id.email_header);
        String user_email = sharedPref.getString(SignInActivity.PERSON_EMAIL, "Email");
        email_header.setText(user_email);

        ImageView photo_header = (ImageView) headerLayout.findViewById(R.id.photo_header);
//        String user_photo = sharedPref.getString("PERSON_PHOTO", null);

        File cache = getExternalCacheDir();
        File image = new File(cache, SignInActivity.PHOTO_PNG);
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap;
        if (image.exists()) {
            bitmap = new BitmapFactory().decodeFile(image.getAbsolutePath(), options);

        } else {
            bitmap = new BitmapFactory().decodeResource(getResources(), R.drawable.user_default, options);
        }
        Bitmap output = getCroppedBitmap(bitmap);
        photo_header.setImageBitmap(output);
    }

    private Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    public void selectDrawerItem(MenuItem menuItem) {
        Class fragmentClass = null;
        String tag_fragment = null;
        switch (menuItem.getItemId()) {
            case R.id.nav_data_fragment:
                fragmentClass = DataFragment.class;
                tag_fragment = DATA_TAG;
                break;
            case R.id.nav_maps_fragment:
                fragmentClass = MapsFragment.class;
                tag_fragment = MAPS_TAG;
                break;
            case R.id.nav_settings_fragment:
                fragmentClass = SettingsFragment.class;
                tag_fragment = SETTINGS_TAG;
                break;
            case R.id.nav_about_fragment:
                fragmentClass = AboutFragment.class;
                tag_fragment = ABOUT_TAG;
                break;
        }

        updateFragment(fragmentClass, tag_fragment);

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        mMenuItemId = menuItem.getItemId();
        // Set action bar title
        mTitle = menuItem.getTitle();
        toolbar.setTitle(mTitle);
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    private void updateFragment(Class fragmentClass, String tagFragment) {
        Fragment f = fragmentManager.findFragmentByTag(tagFragment);
        if (f == null) {
            Log.i(TAG, "Fragment doesn't exist");
            try {
                f = (Fragment) fragmentClass.newInstance();
                fragmentManager.beginTransaction().replace(R.id.flContent, f, tagFragment).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (drawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument.
    // onPostCreate(Bundle state) shows the hamburger icon.
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getCharSequence(TITLE_TOOLBAR);
            mMenuItemId = savedInstanceState.getInt(MENU_ITEM_ID);
        }
        toolbar.setTitle(mTitle);
        selectDrawerItem(nvDrawer.getMenu().findItem(mMenuItemId));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SERVICE_RUNNING, mServiceRunning);
        outState.putCharSequence(TITLE_TOOLBAR, toolbar.getTitle());
        outState.putInt(MENU_ITEM_ID, mMenuItemId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
//        drawerToggle.onConfigurationChanged(newConfig);
    }

//    public void changeUpdates(View view) {
//        Button updates = (Button) view;
//        if (updates.getText().equals(getResources().getString(R.string.start_updates))) {
//            startUpdates();
//        } else {
//            stopUpdates();
//        }
//    }

//    private void updateUI(boolean update) {
//        Button button = (Button) findViewById(R.id.updates);
//        if (update) {
//            button.setText(R.string.stop_updates);
//            button.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
//        } else {
//            button.setText(R.string.start_updates);
//            button.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
//        }
//    }

    // Button handler of Start Updates
    private void startUpdates() {
        Log.i(TAG, "startUpdates");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Request the Location Permissions
            requestLocationPermission(REQUEST_SETTINGS_LOCATION);
        } else {
            Log.i(TAG, "startUpdates.startService");
            startLocationService();
//            updateUI(true);
        }
    }

    // Start the LocationService
    public void startLocationService() {
        // Set the High accuracy
        intent.putExtra(LocationService.ACCURACY_EXTRA, true);
        startService(intent);
        mServiceRunning = true;
    }

    // Button handler of Stop Updates
    public void stopUpdates() {
        Log.i(TAG, "stopUpdates");
        stopService(intent);
        mServiceRunning = false;
//        updateUI(false);
    }

    // This method is invoke for check the result of startResolutionForResult in MyService
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult");
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationService();
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i(TAG, "User chose not to make required location settings changes.");
//                        updateUI(false);
                        break;
                }
                break;
        }
    }

    public void requestLocationPermission(final int reqCode) {
        Log.i(TAG, "Position permission has NOT been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG, "Displaying position permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    PERMISSION_LOCATION, reqCode);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "No rationale");
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSION_LOCATION, reqCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult");
        switch (requestCode) {
            case REQUEST_SETTINGS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted and can start service
                    startLocationService();
//                    updateUI(true);
                } else {
                    // permission denied, show the snackbar to inform the user
                    Snackbar.make(mLayout, R.string.permission_location_rationale,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            PERMISSION_LOCATION, REQUEST_SETTINGS_LOCATION);
                                }
                            })
                            .show();
                }
            }
            case REQUEST_MAPS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "req_maps_location");
                    // permission was granted
//                    Fragment f = fragmentManager.findFragmentByTag(MAPS_TAG);
//                    Fragment mf = MapsFragment.newInstance();
//                    FragmentTransaction transaction = fragmentManager.beginTransaction();
//                    transaction.remove(f);
//                    transaction.add(mf, MAPS_TAG);
//                    transaction.commit();

//                    Fragment fragment = MapsFragment.newInstance();
//                    // Insert the fragment by replacing any existing fragment
//                    FragmentManager fragmentManager = getSupportFragmentManager();
//                    fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
                } else {
                    // permission denied, show the snackbar to inform the user
                    Snackbar.make(mLayout, R.string.permission_location_rationale,
                            Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.ok, new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    ActivityCompat.requestPermissions(MainActivity.this,
                                            PERMISSION_LOCATION, REQUEST_MAPS_LOCATION);
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "onSharedPreferenceChanged");
        if (key.equals(KEY_PREF_UPDATES)) {
            boolean on = sharedPreferences.getBoolean(key, false);
            if (on) {
                Log.i(TAG, "ON");
                startUpdates();
            }
            if (!on) {
                Log.i(TAG, "OFF");
                stopUpdates();
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
