package com.udacity.project4.locationreminders.savereminder

import android.location.Address
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.testClass

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.robolectric.annotation.Config

/*
The warning about the Android SDK 10000 is more complicated - running tests on Android Q requires Java 9.
Instead of trying to configure Android Studio to use Java 9, we're keeping our target and compile SDK at 28.
 */
@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : KoinTest { //extend KoinTest to be able to use "by inject()"

    //Q: How can I inject a fake Repository using Koin
    //A: As it stands, this is an integrated test since Koin is providing the actual repository
    //val viewModel by inject<SaveReminderViewModel>()


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    //TODO: provide testing to the SaveReminderView and its live data objects
    //convention in naming testing functions: subjectUnderTest_actionOrInput_resultState

    @Test
    fun quickTest()
    {
        val testClass by inject<testClass>()
        testClass.testClass.testClassFun()
    }

    @Test
    fun SaveReminderViewModel_ValidateFunction_Pass() = runBlockingTest {
        //Given - A fresh viewModel
        val testRepo = FakeDataSource()

        //Robolectric allows us to get ApplicationContext from Unit test
        val viewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),testRepo)
        //When - Pass in a completed and incomplete ReminderDataItem
        val completeDataItem = ReminderDataItem("Title","Description","Location",1.0,2.0)
        val completeDataItem1 = ReminderDataItem("Title1","Description1","Location1",3.0,4.0)

        //Then - viewModel is true
        assertThat(viewModel.validateAndSaveReminder(completeDataItem), `is`(true))
        assertThat(viewModel.validateAndSaveReminder(completeDataItem1), `is`(true))

        //can't run logs b/c logs are Android features, not Junit features
        //Timber.i()
    //println("TestLat" + viewModel.latLng.value!!.latitude.toString())
    }
}