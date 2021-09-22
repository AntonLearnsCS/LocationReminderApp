package com.udacity.project4.locationreminders

//import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
//import com.udacity.project4.databinding.ReminderDescriptionFragmentBinding
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

        //Note: Inflating an activity that has a binding view using the commented method directly below will not result in creation
        //of correct xml. Instead, use "DataBindingUtil.setContentView(...)"
        //binding = ActivityReminderDescriptionBinding.inflate(layoutInflater)
        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_reminder_description)
        //Alternatively, you can do:
       /* binding = com.udacity.project4.databinding.ActivityReminderDescriptionBinding.inflate(layoutInflater)
        val view : View = binding.root
        setContentView(view)*/
        val bundleItem = intent.getSerializableExtra("EXTRA_ReminderDataItem") as ReminderDataItem
        println("Title: " + bundleItem.title)
        /*val intent = this.intent
        val bundle = intent.extras
        val bundleItem = bundle?.getSerializable("EXTRA_ReminderDataItem") as ReminderDataItem*/

        binding.reminderDataItem = bundleItem

        binding.lifecycleOwner = this

        binding.finishedTask.setOnClickListener {
            Toast.makeText(this,"Removed",Toast.LENGTH_SHORT).show()
            val intentFinished = Intent(this, RemindersActivity::class.java)
            intentFinished.putExtra("finishedTask", bundleItem.id)
            startActivity(intentFinished)
        }
    //return binding.root
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
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
