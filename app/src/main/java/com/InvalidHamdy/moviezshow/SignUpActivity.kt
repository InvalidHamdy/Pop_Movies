package com.InvalidHamdy.moviezshow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.InvalidHamdy.moviezshow.databinding.ActivitySignUpBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONException

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var credentialManager: CredentialManager

    companion object {
        private const val TAG = "SignUpActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        credentialManager = CredentialManager.create(this)

        binding.signUpBttn.setOnClickListener {
            val fName = binding.firstNameEt.text.toString().trim()
            val lName = binding.lastNameEt.text.toString().trim()
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            val conPass = binding.confirmpassEt.text.toString().trim()

            if (fName.isEmpty() || lName.isEmpty() || email.isEmpty() || password.isEmpty() || conPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != conPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUp(fName, lName, email, password)
        }

        binding.signInTxt.setOnClickListener {
            startActivity(Intent(this, LogInActivity::class.java))
            finish()
        }

        binding.googleLoginBttn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signUp(firstName: String, lastName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    val userMap = hashMapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "email" to email
                    )
                    if (userId != null) {
                        db.collection("users").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this@SignUpActivity, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
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

            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val userMap = hashMapOf(
                            "firstName" to (user?.displayName ?: ""),
                            "email" to (user?.email ?: "")
                        )
                        db.collection("users").document(user!!.uid).set(userMap)
                        Toast.makeText(this, "Welcome ${user.displayName}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Google Sign-Up failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: JSONException) {
            Log.e(TAG, "JSON parsing error", e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In error", e)
        }
    }
}