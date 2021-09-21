package com.udacity.project4

import android.app.Application
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
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
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.allOf
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
fun endToEndTest() = runBlocking {
/*    scenario.onActivity { activity ->
    startActivity(ApplicationProvider.getApplicationContext(),(activity.intent),Bundle())}//, Bundle()))*/

        // Set initial state.
        repository.saveReminder(ReminderDTO("TITLE1", "DESCRIPTIONq", "LOCATION", 2.0, 5.0))

        // Start up Tasks screen.
        val activityScenario = ActivityScenario.launch(AuthenticationActivity::class.java)

        //So espresso knows which activity to monitor
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withText("Login")).perform(click())
        //intended(toPackage("com.google.android.gms"))

        //onView(withId(R.id.refreshLayout)).perform()
        //onView(withText("Sign In")).check(matches(isDisplayed()))

    //need to delay because of auto-sign in
    //TODO: Uncomment the delay(3000) below to generate the "Activity Not Found" error
    delay(3000)

    //TODO: Do I need to make a new activityScenario here to tell Espresso to track RemindersActivity's databinding?
    val reminderScenario = ActivityScenario.launch(RemindersActivity::class.java)
    dataBindingIdlingResource.monitorActivity(reminderScenario)
    onView(withText("TITLE1")).check(matches(isDisplayed()))

    onView(withId(R.id.reminderssRecyclerView)).perform(
        RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
        hasDescendant(withText("TITLE1")), click()))

    delay(3000)
        onView(withText("DESCRIPTIONq")).check(matches(isDisplayed()))
        pressBack()
    /*
    //trying to use fragments to test navigation
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
*/

    onView(withId(R.id.addReminderFAB)).perform(click())

    onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        onView(withId(R.id.reminderTitle)).perform(replaceText("TITLE2"))
        onView(withId(R.id.reminderDescription)).perform(setTextInTextView("Description"))
        onView(withId(R.id.coordinates)).check(matches(withText("33.842342, -118.1523526")))
        onView(withId(R.id.selectedLocation)).perform(click())

    //verify(navController).navigate(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())

        //TODO: https://stackoverflow.com/questions/33382344/espresso-test-click-x-y-coordinates
        onView(withId(R.id.map)).perform(clickIn(37.7529, -122.4232))
        //onView(allOf(withId(R.id.save_reminder_layout)).check(matches(withText("TITLE1")))
    //Not sure why comment above does not work but below code does
    //source: https://developer.android.com/training/testing/espresso/basics
    onView(allOf(withId(R.id.save_reminder_layout), withText("TITLE1")))
    delay(3000)
    onView(withId(R.id.saveReminder)).perform(click())
    onView(allOf(withId(R.id.refreshLayout), withText("TITLE2")))

    //TODO: Try deleting pending intent before launching a new one:
    // https://stackoverflow.com/questions/13596422/android-notification-pendingintent-extras-null
    activityScenario.close()
    reminderScenario.close()
}



        //source: https://stackoverflow.com/questions/22177590/click-by-bounds-coordinates/22798043#22798043
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
