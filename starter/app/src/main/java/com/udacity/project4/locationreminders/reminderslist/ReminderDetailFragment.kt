package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
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
import com.udacity.project4.locationreminders.savereminder.selectreminderlocation.DetailFragmentViewModel
import org.koin.android.ext.android.inject
import timber.log.Timber

class ReminderDetailFragment() : BaseFragment() {
    override val _viewModel: RemindersListViewModel by inject()
    private lateinit var binding: ReminderDescriptionFragmentBinding
    private lateinit var passedInReminderItem : ReminderDataItem
    private val viewModel: DetailFragmentViewModel by lazy {
        ViewModelProvider(this).get(DetailFragmentViewModel::class.java)
    }    //val args = ReminderDescriptionActivityArgs.fromBundle(arguments!!).ReminderDataItem
    //val args = this.arguments

    val args : ReminderDescriptionActivityArgs by navArgs()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.reminder_description_fragment,container,false)
        //Timber.i("test",args.ReminderDataItem.title)

        /* when (args.ReminderDataItem)
        {
            null -> passedInReminderItem = args.ReminderDataItem

            else -> passedInReminderItem = ReminderDataItem("Title","Description","Location",2.0,3.0)
        }*/
       /* if (viewModel.reminderDataItem.value != null)
        {

            passedInReminderItem = viewModel.reminderDataItem.value!!//args.ReminderDataItem
        }
        else
        {
            passedInReminderItem = args.ReminderDataItem!!
        }*/
        //TODO: Unable to retrieve argument from sendNotification() since the extra of the intent is not recognize as a
        // safe-args argument
        passedInReminderItem = args.ReminderDataItem

        //val args = intent.getSerializableExtra(EXTRA_ReminderDataItem) as ReminderDataItem?
        binding.reminderDataItem = passedInReminderItem
        //TODO: Add the implementation of the reminder details
        //get allow user to navigate to DescriptionActivity
        /*val arg = ReminderDescriptionActivity.fromBundle(arguments!!).selectedReminderItem
            Timber.i(arg.description)
            binding.reminderDataItem = arg*/

        binding.finishedTask.setOnClickListener {
            //return to the ReminderActivity
            //Note: "ApplicationProvider.getApplicationContext()" gets the context for the whole app.
            // Here, we just need the context of the fragment, doing so results in the error:
            //java.lang.IllegalStateException: No instrumentation registered! Must run under a registering instrumentation.
            val intent = Intent(context, RemindersActivity::class.java)
            intent.putExtra("finishedTask", passedInReminderItem?.id)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.reminderDataItem.value = null
    }
}