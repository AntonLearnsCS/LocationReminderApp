package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.FakeLocalRepository
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.not
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    @get: Rule
    val mInstantTaskExecutorRule = InstantTaskExecutorRule()

    val mRepo by inject<ReminderDataSource>()
//    TODO: test the navigation of the fragments.
//    TODO: test the displayed data on the UI.
//    TODO: add testing for the error messages.

    //Needs coordinates
    /*@Test
    fun addReminder_ClickReminder_NavigateToDetail() = runBlocking {

        //Given - The ReminderList fragment and a mock NavController; added a Reminder
        mRepo.saveReminder(ReminderDTO("Title", "Description", "Location", 2.0, 3.0))

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            onView(withId(R.id.reminderTitle)).perform(setTextInTextView("Title"))//.perform(replaceText("Title"))
            onView(withId(R.id.reminderDescription)).perform(setTextInTextView("Description"))//.perform(replaceText("Description"))
            Navigation.setViewNavController(it.view!!, navController)
        }
            //Need to get coordinates to pass verification and save to Database, SaveReminderFragment does not coordinates
        //by default

        //WHEN - Click on the first list item; uses Espresso
        onView(withId(R.id.saveReminder)).perform(click())

        //Then - Navigate back to ReminderListFragment; uses Espresso
        verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment()
        )
    }*/

    @Test
    fun SaveReminderFragment_LocationClick_NavigateToMap()
    {
        //Given - SaveReminderFragment with NavController
        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //When - Clicked Location button navigates to SelectLocationFragment
        onView(withId(R.id.selectLocation)).perform(click())

        //Then - Navigate to SelectLocationFragment
        verify(navController).navigate(
            SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment()
        )
    }

    @Before
    fun init()
    {
        //mRepo
    }
    @Test
    fun recyclerView_saveReminder_UpdateUI()
    {
        //Given - Fake repository
        //ServiceLocator.tasksRepository = FakeLocalRepository
        ServiceLocator.provideTasksRepository(ApplicationProvider.getApplicationContext())

        runBlocking {
        ServiceLocator.tasksRepository?.saveReminder(ReminderDTO("TitleM","DescriptionM","LocationM",8.0,9.0))
        }
        //When - Launching ListFragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //Then - Items in repository is displayed
        //"withText" is the Matcher to be passed into "hasItem()"
        onView(withId(R.id.reminderssRecyclerView)).check(matches(hasItem(hasDescendant(withText("Title")))))
    }

    //Source: https://stackoverflow.com/questions/53288986/android-espresso-check-if-text-doesnt-exist-in-recyclerview
    fun hasItem(matcher: Matcher<View?>): Matcher<View?> {
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has item: ")
                matcher.describeTo(description)
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                val adapter = view.adapter
                for (position in 0 until adapter!!.itemCount) {
                    val type = adapter.getItemViewType(position)
                    val holder = adapter.createViewHolder(view, type)
                    adapter.onBindViewHolder(holder, position)
                    if (matcher.matches(holder.itemView)) {
                        return true
                    }
                }
                return false
            }
        }
    }
    //Source: https://stackoverflow.com/questions/32846738/android-testing-how-to-change-text-of-a-textview-using-espresso
    fun setTextInTextView(value: String?): ViewAction? {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(TextView::class.java))
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