package tn.poly.movies_wishlist.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val tmdbRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val quoteRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://dummyjson.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val tmdbApi: TmdbApiService by lazy {
        tmdbRetrofit.create(TmdbApiService::class.java)
    }

    val quoteApi: QuoteApiService by lazy {
        quoteRetrofit.create(QuoteApiService::class.java)
    }
}
