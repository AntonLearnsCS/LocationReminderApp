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
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.activity.result.IntentSenderRequest

import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
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
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.Exception


const val GEOFENCE_RADIUS_IN_METERS = 3200f

class SaveReminderFragment : BaseFragment(),EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {
    private var counter = 0
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var contxt: Context
    private lateinit var registerForActivityResult: ActivityResultLauncher<Intent>

    //Get the view model this time as a single to be shared with the another fragment, note the "override" tag
    //Note: We don't use "override val _viewModel: SaveReminderViewModel = get<SaveReminderViewModel>()"
    //because we are setting up our code in a fragment, if it was in an activity it would be allowed
    //https://stackoverflow.com/questions/53332832/unresolved-reference-none-of-the-following-candidates-is-applicable-because-of
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSaveReminderBinding
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)

    private var latLng: LatLng = LatLng(33.0,-118.1)
    private lateinit var geofencingClient: GeofencingClient
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
            latLng = _viewModel.latLng.value!!
            //no id for clicked location b/c ReminderDataItem will automatically generate one for us, id only for geofence
            reminderDataItem = ReminderDataItem(title,description,location, latLng.latitude,
                latLng.longitude
            )

            intent.putExtra("reminderDataItem", reminderDataItem)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db

                checkDeviceLocationSettingsAndStartGeofence()

                val intent = Intent(requireContext(),RemindersActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("ReminderDataItem",reminderDataItem)
                intent.putExtras(bundle)
                _viewModel.cityNameForTwoWayBinding.value = "City"
                startActivity(intent)
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
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(geofencePendingIntent).build()
                    startIntentSenderForResult(geofencePendingIntent.intentSender, REQUEST_TURN_DEVICE_LOCATION_ON, null, 0, 0, 0, null)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON)
        {
            Toast.makeText(contxt,"Location enabled",Toast.LENGTH_SHORT).show()
        }
        else
            Toast.makeText(contxt,"Location not enabled",Toast.LENGTH_SHORT).show()

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

            //Toast.makeText(contxt,"Permission Granted",Toast.LENGTH_SHORT).show()

            //to add a geofence, you add the actual geofence location (geofenceRequest) as well as where you want the
            //activity to start once the geofence is triggered (geofencePendingIntent), which in our case is BroadcastReceiver
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                addOnSuccessListener {
                    _viewModel.validateAndSaveReminder(reminderDataItem)
                Toast.makeText(contxt,"Succesfully added geofence",Toast.LENGTH_SHORT).show()
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

    //TODO: Why is this method still being called when I am running API 28 and Q is API 29?
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.

        //"0" means permission granted for grantResults
        println("OnRequestPermission called: " + grantResults.size + " , " + grantResults[0] +
                " , " + grantResults[1])

        println("Permissions: " + permissions[0] + ", " + permissions[1])
            if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.size > 0 && (grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) && (grantResults[1] == PackageManager.PERMISSION_GRANTED)
                && (grantResults[2] == PackageManager.PERMISSION_GRANTED)
            )
            {
                println("Location: Request permission successful")
                return
            }
        //try requesting permission again
        println("request permission failed")
            addGeofenceForClue()
        Toast.makeText(contxt,"Permission denied: Go to app settings to approve", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        println("Permission granted")
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

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

        Toast.makeText(contxt,"Permission Denied by User", Toast.LENGTH_SHORT).show()
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
        {
            AppSettingsDialog.Builder(this).build().show()
            Toast.makeText(contxt,"Enable location in app settings", Toast.LENGTH_SHORT).show()

        }
        else if (EasyPermissions.somePermissionDenied(requireActivity()))
        {
            Toast.makeText(contxt,"Some permission denied", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onRationaleAccepted(requestCode: Int) {
        TODO("Not yet implemented")
    }

    override fun onRationaleDenied(requestCode: Int) {
        TODO("Not yet implemented")
    }

}
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

