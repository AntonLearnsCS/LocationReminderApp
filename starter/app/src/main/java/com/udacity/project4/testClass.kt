package com.udacity.project4

import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import java.util.*

//based on this testing, injection by Koin is strictly dependent on the parameter specified in the class to be injected
class testClass(val testClass: extensionInterface) {

    fun test()
    {
        testClass.testClassFun()
    }/*
    companion object{

        fun testClassFun()
        {
            println("This is a test")
        }
    }*/
}

interface extensionInterface
{
    fun testClassFun()
}

class testClass1 : extensionInterface {

    override fun testClassFun() {
        println("This is a test1")
    }
}
class testClass2 : extensionInterface {
    override fun testClassFun() {
        println("This is a test2")
    }
}

open class fakeExtensionClass()
{
    fun overrideThisMethod()
    {
        println("override method")
    }
}