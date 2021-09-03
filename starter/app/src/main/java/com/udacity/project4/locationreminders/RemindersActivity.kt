package com.udacity.project4.locationreminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.authentication.FakeLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

//import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {
    private val REQUEST_LOCATION_PERMISSION = 1
    private val runningQOrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        //Sharing viewModel between activity and fragment for communication
        // https://developer.android.com/guide/fragments/communicate
        val _viewModel: RemindersListViewModel by viewModel()

        val arg = intent.getStringExtra("finishedTask")

        if (arg != null)
        {
        _viewModel.removeTaskFromList(arg)
        //_viewModel.idFound.value = arg
        }
        //ServiceLocator.tasksRepository = FakeLocalRepository
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val nav_host_fragment = findNavController(R.id.nav_host_fragment)
                nav_host_fragment.popBackStack()
                //(nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    //triggers request permission display
    /*
     ActivityCompat.requestPermissions(
       this@HuntMainActivity,
       permissionsArray,
       resultCode
   )
   //TODO: What is causing the warning below? The same warning appeared in Wanderer, which ran fine.
   //W/MainFragment: 10: SecurityException: Geofence usage requires ACCESS_FINE_LOCATION permission
     */
   /* @RequiresApi(Build.VERSION_CODES.Q)
    private fun isPermissionGranted() : Boolean {
        if (runningQOrLater)
        {
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
        else
        {
            return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED  && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            if (runningQOrLater) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
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
                        this,
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_LOCATION_PERMISSION
                    )
                    Timber.i("RequestSuccess1")
                }
            }
            else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
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
                        this,
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION),
                        REQUEST_LOCATION_PERMISSION

                    )
                    Timber.i("RequestSuccess2")
                }
            }
            //map.setMyLocationEnabled(true)
            //_viewModel.successfuPermissionGranted.value = true
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
                if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    enableMyLocation()
                }
            }
        }
    }*/
}
