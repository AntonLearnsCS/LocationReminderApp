package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : KoinTest {

//TODO: Add testing implementation to the RemindersLocalRepository.kt
private lateinit var reminderDataSource : ReminderDataSource
        private val repo by inject<RemindersLocalRepository>()
    /*
    runBlocking - runBlocking is a coroutine function. By not providing any context, it will get run on the main thread.
    Runs a new coroutine and blocks the current thread interruptible until its completion.

    We use runBlockingTest to make our results consistent so that we know all tasks are completed (synchronous)
     */


    @Test
    fun saveTask_RetrieveTask() = runBlockingTest {

        //Given - A new DTO
        val myDTO = ReminderDTO("Title","Description","Location",2.0,3.0)

        //When - saved to local repo
        repo.saveReminder(myDTO)

        //Then - will return the saved DTO
        val savedDTO = repo.getReminder(myDTO.id)

        //"savedDTO" is wrapped in "Result"
        when (savedDTO) {
            is Result.Success<*> -> {
                (savedDTO.data as List<ReminderDTO>).map { reminder ->
                    //map the reminder data from the DB to the be ready to be displayed on the UI
                    assertThat(myDTO.description,`is`(equals(reminder.description)))
                }
            }
        }
    }
}