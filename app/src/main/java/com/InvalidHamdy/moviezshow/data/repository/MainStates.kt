package com.InvalidHamdy.moviezshow.data.repository

import com.InvalidHamdy.moviezshow.data.response.Genre
import com.InvalidHamdy.moviezshow.data.response.MediaItem

sealed class GenresState {
    object Loading : GenresState()
    data class Success(val genres: List<Genre>) : GenresState()
    data class Error(val message: String) : GenresState()
}

sealed class TrendingState {
    object Loading : TrendingState()
    data class Success(val items: List<MediaItem>) : TrendingState()
    data class Error(val message: String) : TrendingState()
}

sealed class GenreMediaState {
    object Loading : GenreMediaState()
    data class Success(val items: List<MediaItem>) : GenreMediaState()
    data class Error(val message: String) : GenreMediaState()
}

sealed class TopMediaState {
    object Loading : TopMediaState()
    data class Success(val topItem: MediaItem?) : TopMediaState()
    data class Error(val message: String) : TopMediaState()
}