package com.InvalidHamdy.moviezshow

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.InvalidHamdy.moviezshow.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPref = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isFirstRun = sharedPref.getBoolean("isFirstRun", true)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        val user = FirebaseAuth.getInstance().currentUser
        enableEdgeToEdge()
        setContentView(binding.root)
        val nextIntent = when {
            isFirstRun -> {
                sharedPref.edit().putBoolean("isFirstLaunch", false).apply()
                Intent(this, LogInActivity::class.java)
            }
            user != null -> {
                Intent(this, MainActivity::class.java)
            }
            else -> {
                Intent(this, LogInActivity::class.java)
            }
        }
        startActivity(nextIntent)
        finish()
    }

}