package com.udacity.project4

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragment
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
//Note that to use Koin in an integrated test we had to create our own class seperate from the MyApp class
class RemindersActivityTest :
    AutoCloseKoinTest() { // Extended Koin Test - embed autoclose @after method to close Koin after every test

    @get:Rule
    val intentsTestRule = IntentsTestRule(AuthenticationActivity::class.java)
//@get:Rule var rule: ActivityScenarioRule<*>? = ActivityScenarioRule(AuthenticationActivity::class.java)
    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        //we register an IdlingResource class seperately for databinding since Espresso uses a different mechanism for
        //databinding (Choreographer class)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


//    TODO: add End to End testing to the app
@LargeTest
@Test
fun editTask() = runBlocking {
/*    scenario.onActivity { activity ->
    startActivity(ApplicationProvider.getApplicationContext(),(activity.intent),Bundle())}//, Bundle()))*/

        // Set initial state.

        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)


        //So espresso knows which activity to monitor
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withText("Login")).perform(click())
        intended(toPackage("com.google.android.gms"))

        //onView(withId(R.id.refreshLayout)).perform()
        //onView(withText("Sign In")).check(matches(isDisplayed()))

    //need to delay because of auto-sign in?
    //delay(3000)
    repository.saveReminder(ReminderDTO("TITLE1", "DESCRIPTION", "LOCATION", 2.0, 5.0))
    val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

    val navController = mock(NavController::class.java)

    scenario.onFragment {
        Navigation.setViewNavController(it.view!!, navController)
    }

    onView(withText("TITLE1")).check(matches(isDisplayed()))
    delay(3000)
    onView(withId(R.id.addReminderFAB)).check(matches(isDisplayed()))
    onView(withId(R.id.addReminderFAB)).perform(click())

    verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder()) // actionReminderListFragmentToSaveReminderFragment())

        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        onView(withId(R.id.reminderTitle)).perform(replaceText("TITLE1"))
        onView(withId(R.id.reminderDescription)).perform(setTextInTextView("Description"))
        onView(withId(R.id.coordinates)).check(matches(withText("33.842342, -118.1523526")))
        onView(withId(R.id.selectedLocation)).perform(click())

    verify(navController).navigate(
        SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())


        //TODO: https://stackoverflow.com/questions/33382344/espresso-test-click-x-y-coordinates
        //onView(withId(R.id.map)).perform(clickIn(2, 3))
        //onView(withId(R.id.save_reminder_layout)).check(matches(withText("TITLE1")))

    activityScenario.close()
}

    @Test
    fun endToEndContinued()
    {
        val navController = mock(NavController::class.java)

    }


        //source: https://stackoverflow.com/questions/22177590/click-by-bounds-coordinates/22798043#22798043
        fun clickIn(x: Int, y: Int): ViewAction {
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
                return CoreMatchers.allOf(isDisplayed(), isAssignableFrom(TextView::class.java))
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
