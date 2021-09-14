package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

class DetailFragmentViewModel : ViewModel() {

    val reminderDataItem = MutableLiveData<ReminderDataItem>()
}