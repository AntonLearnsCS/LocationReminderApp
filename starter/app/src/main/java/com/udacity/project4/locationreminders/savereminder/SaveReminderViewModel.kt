package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.location.Address
import android.location.Geocoder
import androidx.core.content.ContentProviderCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.testClass
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) : BaseViewModel(app) {
//, val dataSource: ReminderDataSource

    //idea of geocoder: https://stackoverflow.com/questions/59095837/convert-from-latlang-to-address-using-geocoding-not-working-android-kotlin
    val geocoder = Geocoder(this.app)


    @Synchronized
    fun getSynchronizedLatLngAddress(LatLng : LatLng) : List<Address>
    {
       return geocoder.getFromLocation(LatLng.latitude, LatLng.longitude,1)
    }

    val latLng : MutableLiveData<LatLng> = MutableLiveData(LatLng(33.8,-118.1))
    //MutableLiveData<LatLng>().apply { postValue(LatLng(33.8,-118.1)) }
    //val initialLocation = latLng.value?.let { location(it) }
    //val locationMutable : MutableLiveData<location> = MutableLiveData(initialLocation)

    //TODO: Unchanging even though latLng changes value
    val locationSingle = MutableLiveData(latLng.value?.let { getSynchronizedLatLngAddress(it) })
        //latLng.value?.let { geocoder.getFromLocation(it.latitude, latLng.value!!.longitude,1) }

    val reminderSelectedLocationStr : String? = if (locationSingle.value != null || locationSingle.value?.size != 0) locationSingle.value?.get(0)?.locality
    else null

    val cityNameForTwoWayBinding = MutableLiveData(reminderSelectedLocationStr)

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
        if (reminderData.latitude == 33.8)
        {
            showSnackBarInt.value = R.string.missing_coordinates
            return false
        }
        return true
    }
}