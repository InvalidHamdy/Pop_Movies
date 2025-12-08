package com.InvalidHamdy.moviezshow.data.repository
import com.google.firebase.auth.FirebaseUser

// Login States
sealed class LoginState {
    object Loading : LoginState()
    data class Success(val user: FirebaseUser) : LoginState()
    data class Error(val message: String) : LoginState()
}

// SignUp States
sealed class SignUpState {
    object Loading : SignUpState()
    data class Success(val user: FirebaseUser) : SignUpState()
    data class Error(val message: String) : SignUpState()
}

// Google SignIn States (shared between login and signup)
sealed class GoogleSignInState {
    object Loading : GoogleSignInState()
    data class Success(val user: FirebaseUser) : GoogleSignInState()
    data class Error(val message: String) : GoogleSignInState()
}

// Reset Password States
sealed class ResetPasswordState {
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
}