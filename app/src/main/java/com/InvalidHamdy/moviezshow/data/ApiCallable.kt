package com.InvalidHamdy.moviezshow.data

import com.InvalidHamdy.moviezshow.data.response.CreditsResponse
import com.InvalidHamdy.moviezshow.data.response.GenreResponse
import com.InvalidHamdy.moviezshow.data.response.MediaResponse
import com.InvalidHamdy.moviezshow.data.response.VideoResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiCallable {

    // movie genres
    @GET("genre/movie/list")
    fun getMovieGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Call<GenreResponse>

    // tv genres
    @GET("genre/tv/list")
    fun getTvGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US"
    ): Call<GenreResponse>

    @GET("trending/{media_type}/week")
    fun getTrending(
        @Path("media_type") mediaType: String,
        @Query("api_key") apiKey: String
    ): Call<MediaResponse>

    // discover movie by genre id
    @GET("discover/movie")
    fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Call<MediaResponse>

    // discover tv by genre id
    @GET("discover/tv")
    fun discoverTvByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Call<MediaResponse>
    @GET("movie/{movie_id}/credits")

    fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<CreditsResponse>

    @GET("tv/{tv_id}/credits")
    fun getTvCredits(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): Call<CreditsResponse>
    // videos (trailers) for movie
    @GET("movie/{movie_id}/videos")
    fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<VideoResponse>

    // videos (trailers) for tv
    @GET("tv/{tv_id}/videos")
    fun getTvVideos(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): Call<VideoResponse>

    // Search movies
    @GET("search/movie")
    fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Call<MediaResponse>

    // Search tv
    @GET("search/tv")
    fun searchTv(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US"
    ): Call<MediaResponse>

    // Discover movies (for filters)
    @GET("discover/movie")
    fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: String? = null,
        @Query("primary_release_year") year: String? = null,
        @Query("vote_average.gte") rating: Float? = null,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Call<MediaResponse>

    // Discover tv (for filters)
    @GET("discover/tv")
    fun discoverTv(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: String? = null,
        @Query("first_air_date_year") year: String? = null,
        @Query("vote_average.gte") rating: Float? = null,
        @Query("page") page: Int = 1,
        @Query("language") language: String = "en-US",
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): Call<MediaResponse>
}