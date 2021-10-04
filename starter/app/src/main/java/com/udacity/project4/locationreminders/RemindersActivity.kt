package com.udacity.project4.locationreminders

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.udacity.project4.R
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

//import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

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
        println("ReminderActivity Created")
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
}
