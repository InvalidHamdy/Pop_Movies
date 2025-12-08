package com.InvalidHamdy.moviezshow.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.InvalidHamdy.moviezshow.MainActivity
import com.InvalidHamdy.moviezshow.R
import com.InvalidHamdy.moviezshow.databinding.ActivitySignUpBinding
import com.InvalidHamdy.moviezshow.viewmodel.SignUpViewModel
import com.InvalidHamdy.moviezshow.data.repository.GoogleSignInState
import com.InvalidHamdy.moviezshow.data.repository.SignUpState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import org.json.JSONException

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var credentialManager: CredentialManager
    private val viewModel: SignUpViewModel by viewModels()

    companion object {
        private const val TAG = "SignUpActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        credentialManager = CredentialManager.create(this)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.signUpState.observe(this) { state ->
            when (state) {
                is SignUpState.Loading -> {
                    showLoading(true)
                }
                is SignUpState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is SignUpState.Error -> {
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
                    Toast.makeText(this, "Welcome ${state.user.displayName}", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is GoogleSignInState.Error -> {
                    showLoading(false)
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.signUpBttn.setOnClickListener {
            val fName = binding.firstNameEt.text.toString().trim()
            val lName = binding.lastNameEt.text.toString().trim()
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            val conPass = binding.confirmpassEt.text.toString().trim()

            viewModel.signUp(fName, lName, email, password, conPass)
        }

        binding.signInTxt.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }

        binding.googleLoginBttn.setOnClickListener {
            signInWithGoogle()
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

        val executor = ContextCompat.getMainExecutor(this)

        credentialManager.getCredentialAsync(
            context = this,
            request = request,
            cancellationSignal = null,
            executor = executor,
            callback = object :
                CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    handleSignInResult(result)
                }

                override fun onError(e: GetCredentialException) {
                    Log.e(TAG, "Google Sign-In failed: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Google Sign-In failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )
    }

    private fun handleSignInResult(result: GetCredentialResponse) {
        try {
            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            viewModel.signInWithGoogle(idToken)
        } catch (e: JSONException) {
            Log.e(TAG, "JSON parsing error", e)
            Toast.makeText(this, "Google Sign-In error", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In error", e)
            Toast.makeText(this, "Google Sign-In error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.signUpBttn.isEnabled = !isLoading
        binding.googleLoginBttn.isEnabled = !isLoading
        // You can add a progress bar here if you have one in your layout
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}