package com.InvalidHamdy.moviezshow.screens

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.InvalidHamdy.moviezshow.R
import com.InvalidHamdy.moviezshow.data.repository.AuthRepository
import com.InvalidHamdy.moviezshow.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding:  ActivityProfileBinding
    private val authRepository: AuthRepository = AuthRepository()
    private val user = authRepository.getCurrentUser()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupProfile()
    }

    private fun setupProfile() {
        val email = binding.emailView
        val usrnme = binding.usernameTv
        val creationTv = binding.dateView
        val isVerified = user?.isEmailVerified
        if(isVerified == true){
            binding.verifiedIv.visibility = View.VISIBLE
        }
        email.text = user?.email ?: getString(R.string.guest)
        usrnme.text = user?.displayName ?: getString(R.string.guest)
        creationTv.text = user?.metadata?.creationTimestamp?.let {
            android.text.format.DateFormat.format("dd/MM/yyyy", it)
        } ?: getString(R.string.unknown)
        setupLogOut()
    }

    private fun setupLogOut() {
        binding.logOutBttn.setOnClickListener {
            authRepository.signOut()
            naviageteToLgoin()
        }
    }
    private fun ProfileActivity.naviageteToLgoin() {
        val intent = Intent(this, LogInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}