package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : AutoCloseKoinTest() {

//TODO: Add testing implementation to the RemindersLocalRepository.kt
    //Creating own localDataSource/RoomDatabase
private lateinit var localDataSource : ReminderDataSource
private lateinit var database : RemindersDatabase

//Using Koin, inject a ReminderDataSource instead of a RemindersLocalRepository b/c RemindersLocalRepository
// is cast as a ReminderDataSource
// Source: https://knowledge.udacity.com/questions/647267
private val repo by inject<ReminderDataSource>()


@Test
fun saveTask_RetrieveTask() : Unit = runBlocking {

    //Given - A new DTO
    val myDTO = ReminderDTO("Title","Description","Location",2.0,3.0)

    //When - saved to local repo
    repo.saveReminder(myDTO)

    //Then - will return the saved DTO
    val savedDTO = repo.getReminder(myDTO.id)

    //"savedDTO" is wrapped in "Result"
    assertThat(savedDTO.succeeded,`is`(true))
    savedDTO as Result.Success
    assertThat(savedDTO.data.title, `is`("Title"))
    assertThat(savedDTO.data.description, `is`("Description"))
    assertThat(savedDTO.data.id, `is`(myDTO.id))

    /*when (savedDTO) {
        is Result.Success<*> -> {
            (savedDTO.data as List<*>).map { reminder ->
                //map the reminder data from the DB to the be ready to be displayed on the UI
                assertThat(myDTO.description,`is`(equals(reminder)))
            }
        }
    }*/
}
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { com.udacity.project4.locationreminders.data.local.RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            //androidLogger()
            androidContext(appContext)
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    /*
    runBlocking - runBlocking is a coroutine function. By not providing any context, it will get run on the main thread.
    Runs a new coroutine and blocks the current thread interruptible until its completion.

    We use runBlockingTest to make our results consistent so that we know all tasks are completed (synchronous)
     */
    /*@Before
    fun initDB()
    {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),RemindersDatabase::class.java)
            .allowMainThreadQueries() //allows you to query database from UI thread, don't do this for production code
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    } */

    /*@Test
    fun saveTask_RetrieveTask() : Unit = runBlocking {

        //Given - A new DTO
        val myDTO = ReminderDTO("Title","Description","Location",2.0,3.0)

        //When - saved to local repo
        localDataSource.saveReminder(myDTO)

        //Then - will return the saved DTO
        val savedDTO = localDataSource.getReminder(myDTO.id)

        //"savedDTO" is wrapped in "Result"
        assertThat(savedDTO.succeeded,`is`(true))
        savedDTO as Result.Success
        assertThat(savedDTO.data.title, `is`("Title"))
        assertThat(savedDTO.data.description, `is`("Description"))
        assertThat(savedDTO.data.id, `is`(myDTO.id))

        *//*when (savedDTO) {
            is Result.Success<*> -> {
                (savedDTO.data as List<*>).map { reminder ->
                    //map the reminder data from the DB to the be ready to be displayed on the UI
                    assertThat(myDTO.description,`is`(equals(reminder)))
                }
            }
        }*//*
    }*/
}