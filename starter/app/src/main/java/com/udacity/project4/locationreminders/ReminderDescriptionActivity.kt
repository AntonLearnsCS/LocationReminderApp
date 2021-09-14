package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import com.google.android.gms.location.GeofencingClient
import com.udacity.project4.R
//import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.DetailFragmentViewModel
import timber.log.Timber

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
//Note: We have to designate an activity as the detail screen since we cannot navigate to a fragment from a pending intent of the
//notification
class ReminderDescriptionActivity : AppCompatActivity() {
    private val viewModel: DetailFragmentViewModel by lazy {
        ViewModelProvider(this).get(DetailFragmentViewModel::class.java)
    }

    //source: https://developer.android.com/guide/navigation/navigation-migrate#add
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_description)
        //Source: https://stackoverflow.com/questions/57682209/exception-view-does-not-have-a-navcontroller-set
            //val args : ReminderDescriptionActivityArgs by navArgs()
         val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_detail) as NavHostFragment

        //Pass activity destination args to a start destination fragment
        //To do this, you need a reference to the NavHostFragment and set that fragment's navController's graph

        //NavController manages app navigation within a NavHost.
        //The navGraph shows the relationship between fragments
        //Navigation flows and destinations are determined by the navigation graph owned by the controller
        //2) navHostFragment.navController.setGraph(R.navigation.nav_graph_detail, intent.extras)

        //You need to explicitly mention the NavHostFragment
        Navigation.findNavController(findViewById(R.id.nav_host_fragment_detail)).setGraph(
            R.navigation.nav_graph_detail,intent.extras)

        //NavController manages app navigation within a NavHost.
  /*      Navigation.findNavController(this, R.id.activity_reminder_description)
            .setGraph(R.navigation.nav_graph_detail, intent.extras)*/

        //An intent opens an activity and since we can't set the navGraph as above for notifications, we have to get the intent
        //extra here in the activity
/*
        if (intent.getSerializableExtra("EXTRA_ReminderDataItem") != null)
        {
            println("extra found")
            viewModel.reminderDataItem.value = intent.getSerializableExtra("EXTRA_ReminderDataItem") as ReminderDataItem
        }
        else
        {
            println("Extra not found")
            viewModel.reminderDataItem.value = args.ReminderDataItem
        }
*/


    }
    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        // receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }
    /*
    Logic:
    1) https://developer.android.com/guide/navigation/navigation-migrate#add
    2) https://developer.android.com/reference/kotlin/androidx/navigation/package-summary#(android.app.Activity).findNavController(kotlin.Int)
    3) https://developer.android.com/reference/kotlin/androidx/navigation/Navigation
     */
}
