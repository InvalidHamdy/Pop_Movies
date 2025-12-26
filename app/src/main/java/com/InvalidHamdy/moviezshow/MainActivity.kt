package com.InvalidHamdy.moviezshow
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.InvalidHamdy.moviezshow.data.repository.GenreMediaState
import com.InvalidHamdy.moviezshow.data.repository.GenresState
import com.InvalidHamdy.moviezshow.data.repository.TopMediaState
import com.InvalidHamdy.moviezshow.data.repository.TrendingState
import com.InvalidHamdy.moviezshow.databinding.ActivityMainBinding
import com.InvalidHamdy.moviezshow.data.response.MediaItem
import com.InvalidHamdy.moviezshow.screens.DetailActivity
import com.InvalidHamdy.moviezshow.services.GenreAdapter
import com.InvalidHamdy.moviezshow.services.PosterAdapter
import com.InvalidHamdy.moviezshow.viewmodel.MainViewModel
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private var genreAdapter: GenreAdapter? = null
    private var topAdapter: PosterAdapter? = null
    private var genreShowAdapter: PosterAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        viewModel.init(applicationContext)

        setupRecyclerViews()
        setupObservers()
        setupClickListeners()

        // default: Shows selected
        setMediaButtonsState(isMovieSelected = false)
        viewModel.loadAllForMedia("tv")
    }

    private fun setupRecyclerViews() {
        binding.topShowRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.genreRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.GenreShowRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun setupObservers() {
        // Observe genres
        viewModel.genresState.observe(this) { state ->
            when (state) {
                is GenresState.Loading -> {
                    Log.d("MainActivity", "Loading genres...")
                }
                is GenresState.Success -> {
                    Log.d("MainActivity", "Genres loaded: ${state.genres.size}")
                    genreAdapter = GenreAdapter(state.genres) { genre, pos ->
                        viewModel.loadMediaByGenre(viewModel.currentMediaType, genre.id)
                    }
                    binding.genreRv.adapter = genreAdapter

                    if (state.genres.isNotEmpty()) {
                        genreAdapter?.selectPosition(0)
                        viewModel.loadMediaByGenre(viewModel.currentMediaType, state.genres[0].id)
                    }
                }
                is GenresState.Error -> {
                    Log.e("MainActivity", "Error loading genres: ${state.message}")
                }
            }
        }

        // Observe trending
        viewModel.trendingState.observe(this) { state ->
            when (state) {
                is TrendingState.Loading -> {
                    Log.d("MainActivity", "Loading trending...")
                }
                is TrendingState.Success -> {
                    Log.d("MainActivity", "Trending loaded: ${state.items.size}")
                    topAdapter = PosterAdapter(
                        state.items,
                        onPosterClick = { mediaItem ->
                            openDetailFor(mediaItem, viewModel.currentMediaType)
                        },
                        onFavClick = { item ->
                            viewModel.toggleFavorite(item)
                        },
                        onSaveClick = { item ->
                            showSaveListDialog(item)
                        }
                    )
                    binding.topShowRv.adapter = topAdapter
                    
                    // Apply current local state
                    viewModel.localState.value?.let { (favIds, savedMap) ->
                        topAdapter?.updateLocalState(favIds, savedMap)
                    }
                }
                is TrendingState.Error -> {
                    Log.e("MainActivity", "Error loading trending: ${state.message}")
                }
            }
        }

        // Observe genre media
        viewModel.genreMediaState.observe(this) { state ->
            when (state) {
                is GenreMediaState.Loading -> {
                    Log.d("MainActivity", "Loading genre media...")
                }
                is GenreMediaState.Success -> {
                    Log.d("MainActivity", "Genre media loaded: ${state.items.size}")
                    genreShowAdapter = PosterAdapter(
                        state.items,
                        onPosterClick = { mediaItem ->
                            openDetailFor(mediaItem, viewModel.currentMediaType)
                        },
                        onFavClick = { item ->
                            viewModel.toggleFavorite(item)
                        },
                        onSaveClick = { item ->
                            showSaveListDialog(item)
                        }
                    )
                    binding.GenreShowRv.adapter = genreShowAdapter
                    
                    // Apply current local state
                    viewModel.localState.value?.let { (favIds, savedMap) ->
                        genreShowAdapter?.updateLocalState(favIds, savedMap)
                    }
                }
                is GenreMediaState.Error -> {
                    Log.e("MainActivity", "Error loading genre media: ${state.message}")
                }
            }
        }

        // Observe top media (for main poster)
        viewModel.topMediaState.observe(this) { state ->
            when (state) {
                is TopMediaState.Loading -> {
                    Log.d("MainActivity", "Loading top media...")
                }
                is TopMediaState.Success -> {
                    updateMainPoster(state.topItem)
                }
                is TopMediaState.Error -> {
                    Log.e("MainActivity", "Error loading top media: ${state.message}")
                    clearMainPoster()
                }
            }
        }

        // Observe local state changes
        viewModel.localState.observe(this) { (favIds, savedMap) ->
            topAdapter?.updateLocalState(favIds, savedMap)
            genreShowAdapter?.updateLocalState(favIds, savedMap)
        }
    }

    private fun setupClickListeners() {
        binding.movieTv.setOnClickListener {
            setMediaButtonsState(isMovieSelected = true)
            viewModel.loadAllForMedia("movie")
        }

        binding.showTv.setOnClickListener {
            setMediaButtonsState(isMovieSelected = false)
            viewModel.loadAllForMedia("tv")
        }
        
        binding.moreTopBttn.setOnClickListener {
            openAllShows()
        }
        
        binding.moreGenreBttn.setOnClickListener {
            openAllShows()
        }
    }

    private fun openAllShows() {
        val intent = Intent(this, com.InvalidHamdy.moviezshow.screens.AllShowsActivity::class.java).apply {
            putExtra("media_type", viewModel.currentMediaType)
        }
        startActivity(intent)
    }


    private fun setMediaButtonsState(isMovieSelected: Boolean) {
        if (isMovieSelected) {
            binding.movieTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
            binding.showTv.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.topShowTv.text = "Top Movies"
        } else {
            binding.movieTv.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.showTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
            binding.topShowTv.text = "Top Shows"
        }
    }

    private fun updateMainPoster(item: MediaItem?) {
        if (item == null) {
            clearMainPoster()
            return
        }

        // Title
        val title = item.title ?: item.name ?: ""
        binding.showTitleTv.text = title

        // Description
        binding.descTv.text = item.overview ?: ""

        // Season / release info
        if (viewModel.currentMediaType == "tv") {
            val seasonsText = item.number_of_seasons?.let { "Seasons: $it" }
                ?: item.first_air_date?.let { "First aired: $it" }
                ?: ""
            binding.seasonTv.text = seasonsText
        } else {
            val releaseText = item.release_date?.let { "Release: $it" } ?: ""
            binding.seasonTv.text = releaseText
        }

        // Poster image
        val posterPath = item.poster_path
        if (!posterPath.isNullOrEmpty()) {
            val fullUrl = "https://image.tmdb.org/t/p/w780$posterPath"
            Glide.with(this)
                .load(fullUrl)
                .centerCrop()
                .into(binding.mainPosterIv)
        } else {
            binding.mainPosterIv.setImageResource(R.drawable.placeholder)
        }
    }

    private fun clearMainPoster() {
        binding.showTitleTv.text = ""
        binding.descTv.text = ""
        binding.seasonTv.text = ""
        binding.mainPosterIv.setImageResource(R.drawable.placeholder)
    }

    private fun openDetailFor(item: MediaItem, mediaType: String) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("media_id", item.id)
            putExtra("media_type", mediaType)
            putExtra("poster_path", item.poster_path ?: "")
            putExtra("title", item.title ?: item.name ?: "")
            putExtra("overview", item.overview ?: "")
            item.number_of_seasons?.let { putExtra("number_of_seasons", it) }
        }
        startActivity(intent)
    }

    private fun showSaveListDialog(item: MediaItem) {
        val lists = arrayOf("Completed", "Dropped", "Watch Later", "Remove from List")
        android.app.AlertDialog.Builder(this)
            .setTitle("Add to List")
            .setItems(lists) { _, which ->
                val listName = when(which) {
                    0 -> "completed"
                    1 -> "dropped"
                    2 -> "watch_later"
                    else -> null
                }
                viewModel.updateSaveList(item, listName)
            }
            .show()
    }
}