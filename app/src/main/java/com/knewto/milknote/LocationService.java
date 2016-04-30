package com.knewto.milknote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by willwallis on 4/10/16.
 * Start Location
 * Stop Location
 * Determine Best Location
 * NOTE - Utilize pref_store_location & pref_use_gps user preferences.
 */
public class LocationService extends Service {
    private static final String TAG = "LocationService";
    LocationManager locationManager;
    LocationListener locationListener;
    Location currentBestLocation;

    // BINDING METHODS
    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBindService");
        return mBinder;
    }

    // Class for clients to access.
    public class LocalBinder extends Binder {
        LocationService getService() {
            Log.v(TAG, "LocalBinder");
            return LocationService.this;
        }
    }

    // SERVICE METHODS
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    // BOUND SERVICE METHODS
    // Start finding location - increases battery usage
    public void startLocationService(){
        Log.v(TAG, "Start requested");
        // Check preferences before creating notification
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean storeLocationPref = sharedPref.getBoolean(getString(R.string.pref_location_key), false);
        boolean useGPSPref = sharedPref.getBoolean(getString(R.string.pref_GPS_key), false);
        if (storeLocationPref) {
            startLocationManager(useGPSPref);
        } else {
            // Do nothing - user could change preference later so don't stop service
        }
    }

    // Return best determination of current location
    public Location getCurrentLocation(){
        Log.v(TAG, "getCurrentLocation");
        return currentBestLocation;
    }

    // Stop location tracking
    public void stopLocationService(){
        Log.v(TAG, "Stop Requested");
        stopLocationManager();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy");
        stopLocationManager();
        super.onDestroy();
    }

    // Custom methods for Location Services

    private void startLocationManager(Boolean useGPS){
        Log.v(TAG, "Starting Location Manager");
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if(isBetterLocation(location, currentBestLocation)){
                    currentBestLocation = location;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        currentBestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        // Use GPS if preference set
        if(useGPS){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location gpsCurrentBestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(isBetterLocation(gpsCurrentBestLocation, currentBestLocation)){
                currentBestLocation = gpsCurrentBestLocation;
            }
        }
    }

    // Stops the location manager
    private void stopLocationManager(){
        Log.v(TAG, "Releasing location manager");
        // Remove the listener you previously added
        if (locationManager != null) {locationManager.removeUpdates(locationListener);}
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        Log.v(TAG, "But is it better?");
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        } else if (location == null){
            return false;
            // A new false location is no good
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
