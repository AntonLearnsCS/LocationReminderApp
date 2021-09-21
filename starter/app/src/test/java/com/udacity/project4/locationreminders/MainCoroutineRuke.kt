package com.udacity.project4.locationreminders

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

//By extending TestWatcher() we allow the class to be considered a JUnit rule and be declared as so in TaskViewModelTest.kt
//Note: TestCoroutineDispatcher is synchronous, meaning it will run each coroutine in order, finishing the first before moving on to the next.
@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()):
    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher) {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}