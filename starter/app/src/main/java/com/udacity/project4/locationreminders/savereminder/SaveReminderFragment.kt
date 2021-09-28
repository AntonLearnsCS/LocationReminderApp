package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
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
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber


const val GEOFENCE_RADIUS_IN_METERS = 3200f

class SaveReminderFragment : BaseFragment() {
    private var counter = 0
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var contxt: Context

    //Get the view model this time as a single to be shared with the another fragment, note the "override" tag
    //Note: We don't use "override val _viewModel: SaveReminderViewModel = get<SaveReminderViewModel>()"
    //because we are setting up our code in a fragment, if it was in an activity it would be allowed
    //https://stackoverflow.com/questions/53332832/unresolved-reference-none-of-the-following-candidates-is-applicable-because-of
    override val _viewModel: SaveReminderViewModel by inject()
    //alternatively:
    //override val _viewModel by sharedViewModel<SaveReminderViewModel>()
    private lateinit var binding: FragmentSaveReminderBinding

    private var latLng: LatLng? = LatLng(33.0, -118.1)
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderDataItem : ReminderDataItem
    private var intent = Intent()

    private val geofencePendingIntent: PendingIntent by lazy {
         intent = Intent(contxt, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        //intent.putExtra()
        PendingIntent.getBroadcast(contxt, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private lateinit var requestLocationSetting : ActivityResultLauncher<IntentSenderRequest>
    private lateinit var permissionCallback : ActivityResultLauncher<Array<String>>

    @RequiresApi(Build.VERSION_CODES.Q)
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

        //TODO: Is it possible to pass in the permissionRequest callback here?
        requestLocationSetting  = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                checkDeviceLocationSettingsAndStartGeofence()
            }
            else
            {
                //source: https://stackoverflow.com/questions/30729312/how-to-dismiss-a-snackbar-using-its-own-action-button
                val mSnackbar = Snackbar.make(
                    binding.saveReminderLayout,
                    R.string.location_tracker_needed, Snackbar.LENGTH_LONG
                )
                mSnackbar.setAction("Dismiss"){mSnackbar.dismiss()}
                    mSnackbar.show()
                Log.i("Test", "location setting denied access")
            }
        }


        val test = ActivityResultContracts.RequestMultiplePermissions()
        //TODO: Receiving Type Mismatch error in defining permissionCallback when
        // following: https://developer.android.com/training/permissions/requesting#allow-system-manage-request-code
            permissionCallback = registerForActivityResult(test) { permissions: Map<String, Boolean> ->
                if(permissions.containsValue(true))
                {
                    checkDeviceLocationSettingsAndStartGeofence()
                    Log.i("test", "permission granted contract")
                }
                else
                {
                    val mSnackbar = Snackbar.make(
                        binding.saveReminderLayout,
                        R.string.permission_denied_explanation, Snackbar.LENGTH_LONG
                    )
                    mSnackbar.setAction("dismiss"){mSnackbar.dismiss()}
                    mSnackbar.show()

                    Log.i("test", "permission not granted contract")
                }
            }
        return binding.root
    }

    //onAttach is a callback that is called when the fragment is attached to its host activity
    //"context" refers to the Activity that the fragment is attached to
    //Note: I was receiving the "Not attached to an activity" error b/c the activity had not been created by the time
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
        }



        binding.saveReminder.setOnClickListener {
            //two-way data binding
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.cityNameForTwoWayBinding.value
            latLng = _viewModel.latLng.value
            //no id for clicked location b/c ReminderDataItem will automatically generate one for us, id only for geofence
            reminderDataItem = ReminderDataItem(
                title, description, location, latLng?.latitude,
                latLng?.longitude
            )

            intent.putExtra("reminderDataItem", reminderDataItem)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
                if(_viewModel.validateEnteredData(reminderDataItem))
                checkDeviceLocationSettingsAndStartGeofence()
                else
                    Toast.makeText(contxt, "Missing information", Toast.LENGTH_SHORT).show()


                //TODO If I include navigation from here to reminderListFragment then save button persist
                //findNavController().navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
                //TODO: Why if I pop the backstack here then onDestroy is called before "checkDeviceLocationSettingsAndStartGeofence()"
                // is finished? This is evident in latLng being reverted to null value.
                //findNavController().popBackStack()
                /*_viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())*/
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

        val settingsClient = LocationServices.getSettingsClient(contxt)

        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    //"exception" is defined in terms of "locationSettingsResponseTask". exception.resolution a placeholder for a pendingIntent
                        //source: https://knowledge.udacity.com/questions/650170#650189
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    requestLocationSetting.launch(intentSenderRequest)


                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    //exception.startResolutionForResult(contxt as Activity, REQUEST_TURN_DEVICE_LOCATION_ON)
                }
                catch (sendEx: IntentSender.SendIntentException) {
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
                addGeofenceForClue()
            }
        }
    }



    //call only once permission is granted
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun addGeofenceForClue() {
    //no need for check on running Q or later since
        // "The background location permission is needed in SaveReminderFragment since we want to kick off a Geofence." - reviewer
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
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
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

        //TODO: Why is checkSelfPermission failing here when it was approved in SelectLocationFragment?
        if ((ActivityCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
                    )
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
                //deprecated, use "registerForActivityResult()" instead
                /*ActivityCompat.requestPermissions(
                    contxt as Activity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    REQUEST_LOCATION_PERMISSION
                )*/

            val permissionObject = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

            permissionCallback.launch(permissionObject)
        }
        else {
            //Toast.makeText(contxt,"Permission Granted",Toast.LENGTH_SHORT).show()

            //to add a geofence, you add the actual geofence location (geofenceRequest) as well as where you want the
            //activity to start once the geofence is triggered (geofencePendingIntent), which in our case is BroadcastReceiver
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    _viewModel.saveReminder(reminderDataItem)
                Toast.makeText(contxt, "Succesfully added geofence", Toast.LENGTH_SHORT).show()
                    Log.i("test", "added geofence")
                    //navigate back only once geofence is added
                    val intent = Intent(requireContext(), RemindersActivity::class.java)
                    val bundle = Bundle()
                    bundle.putSerializable("ReminderDataItem", reminderDataItem)
                    intent.putExtras(bundle)
                    _viewModel.cityNameForTwoWayBinding.value = "City"
                    startActivity(intent)
                }
                addOnFailureListener {
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

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "RemindersActivity.savereminder.action.ACTION_GEOFENCE_EVENT"
    }

    override fun onDestroy() {
        super.onDestroy()
        println("Destroyed")
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i("requestCalled", "onRequestPermissionResult called!")
    }
}
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

