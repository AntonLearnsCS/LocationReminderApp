package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.isPresent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch
import java.util.*

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) : BaseViewModel(
    app
) {

//, val dataSource: ReminderDataSource
    //idea of geocoder: https://stackoverflow.com/questions/59095837/convert-from-latlang-to-address-using-geocoding-not-working-android-kotlin
    /*val geocoder = Geocoder(this.app, Locale.ENGLISH)

    fun getLatLngAddress(LatLng: LatLng) : Address?
    {
             list =  geocoder.getFromLocation(LatLng.latitude, LatLng.longitude, 1)
            if (!list.isEmpty() && isPresent()) {
                println("locality: " + list[0].locality)
                return list[0]
            }
            else
                return null
    }
*/


    val latLng : MutableLiveData<LatLng> = MutableLiveData(LatLng(33.842342, -118.1523526))

   /* val locationSingle = MutableLiveData(latLng.value?.let { getLatLngAddress(it) })

    var reminderSelectedLocationStr : String? = if (locationSingle.value != null) {
        locationSingle.value?.postalCode }
    else null*/

    val cityNameForTwoWayBinding = MutableLiveData("City")

    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    //val reminderSelectedLocationStr : MutableLiveData<String> = MutableLiveData(locationSingle?.get(0).toString())


    val selectedPOI = MutableLiveData<PointOfInterest>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()


    val successfuPermissionGranted = MutableLiveData<Boolean>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        latLng.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) : Boolean {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
            return true
        }
        return false
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }
        //TODO uncomment after testing
        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        if (reminderData.latitude == 33.8 && reminderData.longitude == -118.1)
        {
            showSnackBarInt.value = R.string.missing_coordinates
            return false
        }
        return true
    }
}