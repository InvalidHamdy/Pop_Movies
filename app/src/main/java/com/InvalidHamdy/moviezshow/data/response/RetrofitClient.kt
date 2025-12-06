package com.InvalidHamdy.moviezshow.data.response

import com.InvalidHamdy.moviezshow.data.ApiCallable
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitClient {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    val instance: ApiCallable by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiCallable::class.java)
    }
}