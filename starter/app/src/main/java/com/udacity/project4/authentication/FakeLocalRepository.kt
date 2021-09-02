package com.udacity.project4.authentication

import androidx.lifecycle.MutableLiveData
//import com.android.dx.rop.cst.CstArray
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
//import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext

object FakeLocalRepository : ReminderDataSource {
    /*private val remindersDao: RemindersDao
    private val SERVICE_LATENCY_IN_MILLIS = 2000L
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO*/
    private var REMINDER_SERVICE_DATA = LinkedHashMap<String, ReminderDTO>(2)

    init {
        runBlocking{
            saveReminder(ReminderDTO("Title","Description","Location",2.0,3.0))
            saveReminder(ReminderDTO("Title1","Description1","Location1",4.0,5.0))
            saveReminder(ReminderDTO("Title2","Description2","Location2",6.0,7.0))
        }
    }

    private val observableReminders = MutableLiveData<kotlin.Result<List<ReminderDTO>>>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {

        return Result.Success((REMINDER_SERVICE_DATA.values).toList())
        /*= withContext(ioDispatcher) {
            return@withContext try {
                Result.Success(remindersDao.getReminders())
            } catch (ex: Exception) {
                Result.Error(ex.localizedMessage)
            }*/
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        REMINDER_SERVICE_DATA[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        REMINDER_SERVICE_DATA[id]?.let {
            return Result.Success(it)
        }
        return Result.Error("Could not find Reminder")
    }

    override suspend fun deleteAllReminders() {
        REMINDER_SERVICE_DATA.clear()
    }

    override suspend fun deleteTaskReminder(reminderId: String) {
        REMINDER_SERVICE_DATA.remove(reminderId)
    }
}