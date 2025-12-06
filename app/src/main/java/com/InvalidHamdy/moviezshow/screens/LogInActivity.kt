package com.InvalidHamdy.moviezshow.screens

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.InvalidHamdy.moviezshow.MainActivity
import com.InvalidHamdy.moviezshow.R
import com.InvalidHamdy.moviezshow.databinding.ActivityLogInBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.Executors

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        binding.loginBttn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signInWithEmail(email, password)
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
    private fun signInWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Welcome, ${auth.currentUser?.displayName}", Toast.LENGTH_SHORT).show()
                        goToHome()
                    } else {
                        Toast.makeText(this, "Google Authentication failed", Toast.LENGTH_SHORT).show()
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Google credential", e)
        }
    }
    private fun handleGoogleSignInError(e: GetCredentialException) {
        Log.e(TAG, "Google Sign-In failed", e)
        Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) goToHome()
    }
    companion object {
        private const val TAG = "LogInActivity"
    }
}