package com.udacity.project4.locationreminders.geofence

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.TimeFormatException
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.sendNotification
import timber.log.Timber
import java.io.Serializable
import java.util.logging.ErrorManager

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {

//TODO: implement the onReceive method to receive the geofencing events at the background
override fun onReceive(context: Context, intent: Intent) {

    //TODO: Remove Geofence after user clicks notification button or will it expire once enter is triggered?

            //source: https://stackoverflow.com/questions/47593205/how-to-pass-custom-object-via-intent-in-kotlin
            Toast.makeText(context,"Broadcast Received",Toast.LENGTH_LONG).show()
            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }
}


