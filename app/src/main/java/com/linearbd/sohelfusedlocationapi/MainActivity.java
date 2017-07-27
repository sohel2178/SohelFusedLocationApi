package com.linearbd.sohelfusedlocationapi;

import android.Manifest;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements MyLocationService.MyLocationListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 500;
    private static final int REQUIRED_PERMISSION = 2000;
    private static final int GPS_ON = 12;

    private MyLocationService myLocationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requiredPermission();


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(myLocationService!=null){
            myLocationService.start();
        }
    }

    @Override
    protected void onPause() {
        if(myLocationService!=null){
            myLocationService.pause();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if(myLocationService!=null){
            myLocationService.resume();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        myLocationService.stop();
        super.onStop();
    }

    @AfterPermissionGranted(REQUIRED_PERMISSION)
    private void requiredPermission() {
        String[] perms = {android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Already have permission, do the thing
            // ...
           if(myLocationService==null){
               myLocationService = new MyLocationService(this,PLAY_SERVICES_RESOLUTION_REQUEST,GPS_ON);
           }
           myLocationService.start();

        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "App need to Permission for Location",
                    REQUIRED_PERMISSION, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                myLocationService.doIfPlayServiceExist();
            } else if (resultCode == RESULT_CANCELED) {
                myLocationService.doIfPlayServiceNotyExist();
            }
        }

        if (requestCode == GPS_ON) {
            Log.d("TEST","YES");
            myLocationService.requestLocationUpdate();
        }
    }

    @Override
    public void getLocation(Location location) {
        Log.d("RONY",location.getLatitude()+"");
        Log.d("RONY",location.getLongitude()+"");
    }
}
