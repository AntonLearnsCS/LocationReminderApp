package com.udacity.project4.base

import androidx.navigation.NavDirections

/**
 * Sealed class used with the live data to navigate between the fragments
 */
//sealed class - Sealed classes and interfaces represent restricted class hierarchies that provide more control over inheritance.
//All subclasses of a sealed class are known at compile time.
sealed class NavigationCommand {
    /**
     * navigate to a direction
     * The data class extends the sealed class
     */
    data class To(val directions: NavDirections) : NavigationCommand()

    /**
     * navigate back to the previous fragment
     */
    object Back : NavigationCommand()

    /**
     * navigate back to a destination in the back stack
     */
    data class BackTo(val destinationId: Int) : NavigationCommand()
}