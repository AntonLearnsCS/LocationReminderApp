package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View.inflate
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
//import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.databinding.ItReminderBinding.inflate
import com.udacity.project4.locationreminders.RemindersActivity
import timber.log.Timber
import java.util.zip.Inflater


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {
    private lateinit var viewModel: AuthViewModel
    private  lateinit var binding : ActivityAuthenticationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        //inflating xml layout in activity
        //for inflating xml layout in fragment: DataBindingUtil.inflate(inflater, R.layout.activity_authentication, container, false)
        binding = ActivityAuthenticationBinding.inflate(getLayoutInflater())

        binding.lifecycleOwner = this
        viewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google


//          TODO: If the user was authenticated, send him to RemindersActivity
            if (viewModel.authenticationState.value == AuthViewModel.AuthenticationState.AUTHENTICATED)
            {
                binding.button.setText("Logout")
                binding.button.setOnClickListener {
                    Timber.i("auth")
                    binding.button.setText("Login")
                    AuthUI.getInstance().signOut(this)

                }
                // I wouldn't use navController here since we are dealing with activities and not fragments
                val remindersIntent = Intent(this, RemindersActivity::class.java)
                startActivity(remindersIntent)
            }
        else
            {
                binding.button.setText("Login")
                binding.button.setOnClickListener {
                Timber.i("not auth")
                launchSignInFlow()

                    //binding.button.setText("Logout")
                }
            }
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
    }
    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email
        // If users choose to register with their email,
        // they will need to create a password as well
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent.
        // We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), SIGN_IN_RESULT_CODE
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

    companion object {
        const val TAG = "MainFragment"
        const val SIGN_IN_RESULT_CODE = 1001
    }
}
