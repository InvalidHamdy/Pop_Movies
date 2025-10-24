package com.InvalidHamdy.moviezshow
import retrofit2.Call
import retrofit2.Response
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
}