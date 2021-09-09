package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.ListAdapter
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.savereminder.FakeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest {

    /*
    InstantTaskExecutorRule - A JUnit Test Rule that swaps the background executor used by the Architecture
     Components with a different one which executes each task synchronously.
     */
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    /*@Test
    fun selectedReminder_Updated_changeValue()
    {
        val repoTest = FakeRepository()
        //Given a view model
        val reminderViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),repoTest)
        val testReminderData = ReminderDataItem("title","description","location",2.0,3.0)
        //When new value is passed in
        reminderViewModel.selectedReminder.value = testReminderData
        //Then live data is not null
        assertThat(reminderViewModel.selectedReminder.value,`is`(!equals(null)))
    }*/

   /* @Test
    fun reminderListAdapter_CheckCallbackValue_NonNull()
    {
        //Given a ListAdapter
        val myAdapter = ListAdapter<>
    }*/
}