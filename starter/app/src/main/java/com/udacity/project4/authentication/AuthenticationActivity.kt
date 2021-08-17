package com.udacity.project4.authentication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.udacity.project4.R

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var viewModel: AuthViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google


//          TODO: If the user was authenticated, send him to RemindersActivity
            if (viewModel.authenticationState.value == AuthViewModel.AuthenticationState.AUTHENTICATED)
            {
                //findNavController - This method will locate the NavController associated with this view.
                    // This is automatically populated for views that are managed by a NavHost and is intended for use
                        // by various listener interfaces.
                val navController = findNavController(R.id.nav_host_fragment)
                //navController.navigate()
            }
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }
}
