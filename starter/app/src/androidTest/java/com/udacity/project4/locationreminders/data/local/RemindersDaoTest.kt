package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
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
//    TODO: Add testing implementation to the RemindersDao.kt

    //Note: If you want to test the DAO, you must first create an instance of the database since Room will generate the DAO object
    //from the database in which it is defined in.
    //private lateinit var localDataSource : ReminderDataSource
    private lateinit var database : RemindersDatabase

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val localDataSource by inject<ReminderDataSource>()

    @Before
    fun init() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    @After
    fun after() = database.close()

    @Test
    fun RemindersDatabase_InsertItem_GetById() = runBlockingTest {
        // GIVEN - Insert a ReminderDTO.
        val reminderDataItem = ReminderDTO("title", "description","Location",2.0,3.0)
        database.reminderDao().saveReminder(reminderDataItem)

        // WHEN - Get the ReminderDTO by id from the database.
        val loaded = database.reminderDao().getReminderById(reminderDataItem.id)

        // THEN - The loaded data contains the expected values.
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminderDataItem.id))
        assertThat(loaded.title, `is`(reminderDataItem.title))
        assertThat(loaded.description, `is`(reminderDataItem.description))
        assertThat(loaded.latitude, `is`(reminderDataItem.latitude))
        assertThat(loaded.longitude, `is`(reminderDataItem.longitude))
        assertThat(loaded.location, `is`(reminderDataItem.location))

    }

    @Test
    fun updateTaskAndGetById() {
        runBlockingTest {
            // 1. Insert a task into the DAO.
            val reminderItem = ReminderDTO("mTitle","mDescription","mLocation",2.0,4.0)
            database.reminderDao().saveReminder(reminderItem)
            // 2. Update the task by creating a new task with the same ID but different attributes.
            val newReminderItem = ReminderDTO("newTitle","newDescription","newLocation",3.0,5.0)
            newReminderItem.id = reminderItem.id
            database.reminderDao().saveReminder(newReminderItem)
            // 3. Check that when you get the task by its ID, it has the updated values.
            val updatedReminderItem = database.reminderDao().getReminderById(reminderItem.id)

            assertThat(updatedReminderItem?.title,`is`("newTitle"))
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