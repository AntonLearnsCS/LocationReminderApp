package com.udacity.project4.locationreminders.savereminder

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
        //When

        //Then
    }

}