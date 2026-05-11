package tn.poly.movies_wishlist.model

data class MediaItem(
    val id: Long = 0,
    val title: String,
    val type: String,
    val isReleased: Boolean,
    val releaseDate: String?,
    val episodesCount: Int?,
    val isWatched: Boolean,
    val isFavorite: Boolean,
    val description: String,
    val genre: String = "Not Set",
    val review: String = "",
    val isWatching: Boolean = false,
    val lastWatchedEpisode: Int = 0,
    val isOngoing: Boolean = false,
    val releaseDay: String? = null,
    val isStartedWatching: Boolean = false,
    val totalEpisodes: Int? = episodesCount
) {
    val lastEpisodeWatched: Int
        get() = lastWatchedEpisode
}
