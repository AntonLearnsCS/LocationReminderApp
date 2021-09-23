package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest : AutoCloseKoinTest() {

//TODO: Add testing implementation to the RemindersLocalRepository.kt


//Using Koin, inject a ReminderDataSource instead of a RemindersLocalRepository b/c RemindersLocalRepository
// is cast as a ReminderDataSource
// Source: https://knowledge.udacity.com/questions/647267
private val repo by inject<ReminderDataSource>()

    @Before
    fun init() {
        //localDataSource = FakeDataSource(localDataSource.toMutableList())

        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
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
            single { com.udacity.project4.locationreminders.data.local.RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            //androidLogger()
            androidContext(appContext)
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }
@Test
fun saveTask_RetrieveTask() : Unit = runBlocking {

    //Given - A new DTO
    val myDTO = ReminderDTO("Title","Description","Location",2.0,3.0)

    //When - saved to local repo
    repo.saveReminder(myDTO)

    //Then - will return the saved DTO
    val savedDTO = repo.getReminder(myDTO.id)

    //"savedDTO" is wrapped in "Result"
    assertThat(savedDTO.succeeded,`is`(true))
    savedDTO as Result.Success
    assertThat(savedDTO.data.title, `is`("Title"))
    assertThat(savedDTO.data.description, `is`("Description"))
    assertThat(savedDTO.data.id, `is`(myDTO.id))

}

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */

}