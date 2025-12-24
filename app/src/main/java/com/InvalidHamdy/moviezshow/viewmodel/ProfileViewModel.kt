package com.InvalidHamdy.moviezshow.viewmodel

import androidx.lifecycle.ViewModel
import com.InvalidHamdy.moviezshow.data.repository.AuthRepository

class ProfileViewModel (
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

}