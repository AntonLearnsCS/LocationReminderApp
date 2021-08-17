package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class AuthViewModel : ViewModel() {

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    // TODO Create an authenticationState variable based off the FirebaseUserLiveData object. By
    //  creating this variable, other classes will be able to query for whether the user is logged
    //  in or not
    //A "map" maps a unique key to a unique value; here "user" is the key, which ironically is the .value of the LiveData<>
    //The value is the " AuthenticationState.AUTHENTICATED", so we are mapping  val authenticationState to its value based
    // on the key

    val authenticationState : LiveData<AuthenticationState> = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }
}