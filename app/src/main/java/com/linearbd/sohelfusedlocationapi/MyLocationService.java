package com.linearbd.sohelfusedlocationapi;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * Created by Genius 03 on 7/27/2017.
 */

public class MyLocationService implements GoogleApiClient.OnConnectionFailedListener,LocationListener,GoogleApiClient.ConnectionCallbacks {
    private static final long REQUEST_INTERVAL = 30000;//60000
    private static final long REQUEST_FASTEST_INTERVAL = 3000;//5000
    private Activity activity;
    private int gpsOn;
    private int getPlayServicesResolutionRequest;
    private GoogleApiClient mGoogleApiClient;
    private MyLocationListener listener;
    private LocationRequest mLocationRequest;

    public MyLocationService(Activity activity,int getPlayServicesResolutionRequest,int gpsOn) {
        this.activity = activity;
        this.getPlayServicesResolutionRequest = getPlayServicesResolutionRequest;
        this.gpsOn=gpsOn;
        createLocationRequest();

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }
    }

    public void pause(){
        stopLocationUpdatesOnly();

    }

    public void stop(){
        stopLocationUpdates();
    }

    public void start(){
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    public void resume(){
        if(listener==null){
            setListener((MyLocationListener) activity);
        }

    }

    private void setListener(MyLocationListener listener){
        this.listener =listener;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        turnOnLocation();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!= null){

            if(listener!=null){
                listener.getLocation(location);
            }
        }

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(REQUEST_INTERVAL);
        mLocationRequest.setFastestInterval(REQUEST_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(activity);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(activity, result,
                        getPlayServicesResolutionRequest).show();
            }

            return false;
        }

        return true;
    }

    protected void stopLocationUpdatesOnly() {
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }

    }

    private void turnOnLocation(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                // final LocationSettingsStates states = result.getLocationSettingsStates();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.

                        // Request for Location Update
                        requestLocationUpdate();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d("KKKK", "Setting RESOLUTION_REQUIRED");

                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity,
                                    gpsOn);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    public void requestLocationUpdate(){
        //noinspection MissingPermission
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    public void doIfPlayServiceExist(){
        if (!mGoogleApiClient.isConnecting() &&
                !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    public void doIfPlayServiceNotyExist(){
        Toast.makeText(activity, "Google Play Services must be installed.",
                Toast.LENGTH_SHORT).show();
        activity.finish();
    }

    public interface MyLocationListener{
        public void getLocation(Location location);
    }
}
