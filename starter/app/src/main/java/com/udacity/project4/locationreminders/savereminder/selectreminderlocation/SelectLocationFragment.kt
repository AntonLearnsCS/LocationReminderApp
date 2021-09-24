package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Application
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
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
    private lateinit var contxt: Context
    private lateinit var geocoder: Geocoder
    private lateinit var list : List<Address>

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


        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contxt = context
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            map = googleMap
        }
        //onLocationSelected()

        // Add a marker in Lakewood/Long Beach CA and move the camera, note that coordinates have a wide range, which is why decimals
        //can dictate the difference between two cities
        val latitude = 33.8447593
        val longitude = -118.1480706
        val zoomLevel = 12f

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
            _viewModel.cityNameForTwoWayBinding.value = getLatLngAddress(latLng)?.locality
            _viewModel.latLng.value = latLng
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
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || (runningQOrLater && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED))
            {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            else
            {
                map.setMyLocationEnabled(true)
                println("Location: Enabled location successfully")
                _viewModel.successfuPermissionGranted.value = true
            }
        }
        else
        {
            if (runningQOrLater)
            {
                requestPermissions(arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_LOCATION_PERMISSION)
            }
            else {
                //https://stackoverflow.com/questions/32714787/android-m-permissions-onrequestpermissionsresult-not-being-called
                println("Requesting permission")
                requestPermissions(
                    arrayOf<String>(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_LOCATION_PERMISSION
                )
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isPermissionGranted() : Boolean {
        if (!runningQOrLater) {
            return ContextCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        else
        {
            return ContextCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                contxt,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}