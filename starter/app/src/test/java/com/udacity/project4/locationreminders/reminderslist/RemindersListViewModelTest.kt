package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class RemindersListViewModelTest {
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var repository: FakeDataSource
    /*
    InstantTaskExecutorRule - A JUnit Test Rule that swaps the background executor used by the Architecture
     Components with a different one which executes each task synchronously.
     */
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init()
    {
        repository = FakeDataSource()
        //Given a view model with one data item
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),repository)

    }

    //TODO: provide testing to the RemindersListViewModel and its live data objects

    @Test
    fun ReminderListViewModel_DeleteItem_ListChanges() = runBlockingTest{
        //Given a view model with one data item
        val testReminderData = ReminderDataItem("title","description","location",2.0,3.0)
        repository.saveReminder(
             ReminderDTO(
                testReminderData.title,
                testReminderData.description,
                testReminderData.location,
                testReminderData.latitude,
                testReminderData.longitude,
                testReminderData.id
            )
        )
        viewModel.selectedReminder.value = testReminderData
        assertThat(viewModel.selectedReminder.value, `is`(testReminderData))
        //When data item is deleted
        viewModel.removeTaskFromList(testReminderData.id)
        //Then loaded list does not contain the deleted data item; also assert that toast message is shown
        viewModel.loadReminders()


        val list = viewModel.remindersList.value
        assertThat(list,`is`(emptyList()))
    }

    @Test
    fun ReminderViewModel_RetrieveData_DataNotAvailableErrorDisplayed() {

        // Make the repository return errors.
        repository.setReturnError(true)
        viewModel.loadReminders()

        // Then empty and error are true (which triggers an error message to be shown).
        Assert.assertThat(viewModel.empty, `is`(true))
        Assert.assertThat(viewModel.error, `is`(true))
    }

   /* @Test
    fun reminderListAdapter_CheckCallbackValue_NonNull()
    {
        //Given a ListAdapter
        val myAdapter = ListAdapter<>
    }*/
}