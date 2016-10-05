package com.devmicheledonato.thesis.fragments;


import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.devmicheledonato.thesis.GeofenceFile;
import com.devmicheledonato.thesis.LocationService;
import com.devmicheledonato.thesis.MainActivity;
import com.devmicheledonato.thesis.R;
import com.devmicheledonato.thesis.simplegeofence.SimpleGeofence;
import com.devmicheledonato.thesis.simplegeofence.SimpleGeofenceStore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private final String TAG = this.getClass().getSimpleName();

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private MapView mMapView;

    private Context context;
    // File
    private File file;
    // Writer for json file
    private FileWriter fileWriter;
    // Buffer for FileWriter
    private BufferedWriter bufferedWriter;
    // To print on file
    private PrintWriter printWriter;

    private SimpleGeofenceStore simpleGeofenceStore;
    private SimpleGeofence geofence;

    private ArrayList<String> mListID;
    private ArrayList<Marker> mMarkers;
    private ArrayList<Circle> mCircles;

    public MapsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapsFragment newInstance(String param1, String param2) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static MapsFragment newInstance() {
        MapsFragment fragment = new MapsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        context = getActivity();
        file = new File(context.getExternalCacheDir(), GeofenceFile.GEOFENCE_FILENAME);

        simpleGeofenceStore = new SimpleGeofenceStore(context);
        mListID = new ArrayList<String>();
        mMarkers = new ArrayList<Marker>();
        mCircles = new ArrayList<Circle>();

//        mapFragment.getMapAsync(this);

//        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMap();
//        mapFragment.getMapAsync(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady");
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.i(TAG, "Permission not Granted");
            MainActivity.getInstance().requestLocationPermission(MainActivity.REQUEST_MAPS_LOCATION);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            Location loc;
            if ((loc = LocationService.getLocation()) != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(loc.getLatitude(), loc.getLongitude())).zoom(16).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }

        // add geofence's marker on map
        addGeofenceMarker();

        GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout ll = new LinearLayout(context);
                ll.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setText(marker.getTitle());
                title.setTypeface(null, Typeface.BOLD);
                title.setTextColor(Color.BLACK);

                TextView snippet = new TextView(context);
                snippet.setText(marker.getSnippet());

                title.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                snippet.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                ll.addView(title);
                ll.addView(snippet);

                return ll;
            }
        };
        mMap.setInfoWindowAdapter(infoWindowAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_maps, container, false);

        mMapView = (MapView) root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        return root;
    }

    private void removeMarkersCircles() {
        for (Marker marker : mMarkers) {
            marker.remove();
        }
        mMarkers.clear();
        for (Circle circle : mCircles) {
            circle.remove();
        }
        mCircles.clear();
    }

    private void addGeofenceMarker() {
        removeMarkersCircles();

        if (file.exists()) {
            readFile();
            for (String id : mListID) {
                geofence = simpleGeofenceStore.getGeofence(id);

                LatLng center = new LatLng(geofence.getLatitude(), geofence.getLongitude());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(center);
                markerOptions.title(id);

                Long dateEnter;
                Long dateExit = Long.valueOf(0);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ITALY);
                String dateENTERString = "";
                String dateEXITString = "";

                if (simpleGeofenceStore.getGeofenceDateEnter(id) != simpleGeofenceStore.INVALID_LONG_VALUE) {
                    dateEnter = simpleGeofenceStore.getGeofenceDateEnter(id);
                    dateENTERString = simpleDateFormat.format(dateEnter);
                    if (simpleGeofenceStore.getGeofenceDateExit(id) != simpleGeofenceStore.INVALID_LONG_VALUE) {
                        dateExit = simpleGeofenceStore.getGeofenceDateExit(id);
                        dateEXITString = simpleDateFormat.format(dateExit);
                    }

                    markerOptions.snippet("DateEnter: " + dateENTERString + "\nDateExit: " + (dateEXITString.equals("") ? "NA" : dateEXITString));
                }

                Marker marker = mMap.addMarker(markerOptions);
                mMarkers.add(marker);


                // Instantiates a new CircleOptions object and defines the center and radius
                CircleOptions circleOptions = new CircleOptions()
                        .center(center)
                        .radius(100) // In meters
                        .strokeWidth(2)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.parseColor("#500084d3"));

                // Get back the mutable Circle
                Circle circle = mMap.addCircle(circleOptions);
                mCircles.add(circle);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshButton:
                addGeofenceMarker();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void readFile() {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineSplitted = line.split(",");
                String id = lineSplitted[0];
                if (!mListID.contains(id)) {
                    mListID.add(id);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null)
                    bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.i(TAG, "onLowMemory");
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}
