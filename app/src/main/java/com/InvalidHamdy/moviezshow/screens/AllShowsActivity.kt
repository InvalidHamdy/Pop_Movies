package com.InvalidHamdy.moviezshow.screens

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.InvalidHamdy.moviezshow.R
import com.InvalidHamdy.moviezshow.databinding.ActivityAllShowsBinding
import com.InvalidHamdy.moviezshow.services.GenreAdapter
import com.InvalidHamdy.moviezshow.services.PosterAdapter
import com.InvalidHamdy.moviezshow.viewmodel.AllShowsState
import com.InvalidHamdy.moviezshow.viewmodel.AllShowsViewModel
import com.InvalidHamdy.moviezshow.data.repository.GenresState

class AllShowsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllShowsBinding
    private val viewModel: AllShowsViewModel by viewModels()
    
    private var posterAdapter: PosterAdapter? = null
    private var genreAdapter: GenreAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAllShowsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialMediaType = intent.getStringExtra("media_type") ?: "tv"
        viewModel.init(applicationContext, initialMediaType)
        updateMediaTypeUI(initialMediaType)

        setupRecyclerViews()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerViews() {
        val layoutManager = GridLayoutManager(this, 3)
        binding.postersRV.layoutManager = layoutManager
        binding.genreRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        
        // Infinite scroll listener
        binding.postersRV.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) { // Scrolling down
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        viewModel.loadMore()
                    }
                }
            }
        })
    }

    private fun setupObservers() {
        viewModel.allShowsState.observe(this) { state ->
            when (state) {
                is AllShowsState.Loading -> {
                     // Optionally show a progress bar
                }
                is AllShowsState.Success -> {
                    if (state.isAppend) {
                        posterAdapter?.addItems(state.items)
                    } else {
                        // New List - create adapter with all click handlers
                        posterAdapter = PosterAdapter(
                            state.items,
                            onPosterClick = { mediaItem ->
                                val intent = android.content.Intent(this, DetailActivity::class.java).apply {
                                    putExtra("media_id", mediaItem.id)
                                    putExtra("media_type", viewModel.currentMediaType)
                                    putExtra("poster_path", mediaItem.poster_path ?: "")
                                    putExtra("title", mediaItem.title ?: mediaItem.name ?: "")
                                    putExtra("overview", mediaItem.overview ?: "")
                                    mediaItem.number_of_seasons?.let { putExtra("number_of_seasons", it) }
                                }
                                startActivity(intent)
                            },
                            onFavClick = { item ->
                                viewModel.toggleFavorite(item)
                            },
                            onSaveClick = { item ->
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
                        )
                        binding.postersRV.adapter = posterAdapter
                        
                        // Apply current local state to new adapter
                        viewModel.localState.value?.let { (favIds, savedMap) ->
                            posterAdapter?.updateLocalState(favIds, savedMap)
                        }
                    }
                }
                is AllShowsState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.genresState.observe(this) { state ->
            if (state is GenresState.Success) {
                genreAdapter = GenreAdapter(state.genres) { genre, _ ->
                    viewModel.applyGenreFilter(genre)
                }
                binding.genreRv.adapter = genreAdapter
                
                viewModel.activeGenreId?.let { id ->
                     val index = state.genres.indexOfFirst { it.id.toString() == id }
                     if(index != -1) genreAdapter?.selectPosition(index)
                }
            }
        }

        // Observe local state changes (favorites and saved lists)
        viewModel.localState.observe(this) { (favIds, savedMap) ->
            posterAdapter?.updateLocalState(favIds, savedMap)
        }
    }

    private fun setupListeners() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.movieFilterTv.setOnClickListener {
            viewModel.switchMediaType("movie")
            updateMediaTypeUI("movie")
        }
        binding.showsFilterTv.setOnClickListener {
            viewModel.switchMediaType("tv")
            updateMediaTypeUI("tv")
        }

        binding.genreFilterBttn.setOnClickListener {
            binding.genreRv.isVisible = !binding.genreRv.isVisible
        }
        
        binding.yearFilterBttn.setOnClickListener {
            // Placeholder for Year Filter Dialog
            // viewModel.applyYearFilter(...)
            Toast.makeText(this, "Year filter not implemented yet", Toast.LENGTH_SHORT).show()
        }
        
        binding.ratingFilterBttn.setOnClickListener {
            // Placeholder for Rating Filter Dialog
            // viewModel.applyRatingFilter(...)
             Toast.makeText(this, "Rating filter not implemented yet", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMediaTypeUI(type: String) {
        if (type == "movie") {
            binding.movieFilterTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
            binding.showsFilterTv.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.movieFilterTv.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.showsFilterTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
        }
    }
}