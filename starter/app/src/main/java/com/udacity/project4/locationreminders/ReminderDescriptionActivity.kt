package com.udacity.project4.locationreminders

//import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.databinding.ReminderDescriptionFragmentBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
//Note: We have to designate an activity as the detail screen since we cannot navigate to a fragment from a pending intent of the
//notification
class ReminderDescriptionActivity : AppCompatActivity() {

    private lateinit var binding : ActivityReminderDescriptionBinding

    //source: https://developer.android.com/guide/navigation/navigation-migrate#add
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder_description)

        binding = ActivityReminderDescriptionBinding.inflate(layoutInflater)
        //val bundleItem = intent.getSerializableExtra("ReminderDataItem") as ReminderDataItem
        val intent = this.intent
        val bundle = intent.extras
        val bundleItem = bundle?.getSerializable("ReminderDataItem") as ReminderDataItem
        binding.reminderDataItem = bundleItem

        binding.lifecycleOwner = this

        binding.finishedTask.setOnClickListener {
            val intentFinished = Intent(this, RemindersActivity::class.java)
            intentFinished.putExtra("finishedTask", bundleItem.id)
            startActivity(intentFinished)
        }
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
