package com.devmicheledonato.thesis.simplegeofence;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleGeofenceBuilder {

    public static final float DISTANCE_RADIUS = 100; // meters
    //    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000; // 24 hours
    private static final long EXPIRATION_TIME = Geofence.NEVER_EXPIRE;
    private static final int TRANSITION = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;

    // File
    private File geoFile;
    // Writer for json file
    private FileWriter fileWriter;
    // Buffer for FileWriter
    private BufferedWriter bufferedWriter;
    // To print on file
    private PrintWriter printWriter;

    private String mId;
    private double mLatitude;
    private double mLongitude;
    private float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;

    private List<LatLng> positionList;
    private Location enterPoint;

    public SimpleGeofenceBuilder(Context context, boolean delete) {
        String str_geo_file = "geofenceBuilder";
        geoFile = new File(context.getExternalCacheDir(), str_geo_file);

        this.enterPoint = null;

        if (delete) {
            deleteFile();
        } else if (geoFile.exists()) {
            this.enterPoint = getFirstPoint();
        }

        // List of positions
        positionList = new ArrayList<LatLng>();
    }

    public boolean deleteFile() {
        if (geoFile.exists()) {
            return geoFile.delete();
        }
        return false;
    }

//    public SimpleGeofenceBuilder(Location location) {
//        // List of positions
//        positionList = new ArrayList<Location>();
//        this.enterPoint = location;
//        this.enterDate = location.getTime();
//        // add the initial point
//        positionList.add(enterPoint);
//    }

    public Location getEnterPoint() {
        return enterPoint;
    }

    public Long getEnterDate() {
        return getEnterPoint().getTime();
    }

    public void addLocation(Location point) {
        if (enterPoint == null) {
            enterPoint = point;
        }
        addLocationToFile(point);
    }


    private void addLocationToFile(Location point) {
        fileWriter = null;
        bufferedWriter = null;
        printWriter = null;
        try {
            fileWriter = new FileWriter(geoFile, true);
            bufferedWriter = new BufferedWriter(fileWriter);
            printWriter = new PrintWriter(bufferedWriter);
            String line = point.getLatitude() + "," + point.getLongitude() + "," + point.getTime();
            printWriter.println(line);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null)
                    bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fileWriter != null)
                    fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Location getFirstPoint() {
        Location loc = new Location("");

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(geoFile);
            bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            String[] arrayLine = line.split(",");

            loc.setLatitude(Double.parseDouble(arrayLine[0]));
            loc.setLongitude(Double.parseDouble(arrayLine[1]));
            loc.setTime(Long.parseLong(arrayLine[2]));

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
        return loc;
    }

    private void readGeoFile() {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(geoFile);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] arrayLine = line.split(",");
                positionList.add(new LatLng(Double.parseDouble(arrayLine[0]), Double.parseDouble(arrayLine[1])));
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

    public SimpleGeofence prepareGeofence() {
        mId = UUID.randomUUID().toString();
        readGeoFile();
        LatLng centroid = getCentroid();
        mLatitude = centroid.latitude;
        mLongitude = centroid.longitude;
        mRadius = DISTANCE_RADIUS;
        mTransitionType = TRANSITION;
        mExpirationDuration = EXPIRATION_TIME;
        return new SimpleGeofence(mId, mLatitude, mLongitude, mRadius, mExpirationDuration, mTransitionType);
    }

    // Computes the center of all positions
    private LatLng getCentroid() {
        double lat = 0, lng = 0;
        int n = positionList.size();
        for (LatLng loc : positionList) {
            lat += loc.latitude;
            lng += loc.longitude;
        }
        lat = lat / n;
        lng = lng / n;
        return new LatLng(lat, lng);
    }
}
