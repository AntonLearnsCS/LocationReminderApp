package com.udacity.project4.locationreminders.reminderslist

import android.content.Intent
import android.database.Observable
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.data.DataBufferObserver
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup
import com.udacity.project4.utils.wrapEspressoIdlingResource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.*

class ReminderListFragment : BaseFragment() {
    private lateinit var adapter : RemindersListAdapter
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by inject()
    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )

        binding.viewModel = _viewModel
        //binding.setLifecycleOwner { requireActivity().lifecycle }
        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        //val arg = arguments?.getString("finishedTask")

       /* if (_viewModel.idFound.value != null)
        {
            Timber.i("finishedTaskTest")
            _viewModel.removeTaskFromList()
        }
*/
        //setOnRefreshListener - Classes that wish to be notified when the swipe gesture correctly
        // triggers a refresh should implement this interface.
        binding.refreshLayout.setOnRefreshListener { wrapEspressoIdlingResource {  _viewModel.loadReminders() }}

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }


      //TODO: Need to make callback parameter observable so that it can update selectedReminder automatically
        adapter = RemindersListAdapter{
        //The value of the expression parameter in ListAdapter is updated automatically, not sure why...
            _viewModel.selectedReminder.value = it
            //not registering values below on detail activity
            _viewModel.selectedReminder.value!!.latitude = 2.0
            _viewModel.selectedReminder.value!!.longitude = 3.0
        }

        // Observe the navigateToSelectedProperty LiveData and Navigate when it isn't null
        // After navigating, call displayPropertyDetailsComplete() so that the ViewModel is ready
        // for another navigation event.
        _viewModel.selectedReminder.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                // Must find the NavController from the Fragment
                /*val intent = Intent(context,ReminderDescriptionActivity::class.java)
                intent.putExtra("EXTRA_ReminderDataItem",_viewModel.selectedReminder.value)
                startActivity(intent)*/
                //Navigation.findNavController(view).navigate(R.id.ReminderDescriptionActivity)

                Timber.i("selected Reminder: " + _viewModel.selectedReminder.value?.title)

                /*val intent = Intent(context,ReminderDescriptionActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("ReminderDataItem",it)
                intent.putExtras(bundle)*/

                val intent = Intent(context,ReminderDescriptionActivity::class.java)
                intent.putExtra("EXTRA_ReminderDataItem",it)
                startActivity(intent)

                _viewModel.setSelectedReminderToNull()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        wrapEspressoIdlingResource {  _viewModel.loadReminders()}
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the
        //Q:How does posting a value for a generic mutable live data cause navigation to the SaveReminder fragment
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
         adapter = RemindersListAdapter {
            _viewModel.selectedReminder.value = it
        }

        //setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
//                TODO: add the logout implementation
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

}
