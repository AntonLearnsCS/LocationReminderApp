package com.udacity.project4.locationreminders.savereminder

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.withContext
import java.util.LinkedHashMap

class FakeDataSource : ReminderDataSource {

    private var shouldReturnError: Boolean = false

    //Fake repository
    var tasksServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError)
        {
            return Result.Error("Could not find reminder",1)
        }
        //The "Success" will cast the resulting list as a Result of type "Success"
    return Result.Success(tasksServiceData.values.toList())
        }

    override suspend fun saveReminder(reminder: ReminderDTO) {

      tasksServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError)
        {
            return Result.Error("Could not find reminder id: $id",1)
        }
        return Result.Success(tasksServiceData.get(id)!!)
    }

    //TODO: implement
    /* if(tasksServiceData.containsKey(id))
        return Result.Success(tasksServiceData.get(id)!!)*/


    override suspend fun deleteAllReminders() {
        tasksServiceData.clear()
    }

    override suspend fun deleteTaskReminder(reminderId: String) {
        tasksServiceData.remove(reminderId)
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }
}