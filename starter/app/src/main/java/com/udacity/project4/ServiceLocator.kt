package com.udacity.project4

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.runBlocking

//We use a ServiceLocator in order to return an instance of a repository
object ServiceLocator {

    private val lock = Any()
    private var database: RemindersDatabase? = null
    //volatile - meaning that writes to this field are immediately made visible to other threads
    // since multiple threads can request a repository. This is accomplished by: Volatile fields provide memory
    // visibility and guarantee that the value that is being read, comes from the main memory and not the cpu-cache
    @Volatile
    var tasksRepository: ReminderDataSource? = null
        @VisibleForTesting set
    //"@VisibleForTesting set" allows you to set the value for this variable, so we can set the
    //repository to a fake repository

    fun provideTasksRepository(context: Context): ReminderDataSource {
        //recall that synchronized means that only one thread can call this function at a time like a room with a key with only
        //person allowed in at one time. Here, "this", which is the ServiceLocator object acts as the key
        synchronized(this) {
            return tasksRepository ?: createTaskLocalDataSource(context)
        }
    }

   /* private fun createTasksRepository(context: Context): RemindersLocalRepository {
        val newRepo = RemindersLocalRepository(createTaskLocalDataSource(context))
        tasksRepository = newRepo
        return newRepo
    }*/

    private fun createTaskLocalDataSource(context: Context): RemindersLocalRepository {
        val database = database ?: createDataBase(context)
        return RemindersLocalRepository(database.reminderDao())
    }

    private fun createDataBase(context: Context): RemindersDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "Reminders.db"
        ).build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetRepository() {
        synchronized(lock) {
            runBlocking {
                tasksRepository?.deleteAllReminders()
            }
            // Clear all data to avoid test pollution.
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            tasksRepository = null
        }
    }
}
