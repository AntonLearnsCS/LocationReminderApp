package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavGraph
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
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.android.dx.rop.cst.CstArray
import com.udacity.project4.FakeLocalRepository
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.ServiceLocator
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.not
import org.junit.After
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

    @get:Rule
    val intentsTestRule = IntentsTestRule(RemindersActivity::class.java)

    val remindersList = MutableLiveData<List<ReminderDataItem>>()
    private lateinit var reminderDataItem: ReminderDataItem
    private lateinit var reminderDTO: Result<ReminderDTO>
    private lateinit var stubDTO : ReminderDataItem
    private lateinit var reminderZ_id : String
    private lateinit var realRepo : ReminderDataSource
    val mRepo by inject<ReminderDataSource>()
    private lateinit var mViewModel: RemindersListViewModel
    //by inject<RemindersListViewModel>()

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
    //Note: @Before annotation are run before each test, so we clean the repository before each test
    @Before
    fun init()
    {
        Intents.init()
        ServiceLocator.resetRepository()
        //Note: We set ServiceLocator.provideTaskRepository() to a variable instead of calling saveReminder on
        // ServiceLocator.taskRepository.saveReminder() b/c "ServiceLocator.provideTaskRepository()" returns an instance
        //of a repository, so if we don't set that to anything then we never created a repository
        //As such, don't use: ServiceLocator.provideTasksRepository(ApplicationProvider.getApplicationContext()) and instead
        //use: ((ApplicationProvider.getApplicationContext()) as MyApp).taskRepository to get repository instance
        realRepo = ((ApplicationProvider.getApplicationContext()) as MyApp).taskRepository

        runBlocking {
            val Z = ReminderDTO("TitleZ","DescriptionZ","LocationZ",6.0,7.0)
            reminderZ_id = Z.id
            realRepo.saveReminder(Z)
            realRepo.saveReminder(ReminderDTO("TitleM","DescriptionM","LocationM",8.0,9.0))
            realRepo.saveReminder(ReminderDTO("TitleQ","DescriptionZ","LocationZ",10.0,11.0))
        }
        mViewModel  = RemindersListViewModel(ApplicationProvider.getApplicationContext(),realRepo)
    }
    @After
    fun After()
    {
        Intents.release()
    }

    @Test
    fun recyclerView_saveReminder_UpdateUI()
    {
        //Given - A real repository
        //For fake repository use: ServiceLocator.tasksRepository = FakeLocalRepository

        //Note: runBlocking is different from synchronized b/c synchronized ensures that only one thread can access a function
        // or code while runBlocking ensures that all suspend functions are completed before signaling the test execution as completed


        //When - Launching ListFragment, you don't actually need to launch this since we are not modifying the view
        //val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //Then - Items in repository is displayed
        //"withText" is the Matcher to be passed into "hasItem()"
        onView(withId(R.id.reminderssRecyclerView)).check(matches(hasItem(hasDescendant(withText("TitleZ")))))
    }
    //can be used to test notification
    @Test
    fun saveReminderFragment_saveReminder_PendingIntentCalled()
    {
        //Given - The reminderListFragment

        val scenario = launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

        val navController = mock(NavController::class.java)
        //When - Selecting on a task and clicking finished task
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //verify(navController.navigate(ReminderListFragmentDirections.))
        onView(withId(R.id.saveReminder)).perform(click())

        intended(toPackage("com.udacity.project4.geofence.GeofenceBroadcastReceiver"))

        //verify(navController.navigate(ReminderListFragmentDirections.actionReminderListFragmentToReminderDescriptionActivity()))
        //Then - the selected task should be gone from ReminderListFragment
    }

    @Test
    fun ReminderListFragment_ClickItem_NavigateDetailFragment()
    {
        //Given - ReminderListFragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val mNavController = mock(NavController::class.java)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, mNavController)
        }
        //When - Clicked on itemView
       // onView(withId(R.id.reminderssRecyclerView)).perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(hasDescendant(withText("TitleZ")), click()))

        //TODO: Why is this viewModle returning null when I have initialized the values in @Before init()?
        //val titleZ = mViewModel.remindersList.value?.get(0)

        //Then - Navigate to ReminderDescriptionActivity
        onView(withId(R.id.reminderssRecyclerView)).check(matches((hasItem(hasDescendant(withText("TitleZ"))))))
           //TODO: Pass parameter value here

        onView(withId(R.id.reminderssRecyclerView))
            .perform(RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("TitleZ")), click()))

        verify(mNavController).navigate(ReminderListFragmentDirections.actionReminderListFragmentToReminderDescriptionActivity(
            returnReminderDataItemFromDb(reminderZ_id)))
        //check if a view exists
        //onView(withId(R.id.taskTitle)).check(matches(isDisplayed()))

        //onView(withId(R.layout.reminder_description_fragment)).check(matches(withText("TitleZ")))
    }

    fun returnReminderDataItemFromDb(id : String) : ReminderDataItem =
        runBlocking {
            //mViewModel.loadReminders()
            var reminderDataItem : ReminderDataItem = ReminderDataItem("Title","Description","Location",1.0,2.0)
            //Place inside blocking b/c: https://knowledge.udacity.com/questions/686016
            val reminderDTO = realRepo.getReminder(id)


            when (reminderDTO) {
                is Result.Success<*> -> {

                    //TODO: How to prevent error "Smart cast to 'Result.Success<*>' is impossible, because 'reminderDTO' is a mutable
                    // property that could have been changed by this time"

                    reminderDataItem = convertToReminderItem(reminderDTO.data as ReminderDTO)
                }
                is Result.Error ->
                    println("Error")
            }
            return@runBlocking reminderDataItem
        }

    fun convertToReminderItem(reminderDTO: ReminderDTO) : ReminderDataItem
    {
        return ReminderDataItem(
            reminderDTO.title,
            reminderDTO.description,
            reminderDTO.location,
            reminderDTO.latitude,
            reminderDTO.longitude,
            reminderDTO.id
        )
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