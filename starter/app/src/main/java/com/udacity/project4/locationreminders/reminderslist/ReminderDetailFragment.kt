package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.databinding.ActivityReminderDescriptionBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivityArgs

class ReminderDetailFragment(override val _viewModel: BaseViewModel) : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = ActivityReminderDescriptionBinding.inflate(inflater)
        val arg = ReminderDescriptionActivityArgs.fromBundle(arguments!!).selectedReminderItem

        binding.reminderDataItem = arg

        return super.onCreateView(inflater, container, savedInstanceState)

    }
}