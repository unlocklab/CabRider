
package com.example.cabbooking.rider;

import android.app.Application;
import android.content.res.Configuration;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;


public class AppTest extends Application
{
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

    }
}