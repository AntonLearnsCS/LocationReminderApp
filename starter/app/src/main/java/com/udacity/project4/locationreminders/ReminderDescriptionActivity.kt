package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import com.google.android.gms.location.GeofencingClient
import com.udacity.project4.R
//import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import timber.log.Timber

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
//Note: We have to designate an activity as the detail screen since we cannot navigate to a fragment from a pending intent of the
//notification
class ReminderDescriptionActivity : AppCompatActivity() {
    //source: https://developer.android.com/guide/navigation/navigation-migrate#add
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_description)

        //TODO: Why am I receiving error saying that NavController is not set when I am setting the navGraph for
        // the activity here?
        Navigation.findNavController(findViewById(R.id.activity_reminder_description)).setGraph(
            R.navigation.nav_graph_detail,intent.extras
        )
        //NavController manages app navigation within a NavHost.
  /*      Navigation.findNavController(this, R.id.activity_reminder_description)
            .setGraph(R.navigation.nav_graph_detail, intent.extras)*/
    }

    /*
    Logic:
    1) https://developer.android.com/guide/navigation/navigation-migrate#add
    2) https://developer.android.com/reference/kotlin/androidx/navigation/package-summary#(android.app.Activity).findNavController(kotlin.Int)
    3) https://developer.android.com/reference/kotlin/androidx/navigation/Navigation
     */
}
