package com.InvalidHamdy.moviezshow

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.InvalidHamdy.moviezshow.databinding.ActivityDetailBinding
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val apiKey = "093224ec3a7dea80578772862bb63ea8"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mediaId = intent.getIntExtra("media_id", -1)
        val mediaType = intent.getStringExtra("media_type") ?: ""
        val posterPath = intent.getStringExtra("poster_path") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val overview = intent.getStringExtra("overview") ?: ""
        val numberOfSeasons = if (intent.hasExtra("number_of_seasons"))
            intent.getIntExtra("number_of_seasons", 0) else null

        binding.showTitleTv.text = title
        binding.descTv.text = overview

        if (mediaType == "tv") {
            if (numberOfSeasons != null && numberOfSeasons > 0) {
                binding.seasonTv.text = "Seasons: $numberOfSeasons"
            } else {
                binding.seasonTv.text = "Season : Unknown"
            }
        } else {
            binding.seasonTv.text = ""
        }

        if (posterPath.isNotEmpty()) {
            val fullUrl = if (posterPath.startsWith("http")) posterPath
            else "https://image.tmdb.org/t/p/w780$posterPath"

            Glide.with(this)
                .load(fullUrl)
                .centerCrop()
                .into(binding.posterIv)
        } else {
            binding.posterIv.setImageResource(R.drawable.placeholder)
        }
        if (mediaId != -1) {
            loadCredits(mediaType, mediaId)
        } else {
            binding.castMainTv.isVisible = false
            binding.castRv.isVisible = false
        }

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                val videoId = "aqz-KE-bpKQ"
                youTubePlayer.cueVideo(videoId, 0f)
            }
        })

        binding.watchBttn.setOnClickListener {
        }

        binding.shareBttn.setOnClickListener {
        }
    }
    private fun loadCredits(mediaType: String, id: Int) {
        val call: Call<CreditsResponse> = if (mediaType == "movie") {
            RetrofitClient.instance.getMovieCredits(id, apiKey)
        } else {
            RetrofitClient.instance.getTvCredits(id, apiKey)
        }

        call.enqueue(object : Callback<CreditsResponse> {
            override fun onResponse(call: Call<CreditsResponse>, response: Response<CreditsResponse>) {
                if (response.isSuccessful) {
                    val credits = response.body()?.cast ?: emptyList()
                    if (credits.isNotEmpty()) {
                        val adapter = CastAdapter(credits)
                        binding.castRv.adapter = adapter
                        binding.castMainTv.isVisible = true
                        binding.castRv.isVisible = true
                    } else {
                        binding.castMainTv.isVisible = false
                        binding.castRv.isVisible = false
                    }
                } else {
                    Log.e("Credits", "Error: ${response.errorBody()?.string()}")
                    binding.castMainTv.isVisible = false
                    binding.castRv.isVisible = false
                }
            }

            override fun onFailure(call: Call<CreditsResponse>, t: Throwable) {
                Log.e("Credits", "Failure: ${t.message}")
                binding.castMainTv.isVisible = false
                binding.castRv.isVisible = false
            }
        })
    }
}