package tn.poly.movies_wishlist.model

data class EpisodeItem(
    val id: Long = 0,
    val movieId: Long,
    val episodeNumber: Int,
    val isWatched: Boolean
)
