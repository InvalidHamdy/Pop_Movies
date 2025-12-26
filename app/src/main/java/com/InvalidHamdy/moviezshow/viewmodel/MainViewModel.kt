package com.InvalidHamdy.moviezshow.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.InvalidHamdy.moviezshow.data.local.AppDatabase
import com.InvalidHamdy.moviezshow.data.repository.GenreMediaState
import com.InvalidHamdy.moviezshow.data.repository.GenresState
import com.InvalidHamdy.moviezshow.data.repository.LocalRepository
import com.InvalidHamdy.moviezshow.data.repository.MediaRepository
import com.InvalidHamdy.moviezshow.data.repository.TopMediaState
import com.InvalidHamdy.moviezshow.data.repository.TrendingState
import com.InvalidHamdy.moviezshow.data.response.Genre
import com.InvalidHamdy.moviezshow.data.response.MediaItem
import kotlinx.coroutines.launch

class MainViewModel(
    private val mediaRepository: MediaRepository = MediaRepository()
) : ViewModel() {

    private val _genresState = MutableLiveData<GenresState>()
    val genresState: LiveData<GenresState> = _genresState

    private val _trendingState = MutableLiveData<TrendingState>()
    val trendingState: LiveData<TrendingState> = _trendingState

    private val _genreMediaState = MutableLiveData<GenreMediaState>()
    val genreMediaState: LiveData<GenreMediaState> = _genreMediaState

    private val _topMediaState = MutableLiveData<TopMediaState>()
    val topMediaState: LiveData<TopMediaState> = _topMediaState

    // Local database state
    private val _localState = MutableLiveData<Pair<Set<Int>, Map<Int, String>>>()
    val localState: LiveData<Pair<Set<Int>, Map<Int, String>>> = _localState

    private var localRepository: LocalRepository? = null

    private var currentGenres: List<Genre> = emptyList()
    var currentMediaType: String = "tv"
        private set

    fun init(context: Context) {
        val db = AppDatabase.getDatabase(context)
        localRepository = LocalRepository(db.mediaDao())
        loadLocalData()
    }

    fun loadAllForMedia(mediaType: String) {
        currentMediaType = mediaType
        loadGenres(mediaType)
        loadTrending(mediaType)
        loadTopMedia(mediaType)
    }

    fun loadGenres(mediaType: String) {
        _genresState.value = GenresState.Loading

        viewModelScope.launch {
            val result = mediaRepository.getGenres(mediaType)
            _genresState.value = if (result.isSuccess) {
                currentGenres = result.getOrNull()?.genres ?: emptyList()
                GenresState.Success(currentGenres)
            } else {
                GenresState.Error(result.exceptionOrNull()?.message ?: "Failed to load genres")
            }
        }
    }

    fun loadTrending(mediaType: String) {
        _trendingState.value = TrendingState.Loading

        viewModelScope.launch {
            val result = mediaRepository.getTrending(mediaType)
            _trendingState.value = if (result.isSuccess) {
                val items = result.getOrNull()?.results ?: emptyList()
                TrendingState.Success(items)
            } else {
                TrendingState.Error(result.exceptionOrNull()?.message ?: "Failed to load trending")
            }
        }
    }

    fun loadMediaByGenre(mediaType: String, genreId: Int) {
        _genreMediaState.value = GenreMediaState.Loading

        viewModelScope.launch {
            val result = mediaRepository.getMediaByGenre(mediaType, genreId)
            _genreMediaState.value = if (result.isSuccess) {
                val items = result.getOrNull()?.results ?: emptyList()
                GenreMediaState.Success(items)
            } else {
                GenreMediaState.Error(result.exceptionOrNull()?.message ?: "Failed to load media")
            }
        }
    }

    fun loadTopMedia(mediaType: String) {
        _topMediaState.value = TopMediaState.Loading

        viewModelScope.launch {
            val result = mediaRepository.getTrending(mediaType)
            _topMediaState.value = if (result.isSuccess) {
                val items = result.getOrNull()?.results ?: emptyList()
                TopMediaState.Success(items.firstOrNull())
            } else {
                TopMediaState.Error(result.exceptionOrNull()?.message ?: "Failed to load top media")
            }
        }
    }

    fun getCurrentGenres(): List<Genre> = currentGenres

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