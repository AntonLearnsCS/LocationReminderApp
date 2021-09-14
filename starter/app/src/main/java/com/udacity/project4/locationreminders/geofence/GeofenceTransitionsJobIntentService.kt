package com.udacity.project4.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

//Recall: "CoroutineScope" determines the lifecycle of coroutines
class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {
/*
    Users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 */
    private var fenceId : String = ""
    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573
        //So we need to start a job that will monitor our location to ensure that we are either still inside
        // or have left the geofence. We do this in the background since we don't know when the user will leave the area
        //TODO: call this to start the JobIntentService to handle the geofencing transition events i.e exit or enter geofence
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    /*
    Use enqueueWork(Context, Class, int, Intent) to enqueue new work to be dispatched to and handled by your service.
    It will be executed in onHandleWork(Intent).
     */

    //onHandleWorks is called automatically once enqueueWork() is called

    override fun onHandleWork(intent: Intent) {
        //TODO: handle the geofencing transition events and
        // send a notification to the user when he enters the geofence area
        //TODO call @sendNotification

        /*
        After we receive an Intent Broadcast, we need to retrieve the reminder info from our local database,
        which is a potentially long-running task and therefore has to be performed and handled in a background service.
         */

        //You can call GeofencingEvent.fromIntent(android.content.Intent) to get the transition type, geofences
        // that triggered this intent and the location that triggered the geofence transition.
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {

            Timber.i("" + geofencingEvent.errorCode)
            return
        }
        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            //remove geofence
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.v("test", applicationContext.getString(R.string.geofence_entered))

             fenceId = when {
                geofencingEvent.triggeringGeofences.isNotEmpty() ->
                    geofencingEvent.triggeringGeofences[0].requestId
                else -> {
                    Log.e("test", "No Geofence Trigger Found! Abort mission!")
                    return
                }
            }

            // triggeringGeofences - Returns a list of geofences that triggered this geofence transition alert.
            val numberOfTriggers = geofencingEvent.triggeringGeofences.size
            println("num triggers: " + numberOfTriggers)
            var i = 0
            while (i < numberOfTriggers)
            {
                sendNotification(geofencingEvent.triggeringGeofences[i])
                i++
            }
        }
    }

    //TODO: get the request id of the current geofence
    private fun sendNotification(triggeringGeofences: Geofence) {
        println("notification sent")
        val requestId = triggeringGeofences.requestId

        //Get the local repository instance
        val remindersLocalRepository: ReminderDataSource by inject()
//        Interaction to the repository has to be through a coroutine scope
        CoroutineScope(coroutineContext).launch(SupervisorJob()) {
            //get the reminder with the request id
            val result = remindersLocalRepository.getReminder(requestId)
            if (result is Result.Success<ReminderDTO>) {
                val reminderDTO = result.data
                //send a notification to the user with the reminder details
                sendNotification(
                    this@GeofenceTransitionsJobIntentService, ReminderDataItem(
                        reminderDTO.title,
                        reminderDTO.description,
                        reminderDTO.location,
                        reminderDTO.latitude,
                        reminderDTO.longitude,
                        reminderDTO.id
                    )
                )
            }
        }
    }

}