package com.InvalidHamdy.moviezshow.data.repository

import com.InvalidHamdy.moviezshow.data.response.GenreResponse
import com.InvalidHamdy.moviezshow.data.response.MediaResponse
import com.InvalidHamdy.moviezshow.data.response.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository {
    private val apiKey = "093224ec3a7dea80578772862bb63ea8"
    private val api = RetrofitClient.instance

    suspend fun getGenres(mediaType: String): Result<GenreResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (mediaType == "movie") {
                    api.getMovieGenres(apiKey).execute()
                } else {
                    api.getTvGenres(apiKey).execute()
                }

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch genres: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTrending(mediaType: String): Result<MediaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getTrending(mediaType, apiKey).execute()

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch trending: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getMediaByGenre(mediaType: String, genreId: Int): Result<MediaResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (mediaType == "movie") {
                    api.discoverMoviesByGenre(apiKey, genreId).execute()
                } else {
                    api.discoverTvByGenre(apiKey, genreId).execute()
                }

                if (response.isSuccessful && response.body() != null) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("Failed to fetch media by genre: ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}