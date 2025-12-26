package com.InvalidHamdy.moviezshow.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.InvalidHamdy.moviezshow.R
import com.InvalidHamdy.moviezshow.databinding.ActivityFavBinding
import com.InvalidHamdy.moviezshow.services.PosterAdapter
import com.InvalidHamdy.moviezshow.viewmodel.FavListState
import com.InvalidHamdy.moviezshow.viewmodel.FavViewModel
import com.google.firebase.auth.FirebaseAuth

class FavActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavBinding
    private val viewModel: FavViewModel by viewModels()
    private var posterAdapter: PosterAdapter? = null
    private var currentFilter: String = "favorites"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFavBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.init(applicationContext)
        setupUI()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set username from Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        binding.usernameTv.text = currentUser?.displayName ?: "User"
        
        // Set initial filter state
        updateFilterUI("favorites")
    }

    private fun setupRecyclerView() {
        binding.listRv.layoutManager = GridLayoutManager(this, 3)
    }

    private fun setupObservers() {
        viewModel.listState.observe(this) { state ->
            when (state) {
                is FavListState.Loading -> {
                    // Could show loading indicator
                }
                is FavListState.Success -> {
                    posterAdapter = PosterAdapter(
                        state.items,
                        onPosterClick = { mediaItem ->
                            val intent = Intent(this, DetailActivity::class.java).apply {
                                putExtra("media_id", mediaItem.id)
                                putExtra("media_type", "movie") // Could be improved
                                putExtra("poster_path", mediaItem.poster_path ?: "")
                                putExtra("title", mediaItem.title ?: mediaItem.name ?: "")
                                putExtra("overview", mediaItem.overview ?: "")
                            }
                            startActivity(intent)
                        },
                        onFavClick = { item ->
                            viewModel.toggleFavorite(item)
                        },
                        onSaveClick = { item ->
                            showSaveListDialog(item)
                        }
                    )
                    binding.listRv.adapter = posterAdapter
                }
                is FavListState.Empty -> {
                    posterAdapter = PosterAdapter(emptyList())
                    binding.listRv.adapter = posterAdapter
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
                is FavListState.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.favTv.setOnClickListener {
            updateFilterUI("favorites")
            viewModel.loadList("favorites")
        }

        binding.WatchlistTv.setOnClickListener {
            updateFilterUI("watch_later")
            viewModel.loadList("watch_later")
        }

        binding.completedTv.setOnClickListener {
            updateFilterUI("completed")
            viewModel.loadList("completed")
        }

        binding.droppedTv.setOnClickListener {
            updateFilterUI("dropped")
            viewModel.loadList("dropped")
        }
    }

    private fun updateFilterUI(filter: String) {
        currentFilter = filter
        
        // Reset all to white
        binding.favTv.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.WatchlistTv.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.completedTv.setTextColor(ContextCompat.getColor(this, R.color.white))
        binding.droppedTv.setTextColor(ContextCompat.getColor(this, R.color.white))

        // Highlight selected
        when (filter) {
            "favorites" -> binding.favTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
            "watch_later" -> binding.WatchlistTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
            "completed" -> binding.completedTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
            "dropped" -> binding.droppedTv.setTextColor(ContextCompat.getColor(this, R.color.red_bttn))
        }
    }

    private fun showSaveListDialog(item: com.InvalidHamdy.moviezshow.data.response.MediaItem) {
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