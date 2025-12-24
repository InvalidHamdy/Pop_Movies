package com.InvalidHamdy.moviezshow.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.InvalidHamdy.moviezshow.data.repository.AuthRepository
import com.InvalidHamdy.moviezshow.data.repository.GoogleSignInState
import com.InvalidHamdy.moviezshow.data.repository.SignUpState
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _signUpState = MutableLiveData<SignUpState>()
    val signUpState: LiveData<SignUpState> = _signUpState

    private val _googleSignInState = MutableLiveData<GoogleSignInState>()
    val googleSignInState: LiveData<GoogleSignInState> = _googleSignInState

    fun signUp(firstName: String, lastName: String, email: String, password: String, confirmPassword: String) {
        // Validation
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _signUpState.value = SignUpState.Error("Please fill all fields")
            return
        }

        if (password != confirmPassword) {
            _signUpState.value = SignUpState.Error("Passwords do not match")
            return
        }

        if (password.length < 6) {
            _signUpState.value = SignUpState.Error("Password must be at least 6 characters")
            return
        }

        _signUpState.value = SignUpState.Loading

        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(firstName, lastName, email, password)
            _signUpState.value = if (result.isSuccess) {
                SignUpState.Success(result.getOrNull()!!)
            } else {
                SignUpState.Error("Authentication failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _googleSignInState.postValue(GoogleSignInState.Loading)

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(idToken)
            _googleSignInState.value = if (result.isSuccess) {
                GoogleSignInState.Success(result.getOrNull()!!)
            } else {
                GoogleSignInState.Error("Google Sign-In failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}