package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : AutoCloseKoinTest() {

//TODO: Add testing implementation to the RemindersLocalRepository.kt
private lateinit var database : RemindersDatabase

    @get: Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun init() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        //source for setTransactionExecutor: https://medium.com/@eyalg/testing-androidx-room-kotlin-coroutines-2d1faa3e674f
        //if we don't specify the default dispatchers, a different default dispatcher will be provided, which will create a different scope
        //that is beyond the ones created in the DAO functions
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).setTransactionExecutor(mainCoroutineRule.dispatcher.asExecutor())
            .setQueryExecutor(mainCoroutineRule.dispatcher.asExecutor())
            .allowMainThreadQueries().build()
    }
@Test
fun saveTask_RetrieveTask() : Unit = runBlocking {

    //Given - A new DTO
    val myDTO = ReminderDTO("Title","Description","Location",2.0,3.0)

    //When - saved to local repo
    database.reminderDao().saveReminder(myDTO)

    //Then - will return the saved DTO
    val savedDTO = repo.getReminder(myDTO.id)

    //"savedDTO" is wrapped in "Result"
    assertThat(savedDTO.succeeded,`is`(true))
    savedDTO as Result.Success
    assertThat(savedDTO.data.title, `is`("Title"))
    assertThat(savedDTO.data.description, `is`("Description"))
    assertThat(savedDTO.data.id, `is`(myDTO.id))

}

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

}