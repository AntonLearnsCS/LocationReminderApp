package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationListener
import com.udacity.project4.locationreminders.RemindersActivity
import java.util.*

class cityLocation() : LocationListener
{
    //Geocoder: https://stackoverflow.com/questions/39218891/how-to-get-current-locationstreet-city-etc-using-gps-in-android
    override fun onLocationChanged(p0: Location?) {
        //TODO: Create Geocoder
        //val gcd = Geocoder()
    }
}