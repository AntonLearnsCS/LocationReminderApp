package com.udacity.project4.locationreminders.savereminder

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap

class FakeRepository : ReminderDataSource {

    var tasksServiceData: LinkedHashMap<String, Task> = LinkedHashMap()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        TODO("Not yet implemented")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        withContext(ioDispatcher) {

            remindersDao.saveReminder(reminder)
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllReminders() {
        TODO("Not yet implemented")
    }
}