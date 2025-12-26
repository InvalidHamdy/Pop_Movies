package com.InvalidHamdy.moviezshow.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.InvalidHamdy.moviezshow.data.local.AppDatabase
import com.InvalidHamdy.moviezshow.data.local.MediaEntity
import com.InvalidHamdy.moviezshow.data.repository.LocalRepository
import com.InvalidHamdy.moviezshow.data.response.MediaItem
import kotlinx.coroutines.launch

sealed class FavListState {
    object Loading : FavListState()
    data class Success(val items: List<MediaItem>) : FavListState()
    data class Empty(val message: String) : FavListState()
    data class Error(val message: String) : FavListState()
}

class FavViewModel : ViewModel() {

    private val _listState = MutableLiveData<FavListState>()
    val listState: LiveData<FavListState> = _listState

    private var localRepository: LocalRepository? = null
    private var currentFilter: String = "favorites" // favorites, completed, dropped, watch_later

    fun init(context: Context) {
        val db = AppDatabase.getDatabase(context)
        localRepository = LocalRepository(db.mediaDao())
        loadList("favorites") // Default to favorites
    }

    fun loadList(filter: String) {
        currentFilter = filter
        _listState.value = FavListState.Loading

        viewModelScope.launch {
            try {
                val entities = when (filter) {
                    "favorites" -> localRepository?.getAllMedia()?.filter { it.isFavorite } ?: emptyList()
                    "completed" -> localRepository?.getAllMedia()?.filter { it.saveList == "completed" } ?: emptyList()
                    "dropped" -> localRepository?.getAllMedia()?.filter { it.saveList == "dropped" } ?: emptyList()
                    "watch_later" -> localRepository?.getAllMedia()?.filter { it.saveList == "watch_later" } ?: emptyList()
                    else -> emptyList()
                }

                if (entities.isEmpty()) {
                    _listState.value = FavListState.Empty("No items in this list")
                } else {
                    // Convert MediaEntity to MediaItem
                    val mediaItems = entities.map { entity ->
                        MediaItem(
                            id = entity.id,
                            title = if (entity.mediaType == "movie") entity.title else null,
                            name = if (entity.mediaType == "tv") entity.title else null,
                            poster_path = entity.posterPath,
                            overview = null,
                            backdrop_path = null,
                            number_of_seasons = null,
                            first_air_date = null,
                            release_date = null
                        )
                    }
                    _listState.value = FavListState.Success(mediaItems)
                }
            } catch (e: Exception) {
                _listState.value = FavListState.Error(e.message ?: "Failed to load list")
            }
        }
    }

    fun toggleFavorite(item: MediaItem) {
        viewModelScope.launch {
            localRepository?.toggleFavorite(
                item.id,
                item.title ?: item.name ?: "",
                item.poster_path,
                "movie" // Default, could be improved
            )
            // Reload current list
            loadList(currentFilter)
        }
    }

    fun updateSaveList(item: MediaItem, listName: String?) {
        viewModelScope.launch {
            localRepository?.updateSaveList(
                item.id,
                item.title ?: item.name ?: "",
                item.poster_path,
                "movie", // Default, could be improved
                listName
            )
            // Reload current list
            loadList(currentFilter)
        }
    }
}
