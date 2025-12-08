package com.InvalidHamdy.moviezshow.screens

import LogInViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.InvalidHamdy.moviezshow.MainActivity
import com.InvalidHamdy.moviezshow.R
import com.InvalidHamdy.moviezshow.databinding.ActivityLogInBinding
import com.InvalidHamdy.moviezshow.data.repository.GoogleSignInState
import com.InvalidHamdy.moviezshow.data.repository.LoginState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.util.concurrent.Executors

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var credentialManager: CredentialManager
    private val viewModel: LogInViewModel by viewModels()

    companion object {
        private const val TAG = "LogInActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    showLoading(true)
                }
                is LoginState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    navigateToHome()
                }
                is LoginState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.googleSignInState.observe(this) { state ->
            when (state) {
                is GoogleSignInState.Loading -> {
                    showLoading(true)
                }
                is GoogleSignInState.Success -> {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Welcome, ${state.user.displayName}",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToHome()
                }
                is GoogleSignInState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.loginBttn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()

            viewModel.signInWithEmail(email, password)
        }

        binding.signUpTxt.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.googleLoginBttn.setOnClickListener {
            signInWithGoogle()
        }

        binding.forgotPassTxt.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun signInWithGoogle() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val executor = Executors.newSingleThreadExecutor()

        credentialManager.getCredentialAsync(
            this,
            request,
            null,
            executor,
            object : CredentialManagerCallback<
                    GetCredentialResponse,
                    GetCredentialException
                    > {
                override fun onResult(result: GetCredentialResponse) {
                    handleGoogleSignIn(result)
                }

                override fun onError(e: GetCredentialException) {
                    handleGoogleSignInError(e)
                }
            }
        )
    }

    private fun handleGoogleSignIn(result: GetCredentialResponse) {
        try {
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            viewModel.signInWithGoogle(idToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Google credential", e)
            runOnUiThread {
                Toast.makeText(this, "Google Sign-In error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignInError(e: GetCredentialException) {
        Log.e(TAG, "Google Sign-In failed", e)
        runOnUiThread {
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.loginBttn.isEnabled = !isLoading
        binding.googleLoginBttn.isEnabled = !isLoading
        // You can add a progress bar here if you have one in your layout
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.checkCurrentUser() != null) {
            navigateToHome()
        }
    }
}