package com.udacity.project4.locationreminders.savereminder

import android.os.Bundle
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class SaveReminderFragmentTest {

    @get: Rule
    val mInstantTaskExecutorRule = InstantTaskExecutorRule()

@Test
fun saveReminder_SaveButtonClicked_SaveButtonNotVisible()
{
    //Given - SaveReminder fragment
    val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
    //maybe need to populate title, description and coordinates before being able to save
    onView(withId(R.id.reminderTitle)).perform(setTextInTextView("Title"))
    onView(withId(R.id.reminderDescription)).perform(setTextInTextView("Description"))
    onView(withId(R.id.selectedLocation)).perform(setTextInTextView("City"))
    onView(withId(R.id.coordinates)).perform(setTextInTextView("2.0,3.0"))

    val navController = mock(NavController::class.java)
    scenario.onFragment {
        Navigation.setViewNavController(it.view!!, navController)
    }
    //When - User clicks save button
    onView(withId(R.id.saveReminder)).perform(click())

    //Then - SaveButton is not visible
    onView(withId(R.id.saveReminder)).check(matches(not(isDisplayed())))
    //verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
}

    @Test
    fun SaveReminderFragment_Geocoder_UpdateLocation(): Unit = runBlocking{
        //given - fragment
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(),R.style.AppTheme)
        //val scenario = FragmentScenario.launchInContainer(SaveReminderFragment::class.java, Bundle(),R.style.AppTheme,null)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!,navController)
        }

        delay(3000)
        onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText("TITLE2"))
        onView(withId(R.id.reminderDescription)).perform(setTextInTextView("Description"))
        //when - new location is clicked
        onView(withId(R.id.selectLocation)).check(matches(isDisplayed()))
        onView(withId(R.id.selectLocation)).perform(click())
        delay(3000)

        verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        //TODO: https://stackoverflow.com/questions/33382344/espresso-test-click-x-y-coordinates
        onView(withId(R.id.selectLocation)).perform(clickIn(33.940829653849526, -118.13559036701918))
        //then - location name changes
        onView(withId(R.id.selectedLocation)).check(matches((withText("Lakewood"))))
    }

    fun clickIn(x: Double, y: Double): ViewAction {
        return GeneralClickAction(
            Tap.LONG,
            CoordinatesProvider { view ->
                val screenPos = IntArray(2)
                view?.getLocationOnScreen(screenPos)

                val screenX = (screenPos[0] + x).toFloat()
                val screenY = (screenPos[1] + y).toFloat()

                floatArrayOf(screenX, screenY)
            },
            Press.PINPOINT,
            InputDevice.SOURCE_ANY,
            MotionEvent.BUTTON_PRIMARY
        )
    }
    //Source: https://stackoverflow.com/questions/32846738/android-testing-how-to-change-text-of-a-textview-using-espresso
    fun setTextInTextView(value: String?): ViewAction? {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return CoreMatchers.allOf(
                    ViewMatchers.isDisplayed(),
                    ViewMatchers.isAssignableFrom(TextView::class.java)
                )
            }

            override fun perform(uiController: UiController?, view: View) {
                (view as TextView).text = value
            }

            override fun getDescription(): String {
                return "replace text"
            }
        }
    }
}