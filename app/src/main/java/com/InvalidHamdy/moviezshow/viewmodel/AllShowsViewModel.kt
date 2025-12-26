package com.InvalidHamdy.moviezshow.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.InvalidHamdy.moviezshow.data.local.AppDatabase
import com.InvalidHamdy.moviezshow.data.repository.GenresState
import com.InvalidHamdy.moviezshow.data.repository.LocalRepository
import com.InvalidHamdy.moviezshow.data.repository.MediaRepository
import com.InvalidHamdy.moviezshow.data.response.Genre
import com.InvalidHamdy.moviezshow.data.response.MediaItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class AllShowsState {
    object Loading : AllShowsState()
    data class Success(val items: List<MediaItem>, val isAppend: Boolean = false) : AllShowsState()
    data class Error(val message: String) : AllShowsState()
}

class AllShowsViewModel(
    private val mediaRepository: MediaRepository = MediaRepository()
) : ViewModel() {

    private val _allShowsState = MutableLiveData<AllShowsState>()
    val allShowsState: LiveData<AllShowsState> = _allShowsState

    private val _genresState = MutableLiveData<GenresState>()
    val genresState: LiveData<GenresState> = _genresState

    // Local database state: Set<FavIds>, Map<Id, ListName>
    private val _localState = MutableLiveData<Pair<Set<Int>, Map<Int, String>>>()
    val localState: LiveData<Pair<Set<Int>, Map<Int, String>>> = _localState

    private var localRepository: LocalRepository? = null

    var currentMediaType: String = "tv"
        private set

    // Filters
    var activeGenreId: String? = null
        private set
    var activeYear: String? = null
        private set
    var activeRating: Float? = null
        private set
    
    // Search debounce
    private var searchJob: Job? = null
    var lastQuery: String = ""
        private set

    // Pagination
    private var currentPage = 1
    private var isLastPage = false
    private var isLoading = false
    
    fun init(context: Context, mediaType: String) {
        val db = AppDatabase.getDatabase(context)
        localRepository = LocalRepository(db.mediaDao())
        
        currentMediaType = mediaType
        loadGenres()
        resetAndLoad()
        loadLocalData()
    }

    fun switchMediaType(type: String) {
        if (currentMediaType == type) return
        currentMediaType = type
        activeGenreId = null 
        loadGenres()
        resetAndLoad()
    }
    
    fun loadMore() {
        if (isLoading || isLastPage) return
        currentPage++
        loadNetworkData(isLoadMore = true)
    }

    private fun resetAndLoad() {
        currentPage = 1
        isLastPage = false
        loadNetworkData(isLoadMore = false)
    }

    private fun loadGenres() {
        viewModelScope.launch {
            val result = mediaRepository.getGenres(currentMediaType)
            if (result.isSuccess) {
                _genresState.value = GenresState.Success(result.getOrNull()?.genres ?: emptyList())
            }
        }
    }

    private fun loadNetworkData(isLoadMore: Boolean) {
        if (isLoading) return
        isLoading = true
        
        if (!isLoadMore) {
            _allShowsState.value = AllShowsState.Loading
        }

        viewModelScope.launch {
            val result = if (lastQuery.isNotEmpty()) {
                mediaRepository.searchMedia(currentMediaType, lastQuery, currentPage)
            } else {
                 mediaRepository.discoverMedia(
                    mediaType = currentMediaType,
                    genreId = activeGenreId,
                    year = activeYear,
                    rating = activeRating,
                    page = currentPage
                )
            }

            isLoading = false
            
            if (result.isSuccess) {
                val response = result.getOrNull()
                val items = response?.results ?: emptyList()
                
                // Simple check for last page
                if (items.isEmpty()) {
                    isLastPage = true
                }
                
                // If it is load more, we want the UI to Append
                // If it is refresh, we want the UI to Replace
                // To communicate this via LiveData state, we can use a wrapper or just pass the full list.
                // However, since we want efficient appending in UI, passing a flag in Success state helps.
                // For simplicity here, I'll emit Success(items, isAppend)
                
                _allShowsState.value = AllShowsState.Success(items, isAppend = isLoadMore)
                
            } else {
                if (!isLoadMore) {
                    _allShowsState.value = AllShowsState.Error(result.exceptionOrNull()?.message ?: "Failed to load")
                } else {
                    // Could show a toast or error at bottom
                }
            }
        }
    }

    fun search(query: String) {
        if (lastQuery == query) return
        lastQuery = query
        searchJob?.cancel()
        
        if (query.isEmpty()) {
            resetAndLoad()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            resetAndLoad()
        }
    }

    fun applyGenreFilter(genre: Genre?) {
        if (activeGenreId == genre?.id.toString()) return 
        activeGenreId = genre?.id?.toString()
        resetAndLoad()
    }

    fun applyYearFilter(year: String?) {
        activeYear = year
        resetAndLoad()
    }

    fun applyRatingFilter(rating: Float?) {
        activeRating = rating
        resetAndLoad()
    }

    private fun loadLocalData() {
        viewModelScope.launch {
            val allMedia = localRepository?.getAllMedia() ?: emptyList()
            val favIds = allMedia.filter { it.isFavorite }.map { it.id }.toSet()
            val savedMap = allMedia.filter { it.saveList != null }.associate { it.id to it.saveList!! }
            _localState.value = Pair(favIds, savedMap)
        }
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            localRepository?.toggleFavorite(
                item.id,
                item.title ?: item.name ?: "",
                item.poster_path,
                currentMediaType
            )
            loadLocalData()
        }
    }

    fun updateSaveList(item: MediaItem, listName: String?) {
        viewModelScope.launch {
            localRepository?.updateSaveList(
                item.id,
                item.title ?: item.name ?: "",
                item.poster_path,
                currentMediaType,
                listName
            )
            loadLocalData()
        }
    }
}
