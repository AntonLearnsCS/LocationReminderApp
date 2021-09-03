package com.udacity.project4.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.navArgs
import com.google.android.gms.location.GeofencingClient
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import timber.log.Timber

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
//Note: We have to designate an activity as the detail screen since we cannot navigate to a fragment from a pending intent of the
//notification
class ReminderDescriptionActivity : AppCompatActivity() {

    val args: ReminderDescriptionActivityArgs by navArgs()

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description)
        //TOOD: Checkout: https://developer.android.com/guide/navigation/navigation-migrate#add
        //create two nav graphs: https://stackoverflow.com/questions/62214514/navigate-from-one-activity-to-another-with-navigation-component
        val passedInReminderItem = args.ReminderDataItem//intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem?
        binding.reminderDataItem = passedInReminderItem
        //TODO: Add the implementation of the reminder details
    //get allow user to navigate to DescriptionActivity
    /*val arg = ReminderDescriptionActivity.fromBundle(arguments!!).selectedReminderItem
        Timber.i(arg.description)
        binding.reminderDataItem = arg*/
        binding.finishedTask.setOnClickListener{
            //return to the ReminderActivity
            var intent = Intent(applicationContext,RemindersActivity::class.java)
            intent.putExtra("finishedTask",passedInReminderItem?.id)
            startActivity(intent)
        }
    }
}
