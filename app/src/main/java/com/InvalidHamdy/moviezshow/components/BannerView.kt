package com.InvalidHamdy.moviezshow.components
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.InvalidHamdy.moviezshow.MainActivity
import com.InvalidHamdy.moviezshow.databinding.BannerBinding
import com.InvalidHamdy.moviezshow.screens.ProfileActivity

class BannerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private val binding: BannerBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = BannerBinding.inflate(inflater, this, true)
        setupClicks()
    }

    private fun setupClicks() {
//        binding.favIv.setOnClickListener {
//            val intent = Intent(context, MainActivity::class.java)
//            context.startActivity(intent)
//        }

        binding.savedIv.setOnClickListener {
            Toast.makeText(context, "Saved Clicked", Toast.LENGTH_SHORT).show()
        }

        binding.accIv.setOnClickListener {
            val intent = Intent(context, ProfileActivity::class.java)
            context.startActivity(intent)
        }

        binding.logoIv.setOnClickListener {
            Toast.makeText(context, "Logo Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    fun setIconsVisibility(
        fav: Boolean = true,
        saved: Boolean = true,
        acc: Boolean = true
    ) {
        binding.favIv.visibility = if (fav) VISIBLE else GONE
        binding.savedIv.visibility = if (saved) VISIBLE else GONE
        binding.accIv.visibility = if (acc) VISIBLE else GONE
    }
}

