package com.example.cabbooking.rider.activities;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cabbooking.rider.dto.UserDto;
import com.example.cabbooking.rider.intr.AdpListner;
import com.example.cabbooking.rider.other.Const;
import com.example.cabbooking.rider.other.MySharedPref;
import com.example.cabbooking.rider.services.GPSTracker;
import com.google.gson.Gson;

import java.util.HashMap;

public abstract class BaseActivity extends AppCompatActivity implements AdpListner {
    public GPSTracker gpsTracker = null;
    protected abstract int getLayoutResourceId();
    protected abstract void setLocation1(Location location);
    public Location my_location = null;

    boolean isBound = false;

    private ServiceConnection serviceConnector = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.M)
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            try {
                GPSTracker.MyLocalBinder binder = (GPSTracker.MyLocalBinder) service;
                gpsTracker = binder.getService();
                gpsTracker.setData(BaseActivity.this);
                my_location = gpsTracker.getLocation(BaseActivity.this);
                System.out.println("tesdf--------------------------------------");
                isBound = true;
                getLocation1();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName arg0) {
            gpsTracker = null;
            System.out.println("tesdf-------------------------------1-------");
            isBound = false;
        }

    };

    String str[] = new String[]{
            android.Manifest.permission.INTERNET
            , android.Manifest.permission.ACCESS_NETWORK_STATE
            , Manifest.permission.ACCESS_COARSE_LOCATION
            , Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean checkIfAlreadyhavePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true;
        } else {
            return false;
        }
    }

    private void requestReadPhoneStatePermission() {
        ActivityCompat.requestPermissions(BaseActivity.this, str,
                1);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        try {
            if (checkIfAlreadyhavePermission()) {
                Intent intent = new Intent(getApplicationContext(), GPSTracker.class);
                if (Const.isMyServiceRunning(BaseActivity.this, GPSTracker.class) == false) {
                    startService(intent);
                }
                bindService(intent, serviceConnector, 0);

            } else {
                requestReadPhoneStatePermission();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            getLocation1();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocation1() {
        try {
            if (gpsTracker != null) {
                if (gpsTracker.canGetLocation()) {
                    my_location = gpsTracker.getLocation(BaseActivity.this);
                    setLocation1(my_location);
                } else {
                    gpsTracker.showSettingsAlert();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void click7(int position) {
        try {
            my_location = gpsTracker.getLocation(BaseActivity.this);
            setLocation1(my_location);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
