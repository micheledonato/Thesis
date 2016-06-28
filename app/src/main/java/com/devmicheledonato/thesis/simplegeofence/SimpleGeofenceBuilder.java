package com.devmicheledonato.thesis.simplegeofence;

import android.location.Location;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SimpleGeofenceBuilder {

    public static final float DISTANCE_RADIUS = 50;
    private static final long EXPIRATION_TIME = 12 * 60 * 60 * 1000;
    private static final int TRANSITION = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;

    private String mId;
    private double mLatitude;
    private double mLongitude;
    private float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;

    private List<Location> positionList;
    private final Location enterPoint;
    private final Long enterDate;

    public SimpleGeofenceBuilder(Location location, Long dateInMillis) {
        // List of positions
        positionList = new ArrayList<Location>();
        this.enterPoint = location;
        this.enterDate = dateInMillis;
        // add the initial point
        positionList.add(enterPoint);
    }

    public Location getEnterPoint(){
        return enterPoint;
    }

    public Long getEnterDate(){
        return enterDate;
    }

    public void addLocation(Location point) {
        positionList.add(point);
    }

    public SimpleGeofence prepareGeofence() {
        mId = UUID.randomUUID().toString();
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
        for (Location loc : positionList) {
            lat += loc.getLatitude();
            lng += loc.getLongitude();
        }
        lat = lat / n;
        lng = lng / n;
        return new LatLng(lat, lng);
    }
}
