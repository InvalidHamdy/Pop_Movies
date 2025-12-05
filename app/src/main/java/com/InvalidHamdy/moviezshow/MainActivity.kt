package com.InvalidHamdy.moviezshow

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.InvalidHamdy.moviezshow.databinding.ActivityMainBinding
import com.bumptech.glide.Glide
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    // adapters
    private var genreAdapter: GenreAdapter? = null
    private var topAdapter: PosterAdapter? = null
    private var genreShowAdapter: PosterAdapter? = null

    private val apiKey = "093224ec3a7dea80578772862bb63ea8"
    private var currentMediaType = "tv" // default to shows as requested ("shows chosen")
    private var currentGenres: List<Genre> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Setup recyclers (horizontal)
        binding.topShowRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.genreRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.GenreShowRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // default: Shows selected
        setMediaButtonsState(isMovieSelected = false)
        loadAllForMedia("tv")

        binding.movieTv.setOnClickListener {
            setMediaButtonsState(isMovieSelected = true)
            loadAllForMedia("movie")
        }

        binding.showTv.setOnClickListener {
            setMediaButtonsState(isMovieSelected = false)
            loadAllForMedia("tv")
        }
    }

    private fun setMediaButtonsState(isMovieSelected: Boolean) {
        if (isMovieSelected) {
            binding.movieTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
            binding.showTv.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.topShowTv.text = "Top Movies"
            currentMediaType = "movie"
            getTopTrendingAndFill(currentMediaType)
        } else {
            binding.movieTv.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.showTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
            binding.topShowTv.text = "Top Shows"
            currentMediaType = "tv"
            getTopTrendingAndFill(currentMediaType)
        }
    }

    private fun loadAllForMedia(mediaType: String) {
        loadGenres(mediaType)
        loadTrending(mediaType)
    }

    private fun loadGenres(mediaType: String) {
        val call = if (mediaType == "movie") RetrofitClient.instance.getMovieGenres(apiKey)
        else RetrofitClient.instance.getTvGenres(apiKey)

        call.enqueue(object : Callback<GenreResponse> {
            override fun onResponse(call: Call<GenreResponse>, response: Response<GenreResponse>) {
                if (response.isSuccessful) {
                    currentGenres = response.body()?.genres ?: emptyList()
                    Log.d("Genres", "Genres: $currentGenres")

                    // create adapter with click callback
                    genreAdapter = GenreAdapter(currentGenres) { genre, pos ->
                        // when user taps a genre -> fetch items for that genre
                        loadByGenre(currentMediaType, genre.id)
                    }

                    binding.genreRv.adapter = genreAdapter

                    if (currentGenres.isNotEmpty()) {
                        genreAdapter?.selectPosition(0)
                        loadByGenre(mediaType, currentGenres[0].id)
                    } else {
                        binding.GenreShowRv.adapter = PosterAdapter(emptyList())
                    }
                } else {
                    Log.e("Genres", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GenreResponse>, t: Throwable) {
                Log.e("Genres", "Failure: ${t.message}")
            }
        })
    }

    private fun loadTrending(mediaType: String) {
        RetrofitClient.instance.getTrending(mediaType, apiKey)
            .enqueue(object : Callback<MediaResponse> {
                override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                    if (response.isSuccessful) {
                        val items = response.body()?.results ?: emptyList()
                        topAdapter = PosterAdapter(items){mediaItem ->
                            openDetailFor(mediaItem, currentMediaType)

                        }
                        binding.topShowRv.adapter = topAdapter
                    } else {
                        Log.e("Trending", "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                    Log.e("Trending", "Failure: ${t.message}")
                }
            })
    }

    private fun loadByGenre(mediaType: String, genreId: Int) {
        val call = if (mediaType == "movie") {
            RetrofitClient.instance.discoverMoviesByGenre(apiKey, genreId)
        } else {
            RetrofitClient.instance.discoverTvByGenre(apiKey, genreId)
        }

        call.enqueue(object : Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val items = response.body()?.results ?: emptyList()
                    genreShowAdapter = PosterAdapter(items){
                        mediaItem ->
                        openDetailFor(mediaItem, currentMediaType)
                    }
                    binding.GenreShowRv.adapter = genreShowAdapter
                } else {
                    Log.e("Discover", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                Log.e("Discover", "Failure: ${t.message}")
            }
        })
    }
    private fun getTopTrendingAndFill(mediaType: String) {
        RetrofitClient.instance.getTrending(mediaType, apiKey)
            .enqueue(object : Callback<MediaResponse> {
                override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                    if (!response.isSuccessful) {
                        // optional: log or show error
                        return
                    }

                    val results = response.body()?.results ?: emptyList()
                    if (results.isEmpty()) {
                        // nothing to show — clear fields
                        binding.showTitleTv.text = ""
                        binding.descTv.text = ""
                        binding.seasonTv.text = ""
                        binding.mainPosterIv.setImageResource(R.drawable.placeholder) // optional placeholder
                        return
                    }

                    // take the first (top) item
                    val top = results[0]

                    // Title (movie uses 'title', tv uses 'name')
                    val title = top.title ?: top.name ?: ""
                    binding.showTitleTv.text = title

                    // Description/overview
                    binding.descTv.text = top.overview ?: ""

                    // Season / release info:
                    if (mediaType == "tv") {
                        // prefer number_of_seasons if available, otherwise fall back to first air date
                        val seasonsText = top.number_of_seasons?.let { "Seasons: $it" }
                            ?: top.first_air_date?.let { "First aired: $it" }
                            ?: ""
                        binding.seasonTv.text = seasonsText
                    } else { // movie
                        val releaseText = top.release_date?.let { "Release: $it" } ?: ""
                        binding.seasonTv.text = releaseText
                    }

                    // Poster image (w780 or w500)
                    val posterPath = top.poster_path
                    if (!posterPath.isNullOrEmpty()) {
                        val fullUrl = "https://image.tmdb.org/t/p/w780$posterPath"
                        Glide.with(this@MainActivity)
                            .load(fullUrl)
                            .centerCrop()
                            .into(binding.mainPosterIv)
                    } else {
                        binding.mainPosterIv.setImageResource(R.drawable.placeholder)
                    }
                }

                override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                    // optional: log error and set placeholder
                    binding.showTitleTv.text = ""
                    binding.descTv.text = ""
                    binding.seasonTv.text = ""
                    binding.mainPosterIv.setImageResource(R.drawable.placeholder)
                }
            })
    }
    private fun openDetailFor(item: MediaItem, mediaType: String) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("media_id", item.id)
            putExtra("media_type", mediaType) // "tv" or "movie"
            putExtra("poster_path", item.poster_path ?: "")
            putExtra("title", item.title ?: item.name ?: "")
            putExtra("overview", item.overview ?: "")
            item.number_of_seasons?.let { putExtra("number_of_seasons", it) }
        }
        startActivity(intent)
    }
}
