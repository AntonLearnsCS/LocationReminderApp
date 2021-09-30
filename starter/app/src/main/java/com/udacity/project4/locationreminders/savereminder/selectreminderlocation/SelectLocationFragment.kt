package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {
    private val TAG = "SelectLocationFragment"
    private lateinit var contxt: Context
    private lateinit var geocoder: Geocoder
    private lateinit var list : List<Address>
    private lateinit var permissionCallback : ActivityResultLauncher<Array<String>>
    private var latitude : Double = 33.8447593
    private var longitude : Double = -118.1480706
    private val zoomLevel = 12f
    private var defaultLocation = LatLng(latitude,longitude)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation : Task<Location>
    private val locationCallBack: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            val location: Location? = p0?.lastLocation
            if(location != null) {
                latitude = defaultLocation.latitude
                longitude = defaultLocation.longitude
            }
        }
    }
    override fun onStart() {
        super.onStart()
        geocoder = Geocoder(requireContext(), Locale.ENGLISH)
    }


    fun getLatLngAddress(LatLng: LatLng) : Address?
    {
        list =  geocoder.getFromLocation(LatLng.latitude, LatLng.longitude, 1)
        if (!list.isEmpty() && Geocoder.isPresent()) {
            println("locality: " + list[0].locality)
            return list[0]
        }
        else
            return null
    }

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var map : GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_select_location,
            container,
            false
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(contxt)

        binding.lifecycleOwner = requireActivity()

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        //The activity that contains the SupportMapFragment must implement the OnMapReadyCallback
        //interface and that interface's onMapReady() method.
        //val mapFragment : MapFragment = childFragmentManager.findFragmentById(R.id.map) as MapFragment

        val activity = getActivity()
        if (activity != null && isAdded)
        {
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

        val test = ActivityResultContracts.RequestMultiplePermissions()

        permissionCallback = registerForActivityResult(test) { permissions: Map<String, Boolean> ->
            if(permissions.containsValue(true))
            {
                getDeviceLocation()
                Log.i("test", "permission granted contract")
            }
            else
            {
                if((shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) == false
                            || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) == false))
                {
                    val mSnackbar = Snackbar.make(
                        binding.layout,
                        "Go to app settings to enable map location", Snackbar.LENGTH_SHORT
                    )

                    mSnackbar.setAction("dismiss"){mSnackbar.dismiss()}
                    mSnackbar.show()

                }
                val mSnackbar = Snackbar.make(
                    binding.layout,
                    "Enabling location moves map to your location", Snackbar.LENGTH_LONG
                )

                mSnackbar.setAction("dismiss"){mSnackbar.dismiss()}
                mSnackbar.show()

                Log.i("test", "permission not granted contract")
            }
        }

        enableLocation()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contxt = context
    }

    //https://stackoverflow.com/questions/43100365/how-to-refresh-a-google-map-manually
    override fun onResume() {
        super.onResume()
        //source: https://stackoverflow.com/questions/37618738/how-to-check-if-a-lateinit-variable-has-been-initialized
        if(this::map.isInitialized) {
            Log.i("test","map is initialized and onResume called")
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        defaultLocation.latitude,
                        defaultLocation.longitude
                    ), zoomLevel
                )
            )
            mapFragment.getMapAsync(this)
            Log.i("test",defaultLocation.latitude.toString())
        }
        else
            Log.i("test","map is not initialized")
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap
        }
        //onLocationSelected()
        if (defaultLocation.latitude.equals(33.8447593))
        {
            if (locationPermissionGranted())
            getDeviceLocation()
            else {
                if ((!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || !shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ))
                ) {
                    val mSnackbar = Snackbar.make(
                        binding.layout,
                        "Go to app settings to enable map location", Snackbar.LENGTH_LONG
                    )

                    mSnackbar.setAction("dismiss") { mSnackbar.dismiss() }
                    mSnackbar.show()

                }
            }
        }


        // Add a marker in Lakewood/Long Beach CA and move the camera, note that coordinates have a wide range, which is why decimals
        //can dictate the difference between two cities
        //updateLocationUI()

        //move camera to user's current location, if location is not turned on go to default location

        map.addMarker(MarkerOptions().position(defaultLocation))


        //BitmapDescriptorFactory is used to create a definition of a Bitmap image, used for marker icons and ground overlays.

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
    }

    fun enableLocation()
    {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
        {
            Log.i("test", "SelectLocation foreground permission enabled")
            return
        }
        else{
            Log.i("test", "SelectLocation foreground permission not yet enabled")

            val mArray = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
            permissionCallback.launch(mArray)
        }
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
            _viewModel.cityNameForTwoWayBinding.value = getLatLngAddress(latLng)?.locality
            _viewModel.latLng?.value = latLng
            println("SelectLocation: " + latLng.latitude.toString() + ", " + latLng.longitude.toString())
            //println("locationSingle: " + _viewModel.locationSingle.value?.locality + " Coordinates: " + _viewModel.latLng.value?.latitude
            //          + ", " + _viewModel.latLng.value?.longitude )
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
            poiMarker.showInfoWindow()
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

    fun locationPermissionGranted() : Boolean
    {
        if ((ActivityCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
                    )
        ){
            return true
        }
        else
            return false
    }

    //source: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial#kotlin_7
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */

        val fusedLocationProviderClient = FusedLocationProviderClient(contxt)
        var lastKnownLocation: Location
        try {
            if (locationPermissionGranted()) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful && task.result != null) {
                        //TODO: Why was task.result null?
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        defaultLocation =
                            LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation.latitude,
                                    lastKnownLocation.longitude
                                ), zoomLevel
                            )
                        )

                    }
                    else {
                        requestLocation()
                        Log.i("test", "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        /*map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, zoomLevel))
                        map?.uiSettings?.isMyLocationButtonEnabled = false*/
                    }
                }
            }
            else
            {
                Log.i("test", "Current location is null. Using defaults.")
                map?.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(defaultLocation, zoomLevel))
                map?.uiSettings?.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    //https://stackoverflow.com/questions/63223410/does-fusedlocationproviderclient-need-to-initialize-location-often-null
    private fun requestLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (ActivityCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                contxt,
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
            return
        }
        Log.i("test","requestLocation called")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallBack, Looper.myLooper())
        lastLocation = fusedLocationClient.lastLocation
        if (lastLocation.isSuccessful){
            defaultLocation = LatLng(lastLocation.result.latitude, lastLocation.result.longitude)
        }
        else
        {
            map.moveCamera(CameraUpdateFactory
                .newLatLngZoom(defaultLocation, zoomLevel))
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    // TODO: Change the map type based on the user's selection.

    override fun onOptionsItemSelected(item: MenuItem)  : Boolean = when(item.itemId){
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}