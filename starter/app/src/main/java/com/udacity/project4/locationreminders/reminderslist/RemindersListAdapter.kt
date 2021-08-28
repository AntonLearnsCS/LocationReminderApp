package com.udacity.project4.locationreminders.reminderslist

import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.R
import com.udacity.project4.base.BaseRecyclerViewAdapter
import com.udacity.project4.base.DataBindingViewHolder
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import timber.log.Timber


//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}