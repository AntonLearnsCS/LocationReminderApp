package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.room.Room


/**
 * Singleton class that is used to create a reminder db
 */
object LocalDB {

    /**
     * static method that creates a reminder class and returns the DAO of the reminder
     */
    fun createRemindersDao(context: Context): RemindersDao {
        return Room.databaseBuilder(
            context.applicationContext,
            RemindersDatabase::class.java, "locationReminders.db"
        ).build().reminderDao()

        //we call .reminderDao() so that we don't have to call "abstract fun reminderDao(): RemindersDao" whenever we
        //want to access DAO methods when referencing a database instance
    }
}