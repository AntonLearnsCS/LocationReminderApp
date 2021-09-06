package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.databinding.ReminderDescriptionFragmentBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.ReminderDescriptionActivityArgs
//import com.udacity.project4.locationreminders.ReminderDescriptionActivityArgs
import com.udacity.project4.locationreminders.RemindersActivity
import org.koin.android.ext.android.inject
import timber.log.Timber

class ReminderDetailFragment() : BaseFragment() {
    override val _viewModel: BaseViewModel by inject()
    private lateinit var binding: ReminderDescriptionFragmentBinding

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.reminder_description_fragment,container,false)

        Timber.i("test",args.ReminderDataItem.title)
        val passedInReminderItem = args.ReminderDataItem //intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem?
        binding.reminderDataItem = passedInReminderItem
        //TODO: Add the implementation of the reminder details
        //get allow user to navigate to DescriptionActivity
        /*val arg = ReminderDescriptionActivity.fromBundle(arguments!!).selectedReminderItem
            Timber.i(arg.description)
            binding.reminderDataItem = arg*/

        binding.finishedTask.setOnClickListener{
            //return to the ReminderActivity
            var intent = Intent(ApplicationProvider.getApplicationContext(), RemindersActivity::class.java)
            intent.putExtra("finishedTask", passedInReminderItem.id)
            startActivity(intent)
        }
        return binding.root
    }
}