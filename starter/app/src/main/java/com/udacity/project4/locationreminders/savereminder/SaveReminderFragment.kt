package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.map
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity.Companion.TAG
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.getUniqueId
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


const val GEOFENCE_RADIUS_IN_METERS = 100f

//following structure from DevByte domain -> DTO conversion
//create a dataclass
data class ReminderContainer(val reminder: List<ReminderDataItem>)

fun ReminderDataItem.asDatabaseModel(): ReminderDTO {
   // val nonLive : ReminderDataItem? = reminder.value
    return ReminderDTO(
            title = this.title,
            description = this.description,
            location = this.location,
            latitude = this.latitude,
            longitude = this.longitude,
            id = this.id)
}


class SaveReminderFragment : BaseFragment() {
    private var counter = 0
    //Get the view model this time as a single to be shared with the another fragment, note the "override" tag
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private var geofenceList = arrayListOf<Geofence>()
    private lateinit var dataSource: ReminderDataSource
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    private lateinit var geofencingClient: GeofencingClient
    //private val runningQOrLater : Boolean = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var reminderDataItem : ReminderDataItem
    private var intent = Intent()
    private val geofencePendingIntent: PendingIntent by lazy {
         intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_save_reminder,
            container,
            false
        )

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        //TODO: Strange, pressing add button after save button calls onCreateView
        Timber.i("testingNull" + _viewModel.reminderTitle.value)

        //observe locationSingle variable
        Timber.i("locationSingle: " + _viewModel.locationSingle.value?.get(0)?.locality + " Coordinates: " + _viewModel.latLng.value?.latitude
         + ", " + _viewModel.latLng.value?.longitude)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = requireActivity()
        //Timber.tag("coord").i( _viewModel.latLng.value?.latitude + _viewModel.latLng.value?.longitude)
        println("SaveReminder" + _viewModel.latLng.value?.latitude + ", " + _viewModel.latLng.value?.longitude)
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }
        reminderDataItem = ReminderDataItem(_viewModel.reminderTitle.value,_viewModel.reminderDescription.value,
            _viewModel.reminderSelectedLocationStr,_viewModel.latitude.value,_viewModel.longitude.value)

        binding.saveReminder.setOnClickListener {
            //two-way data binding
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            //no id for clicked location b/c ReminderDataItem will automatically generate one for us, id only for geofence
            reminderDataItem = ReminderDataItem(title,description,location,latitude,longitude)

            intent.putExtra("reminderDataItem", reminderDataItem)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            if(_viewModel.latLng.value != null)
            {
                Timber.i("viewModelNotNull")
                checkDeviceLocationSettingsAndStartGeofence()
            }

            if (_viewModel.validateAndSaveReminder(reminderDataItem))
            {
                Timber.i("testValidate")
                //findNavController().navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
            //findNavController().popBackStack()
             _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
            }
            else
            {
                Toast.makeText(context,"Missing title/description",Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */

    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        requireActivity(),
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                Timber.i("Error getting location settings resolution:" + sendEx.message)
                //Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.saveReminderLayout,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }
        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful ) {
                Timber.i("Success")
                addGeofenceForClue()
            }
        }
    }
    //call only once permission is granted
    private fun addGeofenceForClue() {
        val id = getUniqueId()
        //Build the geofence using the geofence builder
        _viewModel.latLng.value?.let {
            Geofence.Builder()
                .setRequestId(_viewModel.reminderSelectedLocationStr + _viewModel.latLng.value!!.latitude.toString()) //so we can reference the geofences built
                .setCircularRegion(
                    it.latitude,
                    _viewModel.latLng.value!!.longitude,
                    GEOFENCE_RADIUS_IN_METERS
                )
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()
        }?.let { geofenceList.add(it) }

        //Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofenceList[counter])
            .build()

        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return
            Timber.i("CheckSelfNotPassed")
        }
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences added.
                Toast.makeText(
                    requireActivity(), "Geofence Added",
                    Toast.LENGTH_SHORT
                )
                    .show()
                Log.e("Add Geofence", geofenceList[counter].requestId)

                counter++

                // Tell the viewmodel that we've reached the end of the game and
                // activated the last "geofence" --- by removing the Geofence.

            //viewModel.geofenceActivated()
            }
            addOnFailureListener {
                // Failed to add geofences.
                Toast.makeText(
                    requireActivity(), R.string.geofences_not_added,
                    Toast.LENGTH_SHORT
                ).show()
                if ((it.message != null)) {
                    Log.w(TAG, it.message!!)
                }
            }
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "RemindersActivity.savereminder.action.ACTION_GEOFENCE_EVENT"
    }



    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()

        //TODO: Remove Geofence after user clicks notification button
        /*geofencingClient?.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                // ...
            }
            addOnFailureListener {
                // Failed to remove geofences
                // ...
            }
        }*/
    }
}



private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
