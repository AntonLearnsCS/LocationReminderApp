package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import timber.log.Timber

@Config(sdk = [Build.VERSION_CODES.Q])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //TODO: provide testing to the SaveReminderView and its live data objects
    //convention in naming testing functions: subjectUnderTest_actionOrInput_resultState
    @Test
    fun LatLng_UpdateValue_Updated()
    {
        //Given - A fresh viewModel
        val testRepo = FakeRepository()


        val viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),testRepo)
        //When we update the value of LatLng that has an initial value
        viewModel.latLng.value = LatLng(2.0,2.0)
        //viewModel.latLng.value = LatLng(4.0,4.0)
        //Then the new value should be displayed
        Timber.i("TestLat" + viewModel.latLng.value!!.latitude.toString())
    }

}