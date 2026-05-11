package tn.poly.movies_wishlist.network

import retrofit2.Call
import retrofit2.http.GET

interface QuoteApiService {
    @GET("quotes/random")
    fun getRandomQuote(): Call<QuoteResponse>
}

data class QuoteResponse(
    val id: Int,
    val quote: String,
    val author: String
)
