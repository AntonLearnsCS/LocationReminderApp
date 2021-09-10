package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Application
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private val TAG = "SelectLocationFragment"
    private val REQUEST_LOCATION_PERMISSION = 1

    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var map : GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_select_location,container,false)

        //DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = requireActivity()

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location
        //onLocationSelected()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //The activity that contains the SupportMapFragment must implement the OnMapReadyCallback
        // interface and that interface's onMapReady() method.
        //val mapFragment : MapFragment = childFragmentManager.findFragmentById(R.id.map) as MapFragment

        val activity = getActivity()
        if (activity != null && isAdded)
        {
            Timber.i("testingNull")
            //Q: Issue caused by navigating away from current fragment to MapFragment? This causes "Fragment not attach to Activity"
            //error
            //childFragmentManager - Return a private FragmentManager for placing and managing Fragments inside of this Fragment.
            mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            //mapFragment.setTargetFragment(this,1)
        }
        //Tried methods to get MapFragment:

        //SupportFragmentManager.newInstance().childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //binding.map as MapFragment
        //.findFragmentById(R.id.map) as SupportMapFragment
        //getMapAsync sets a callback object which will be triggered when the GoogleMap instance is ready to be used.
        //so "getMapAsync" will pass in the "googleMap" parameter in "onMapReady()"

            //getMapAsync is called and executed from the Main thread
            mapFragment.getMapAsync(this)


        return binding.root
    }

/*    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        Timber.i("testingNullFragmentListener")
    }*/

    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
            map.setOnMapLongClickListener { latLang ->
                //_viewModel.locationMutable.value?.latLng = latLang
                findNavController().popBackStack()
            }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap
        }
        onLocationSelected()

        // Add a marker in Lakewood/Long Beach CA and move the camera, note that coordinates have a wide range, which is why decimals
        //can dictate the difference between two cities
        val latitude = 33.8447593
        val longitude = -118.1480706
        val zoomLevel = 15f

        val homeLatLng = LatLng(latitude, longitude)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng, zoomLevel))
        map.addMarker(MarkerOptions().position(homeLatLng))

        val overlaySize = 100f
        val androidOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.fui_ic_github_white_24dp))
            .position(homeLatLng, overlaySize)

        //BitmapDescriptorFactory is used to create a definition of a Bitmap image, used for marker icons and ground overlays.

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
        //map.addGroundOverlay(androidOverlay)
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(), //A object represents a specific geographical, political, or cultural region
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            /*
            As you can see, the problem isn't with MutableLiveData, but with your ViewModel. Since it's a SharedViewModel,
            you need to have it's LifeCycleOwner set to either an Activity or to your Application class. This way, if you
            reuse that LifeCycleOwner with that ViewModel, the changes of your LiveData properties will be visible to your
             other Activities or Fragment
             Source: https://stackoverflow.com/questions/54871649/mutablelivedata-sets-value-but-getvalue-returns-null
             */

            _viewModel.latLng.value = latLng
            println("SelectLocation:" + latLng.latitude.toString() + ", " + latLng.longitude.toString())
            Timber.i("locationSingle: " + _viewModel.locationSingle.value?.get(0)?.locality + " Coordinates: " + _viewModel.latLng.value?.latitude
                    + ", " + _viewModel.latLng.value?.longitude)
            findNavController().popBackStack()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
        }
    }
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style //the customized style is downloaded as a JSON from: https://mapstyle.withgoogle.com/
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        }
        catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isPermissionGranted() : Boolean {
        if (runningQOrLater)
        {
            return ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        else
        {
            return ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED  && ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun enableMyLocation() {
        if (!isPermissionGranted()) {
            if (runningQOrLater) {
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return
                    Timber.i("RequestFailed1")
                }
                else
                {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                    Timber.i("RequestSuccess1")
                }
            }
            else {
                if (ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    //return
                    Timber.i("RequestFailed2")

                }
                else
                {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                    Timber.i("RequestSuccess2")
                }
            }
            map.setMyLocationEnabled(true)
            _viewModel.successfuPermissionGranted.value = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (runningQOrLater)
            {
                if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[1] == PackageManager.PERMISSION_GRANTED) && (grantResults[2] == PackageManager.PERMISSION_GRANTED))
                    {
                    enableMyLocation()
                    }
            }
            else
            {
                enableMyLocation()
                /*if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    enableMyLocation()
                }*/
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
        R.id.normal_map -> {
            true
        }
        R.id.hybrid_map -> {
            true
        }
        R.id.satellite_map -> {
            true
        }
        R.id.terrain_map -> {
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.locationSingle.value = null
    }
}
private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
private const val TAG = "HuntMainActivity"
private const val LOCATION_PERMISSION_INDEX = 0
private const val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

/*

 */