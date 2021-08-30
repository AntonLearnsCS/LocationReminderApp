package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.Dispatchers

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest : AutoCloseKoinTest() {
//Not
//    TODO: Add testing implementation to the RemindersDao.kt

    //private lateinit var localDataSource : ReminderDataSource
    private lateinit var database : RemindersDatabase

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val localDataSource by inject<ReminderDataSource>()

    @Test
    fun LocalSourceSet_AddRemoveItem_Empty(): Unit = runBlocking {

        //Given - Adding an item to LocalSourceSet
        val myReminder = ReminderDTO("Title", "Description", "Location", 2.0, 3.0)
        localDataSource.saveReminder(myReminder)

        //When - Removing an item
        localDataSource.deleteTaskReminder(myReminder.id)
        //Then - That item should return null upon retrieval
        val returnItem = localDataSource.getReminder(myReminder.id)


        //assertThat(returnItem.d)
    }

    @Test
    fun updateTaskAndGetById() {
        runBlocking {
            // 1. Insert a task into the DAO.
            val task = ReminderDTO("Title", "Description", "Location", 2.0, 3.0)
            localDataSource.saveReminder(task)
            //database.taskDao().insertTask(task)
            // 2. Update the task by creating a new task with the same ID but different attributes.
            val newTask = ReminderDTO("Title1", "Description1", "Location1", 2.1, 3.1)
            newTask.id = task.id

            localDataSource.saveReminder(newTask)
            // 3. Check that when you get the task by its ID, it has the updated values.
            val updatedTask = localDataSource.getReminder(task.id)
            assertThat(updatedTask.succeeded,`is`(true))
            updatedTask as Result.Success
            assertThat(updatedTask.data.title,`is`("Title1"))
        }
    }

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
            single { RemindersLocalRepository(get()) as ReminderDataSource }
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
Testing uses Room.inMemoryDatabaseBuilder to create a Room DB instance.

Testing covers:

inserting and retrieving data using DAO.
predictable errors like data not found.
 */
    /*@Test
    fun LocalSourceSet_AddRemoveItem_Empty() = runBlocking {

    //Given - Adding an item to LocalSourceSet
    val myReminder = ReminderDTO("Title", "Description", "Location", 2.0, 3.0)
    localDataSource.saveReminder(myReminder)

    //When - Removing an item
    localDataSource.deleteTaskReminder(myReminder.id)
    //Then - That item should return null upon retrieval
    val returnItem = localDataSource.getReminder(myReminder.id)

    //Good, correct result
    assertThat(returnItem.succeeded,`is`(false))
}

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries() //allows you to query database from UI thread, don't do this for production code
            .build()

        localDataSource =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }*/
}