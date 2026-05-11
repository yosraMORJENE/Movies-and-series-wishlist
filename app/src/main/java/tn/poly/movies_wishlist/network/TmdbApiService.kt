package tn.poly.movies_wishlist.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApiService {
    @GET("search/multi")
    fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String
    ): Call<TmdbSearchResponse>

    @GET("movie/{movie_id}")
    fun getMovieDetails(
        @retrofit2.http.Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<TmdbMovieDetailsResponse>

    @GET("tv/{tv_id}")
    fun getTvDetails(
        @retrofit2.http.Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): Call<TmdbTvDetailsResponse>
}

data class TmdbSearchResponse(
    val results: List<TmdbResult> = emptyList()
)

data class TmdbResult(
    val media_type: String? = null,
    val title: String? = null,
    val name: String? = null,
    val overview: String? = null,
    val release_date: String? = null,
    val first_air_date: String? = null,
    val id: Int? = null
)

data class TmdbGenre(
    val id: Int? = null,
    val name: String? = null
)

data class TmdbMovieDetailsResponse(
    val title: String? = null,
    val release_date: String? = null,
    val overview: String? = null,
    val genres: List<TmdbGenre> = emptyList()
)

data class TmdbTvDetailsResponse(
    val name: String? = null,
    val first_air_date: String? = null,
    val overview: String? = null,
    val genres: List<TmdbGenre> = emptyList(),
    val number_of_episodes: Int? = null
)
