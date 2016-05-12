package com.devmicheledonato.thesis;

import android.location.Location;
import android.transition.Transition;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SimpleGeofence {

    public static final float DISTANCE_RADIUS = 50;
    private static final long GEOFENCE_EXPIRATION_TIME = 12 * 60 * 60 * 1000;
    List<Location> positionList;

    private String mId;
    private double mLatitude;
    private double mLongitude;
    private float mRadius;
    private long mExpirationDuration;
    private int mTransitionType;

    private final Location enterPoint;
    private final Long enterDate;

    public SimpleGeofence(Location location, Long time) {
        positionList = new ArrayList<Location>();

        this.enterPoint = location;
        this.enterDate = time;

        positionList.add(enterPoint);
    }

    public Location getEnterPoint(){
        return enterPoint;
    }

    public void addLocation(Location location){
        positionList.add(location);
    }

    private LatLng getCentroid(){
        Location loc;
        double lat = 0, lng = 0;
        int n = positionList.size();
        Iterator<Location> itr = positionList.iterator();
        while(itr.hasNext()){
            loc = itr.next();
            lat += loc.getLatitude();
            lng += loc.getLongitude();
        }
        lat = lat/n;
        lng = lng/n;
        return new LatLng(lat, lng);
    }

    public double getLatitude(){
        return mLatitude;
    }
    public double getLongitude(){
        return mLongitude;
    }

    public Geofence prepareGeofence(){
        String id = UUID.randomUUID().toString();
        mId = id;
        LatLng centroid = getCentroid();
        mLatitude = centroid.latitude;
        mLongitude = centroid.longitude;
        mRadius = DISTANCE_RADIUS;
        mTransitionType = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;
        mExpirationDuration = GEOFENCE_EXPIRATION_TIME;
        return toGeofence();
    }

    /**
     * Creates a Location Services Geofence object from a SimpleGeofence.
     * @return A Geofence object.
     */
    private Geofence toGeofence() {
        // Build a new Geofence object.
        return new Geofence.Builder()
                .setRequestId(mId)
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(mLatitude, mLongitude, mRadius)
                .setExpirationDuration(mExpirationDuration)
                .build();
    }
}
