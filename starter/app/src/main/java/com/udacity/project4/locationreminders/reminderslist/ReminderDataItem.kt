package com.udacity.project4.locationreminders.reminderslist

import androidx.annotation.Nullable
import java.io.Serializable
import java.util.*

/**
 * data class acts as a data mapper between the DB and the UI
 */
data class ReminderDataItem(
    var title: String?,
    var description: String?,
    var location: String?,
    var latitude: Double?,
    var longitude: Double?,
    val id: String = UUID.randomUUID().toString() //generates random id
) : Serializable //This process of converting Non primitive types to primitives and sending across
// to other application over some communication channel is called as Serialization. This is so the Android OS can recognize
//the ReminderDataItem objects when processing it through system applications like NotificationManager.
// Serializable is use for passing object through Intent
