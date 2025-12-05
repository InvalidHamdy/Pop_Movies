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
    private var youTubePlayerRef: YouTubePlayer? = null
    private var pendingVideoKey: String? = null

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
            loadVideos(mediaType, mediaId)
        } else {
            binding.castMainTv.isVisible = false
            binding.castRv.isVisible = false
        }

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayerRef = youTubePlayer
                pendingVideoKey?.let { youTubePlayer.cueVideo(it, 0f) }
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

    private fun sanitizeYoutubeKey(raw: String): String {
        val lower = raw.trim()
        val vIndex = lower.indexOf("v=")
        if (vIndex != -1) {
            val after = lower.substring(vIndex + 2)
            val amp = after.indexOf('&')
            return if (amp != -1) after.substring(0, amp) else after
        }
        val slashIndex = lower.lastIndexOf('/')
        if (slashIndex != -1 && slashIndex < lower.length - 1) {
            val candidate = lower.substring(slashIndex + 1)
            val q = candidate.indexOf('?')
            return if (q != -1) candidate.substring(0, q) else candidate
        }
        return raw
    }

    private fun loadVideos(mediaType: String, id: Int) {
        val call: Call<VideoResponse> = if (mediaType == "movie") {
            RetrofitClient.instance.getMovieVideos(id, apiKey)
        } else {
            RetrofitClient.instance.getTvVideos(id, apiKey)
        }

        call.enqueue(object : Callback<VideoResponse> {
            override fun onResponse(call: Call<VideoResponse>, response: Response<VideoResponse>) {
                if (response.isSuccessful) {
                    val videos = response.body()?.results ?: emptyList()

                    Log.d("Videos", "TMDB returned ${videos.size} videos for id=$id: ${
                        videos.map { "${it.site}/${it.type}/${it.key}" }
                    }")

                    // Only consider YouTube videos (embedded player expects YouTube ids)
                    val trailer = videos.firstOrNull { it.site.equals("YouTube", true) && it.type.equals("Trailer", true) }
                        ?: videos.firstOrNull { it.site.equals("YouTube", true) }

                    if (trailer == null) {
                        Log.d("Videos", "No YouTube videos returned, hiding watch button")
                        pendingVideoKey = null
                        binding.watchBttn.isVisible = false
                        return
                    }

                    val rawKey = trailer.key ?: ""
                    val key = sanitizeYoutubeKey(rawKey)
                    Log.d("Videos", "Selected rawKey=$rawKey sanitized=$key site=${trailer.site} type=${trailer.type}")

                    // basic validation: YouTube ids are typically 11 chars; if clearly wrong hide play
                    if (key.isBlank() || key.length < 6) {
                        Log.w("Videos", "Sanitized key looks invalid, hiding watch button")
                        pendingVideoKey = null
                        binding.watchBttn.isVisible = false
                        return
                    }

                    pendingVideoKey = key
                    youTubePlayerRef?.let { it.cueVideo(key, 0f) }
                    binding.watchBttn.isVisible = true
                } else {
                    Log.e("Videos", "Error: ${response.errorBody()?.string()}")
                    binding.watchBttn.isVisible = false
                }
            }

            override fun onFailure(call: Call<VideoResponse>, t: Throwable) {
                Log.e("Videos", "Failure: ${t.message}")
                binding.watchBttn.isVisible = false
            }
        })
    }
}