package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity.Companion.TAG
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit


const val GEOFENCE_RADIUS_IN_METERS = 100000f

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
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var contxt: Context


    //Get the view model this time as a single to be shared with the another fragment, note the "override" tag
    //Note: We don't use "override val _viewModel: SaveReminderViewModel = get<SaveReminderViewModel>()"
    //because we are setting up our code in a fragment, if it was in an activity it would be allowed
    //https://stackoverflow.com/questions/53332832/unresolved-reference-none-of-the-following-candidates-is-applicable-because-of
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSaveReminderBinding
    private var geofenceList = mutableListOf<Geofence>()
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    private var latLng: LatLng? = LatLng(33.0,-118.1)
    private lateinit var geofencingClient: GeofencingClient
    private val runningQOrLater : Boolean = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private lateinit var reminderDataItem : ReminderDataItem
    private var intent = Intent()

    private val geofencePendingIntent: PendingIntent by lazy {
         intent = Intent(contxt, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        //intent.putExtra()
        PendingIntent.getBroadcast(contxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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

        //Q: Culprit?
        //references the fragment instead of activity
        binding.lifecycleOwner = (contxt as Activity) as LifecycleOwner

        binding.viewModel = _viewModel

        geofencingClient = LocationServices.getGeofencingClient(contxt)
        //TODO: Strange, pressing add button after save button calls onCreateView
        Timber.i("testingNull" + _viewModel.reminderTitle.value)

        //observe locationSingle variable
        Timber.i("locationSingle: " + _viewModel.locationSingle.value?.get(0)?.locality + " Coordinates: " + _viewModel.latLng.value?.latitude
         + ", " + _viewModel.latLng.value?.longitude)

        return binding.root
    }

    /*
     override fun onStart() {
        super.onStart()
        checkPermissionsAndStartGeofencing()
    }

    /*
 *  When we get the result from asking the user to turn on device location, we call
 *  checkDeviceLocationSettingsAndStartGeofence again to make sure it's actually on, but
 *  we don't resolve the check to keep the user from seeing an endless loop.
 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            // We don't rely on the result code, but just check the location setting again
            checkDeviceLocationSettingsAndStartGeofence(false)
        }
    }

    /*
     *  When the user clicks on the notification, this method will be called, letting us know that
     *  the geofence has been triggered, and it's time to move to the next one in the treasure
     *  hunt.
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val extras = intent?.extras
        if(extras != null){
            if(extras.containsKey(GeofencingConstants.EXTRA_GEOFENCE_INDEX)){
                viewModel.updateHint(extras.getInt(GeofencingConstants.EXTRA_GEOFENCE_INDEX))
                checkPermissionsAndStartGeofencing()
            }
        }
    }
     */
    //onAttach is a callback that is called when the fragment is attached to its host activity
    //"context" refers to the Activity that the fragment is attached to
    //Note: I was receiving the "Not attached to an activity" error b/c I the activity had not been created by the time
    //the fragment was created and initialized. This is why we use "contxt" instead of "requireContext()" since
    // "requireContext" will not have yet been initialized and is defaulted to "IllegalStateException"
    //Source: https://stackoverflow.com/questions/33742646/what-causes-a-fragment-to-get-detached-from-an-activity
    override fun onAttach(context: Context) {
        super.onAttach(context)
        contxt = context
    }

    /*
    According to Fragment lifecycle in Android OS, you cannot get the Activity associated with the fragment in the onCreateView,
    because the Activity with which the Fragment is associated will not be created at that stage.
     */

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStart() {
        super.onStart()
        binding.selectLocation.setOnClickListener {
            //Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
            //findNavController().popBackStack()
        }
        /* reminderDataItem = ReminderDataItem(_viewModel.reminderTitle.value,_viewModel.reminderDescription.value,
             _viewModel.reminderSelectedLocationStr,_viewModel.latitude.value,_viewModel.longitude.value)*/

        binding.saveReminder.setOnClickListener {
            //two-way data binding
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr
            latLng = _viewModel.latLng.value
            //no id for clicked location b/c ReminderDataItem will automatically generate one for us, id only for geofence
            reminderDataItem = ReminderDataItem(title,description,location,latLng?.latitude,latLng?.longitude)

            intent.putExtra("reminderDataItem", reminderDataItem)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            if (_viewModel.validateAndSaveReminder(reminderDataItem))
            {
                Timber.tag("test").i("test","testing")
                println("Passed validate and latLng: " + _viewModel.latLng.value?.latitude)
                checkDeviceLocationSettingsAndStartGeofence()
                //_viewModel.navigationCommand.value =
                //NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
                //findNavController().navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
                //TODO: Why if I pop the backstack here then onDestroy is called before "checkDeviceLocationSettingsAndStartGeofence()"
                // is finished? This is evident in latLng being reverted to null value.
                //findNavController().popBackStack()
                /*_viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())*/
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

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkDeviceLocationSettingsAndStartGeofence(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        println("checkDeviceLocationBeforeSettingsCheck0: " + isDetached)

        val settingsClient = LocationServices.getSettingsClient(contxt)
        println("checkDeviceLocationBeforeSettingsCheck1: " + isDetached)

        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())
        println("checkDeviceLocationBeforeSettingsCheck2: " + isDetached)

        locationSettingsResponseTask.addOnFailureListener { exception ->
            println("LocationSettingsResponseOnFailure")
            if (exception is ResolvableApiException && resolve){

                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        contxt as Activity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                Timber.i("Error getting location settings resolution:" + sendEx.message)
                //Log.d(TAG, "Error geting location settings resolution: " + sendEx.message)
                }
            }
            else {
                Snackbar.make(
                    binding.saveReminderLayout,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettingsAndStartGeofence()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if ( it.isSuccessful && !isDetached) {
                println("success on location settings response task: " + isDetached)
                Timber.i("Success")
                addGeofenceForClue()
            }
        }
    }
    //call only once permission is granted
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addGeofenceForClue() {
        println("addGeofence latLng: " + latLng?.latitude)

        // Build the Geofence Object
        val geofence = latLng?.latitude?.let {
            latLng?.longitude?.let { it1 ->
                Geofence.Builder()
                    // Set the request ID, string to identify the geofence.
                    .setRequestId(reminderDataItem.id)//_viewModel.latLng.value?.latitude.toString())
                    // Set the circular region of this geofence.
                    .setCircularRegion(
                        it,
                        it1,
                        GEOFENCE_RADIUS_IN_METERS
                    )
                    // Set the expiration duration of the geofence. This geofence gets
                    // automatically removed after this period of time.
                    .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            }
        }

        //Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        println("AddGeofence Detached? : "  + isDetached)

        //TODO: Why is checkSelfPermission failing here when it was approved in SelectLocationFragment?
        if ((ActivityCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
                    ) || !is_Q_Or_Lower()
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return
            //Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show()
            //TODO: Why is onDestroy() being called?
            println("First try is no permission")
            if (runningQOrLater)
            {
                ActivityCompat.requestPermissions(
                    contxt as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    REQUEST_LOCATION_PERMISSION
                )
            }
            else {
                ActivityCompat.requestPermissions(
                    contxt as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
        else {

            Toast.makeText(contxt,"Permission Granted",Toast.LENGTH_SHORT).show()

            //to add a geofence, you add the actual geofence location (geofenceRequest) as well as where you want the
            //activity to start once the geofence is triggered (geofencePendingIntent), which in our case is BroadcastReceiver
            println("Detached? : " + isDetached)
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    // Geofences added.
                    //Toast.makeText(requireActivity(), "Geofence Added", Toast.LENGTH_SHORT).show()
                    println("geofence added succesfully")
                    //findNavController().navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
                    //Log.e("Add Geofence", geofenceList[counter].requestId)
                    counter++

                    // Tell the viewmodel that we've reached the end of the game and
                    // activated the last "geofence" --- by removing the Geofence.

                    //viewModel.geofenceActivated()
                }
                addOnFailureListener {
                    // Failed to add geofences.
                    Toast.makeText(
                        contxt, R.string.geofences_not_added,
                        Toast.LENGTH_SHORT
                    ).show()
                    if ((it.message != null)) {
                        Log.w(TAG, it.message!!)
                    }
                }
            }

        }
    }
    fun is_Q_Or_Lower() : Boolean
    {
        if (runningQOrLater && ActivityCompat.checkSelfPermission(
                contxt, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        )
            return false
        else
            return true
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        println("Permission Result: ResultsRequest")
        Toast.makeText(contxt,"ResultsRequest", Toast.LENGTH_SHORT).show()
            if (is_Q_Or_Lower()) {
                if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                ) {
                    //Toast.makeText(requireContext(),"Q>=SuccessRequest", Toast.LENGTH_SHORT).show()
                    checkDeviceLocationSettingsAndStartGeofence()
                }
            }
            else
            {
                if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[1] == PackageManager.PERMISSION_GRANTED
                            && (grantResults[2] == PackageManager.PERMISSION_GRANTED))
                ) {
                    //Toast.makeText(requireContext(),"Q>=SuccessRequest", Toast.LENGTH_SHORT).show()
                    checkDeviceLocationSettingsAndStartGeofence()
                }
            }
    }

    //call after user enters geofence
    //Can be used to remove current geofences pending intent before sending new pending intent
    //Q: Is that necessary here?
    private fun removeGeofences() {
       /* if (!foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }*/
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences removed
                //Log.d(TAG, getString(R.string.geofences_removed))
                Toast.makeText(contxt,"Geofences removed", Toast.LENGTH_SHORT)
                    .show()
            }
            addOnFailureListener {
                // Failed to remove geofences
                Log.d(TAG, "Geofences not removed")
            }
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "RemindersActivity.savereminder.action.ACTION_GEOFENCE_EVENT"
    }



    override fun onDestroy() {
        super.onDestroy()
        println("Destroyed")
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

    override fun onDestroyView() {
        super.onDestroyView()
        println("Destroyed")
    }
}



private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1
