package tn.poly.movies_wishlist.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import tn.poly.movies_wishlist.FilterType
import tn.poly.movies_wishlist.model.EpisodeItem
import tn.poly.movies_wishlist.model.MediaItem

class MovieDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_MOVIES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_IS_RELEASED INTEGER NOT NULL,
                $COLUMN_RELEASE_DATE TEXT,
                $COLUMN_EPISODES_COUNT INTEGER,
                $COLUMN_IS_WATCHED INTEGER NOT NULL,
                $COLUMN_IS_FAVORITE INTEGER NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_GENRE TEXT,
                $COLUMN_REVIEW TEXT,
                $COLUMN_IS_WATCHING INTEGER NOT NULL DEFAULT 0,
                $COLUMN_LAST_WATCHED_EPISODE INTEGER NOT NULL DEFAULT 0,
                $COLUMN_IS_ONGOING INTEGER NOT NULL DEFAULT 0,
                $COLUMN_RELEASE_DAY TEXT,
                $COLUMN_IS_STARTED_WATCHING INTEGER NOT NULL DEFAULT 0,
                $COLUMN_TOTAL_EPISODES INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_EPISODES (
                $COLUMN_EPISODE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EPISODE_MOVIE_ID INTEGER NOT NULL,
                $COLUMN_EPISODE_NUMBER INTEGER NOT NULL,
                $COLUMN_EPISODE_WATCHED INTEGER NOT NULL DEFAULT 0,
                FOREIGN KEY($COLUMN_EPISODE_MOVIE_ID) REFERENCES $TABLE_MOVIES($COLUMN_ID) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_GENRE TEXT")
            } catch (e: Exception) { }
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_REVIEW TEXT")
            } catch (e: Exception) { }
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_IS_WATCHING INTEGER NOT NULL DEFAULT 0")
            } catch (e: Exception) { }
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_LAST_EPISODE_WATCHED INTEGER NOT NULL DEFAULT 0")
            } catch (e: Exception) { }
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_IS_ONGOING INTEGER NOT NULL DEFAULT 0")
            } catch (e: Exception) { }
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_RELEASE_DAY TEXT")
            } catch (e: Exception) { }
        }

        if (oldVersion < 3) {
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_IS_STARTED_WATCHING INTEGER NOT NULL DEFAULT 0")
            } catch (e: Exception) { }
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_TOTAL_EPISODES INTEGER")
            } catch (e: Exception) { }
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS $TABLE_EPISODES (
                    $COLUMN_EPISODE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_EPISODE_MOVIE_ID INTEGER NOT NULL,
                    $COLUMN_EPISODE_NUMBER INTEGER NOT NULL,
                    $COLUMN_EPISODE_WATCHED INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY($COLUMN_EPISODE_MOVIE_ID) REFERENCES $TABLE_MOVIES($COLUMN_ID) ON DELETE CASCADE
                )
                """.trimIndent()
            )
        }

        if (oldVersion < 4) {
            try {
                db.execSQL("ALTER TABLE $TABLE_MOVIES ADD COLUMN $COLUMN_LAST_WATCHED_EPISODE INTEGER NOT NULL DEFAULT 0")
            } catch (e: Exception) { }
            try {
                db.execSQL("UPDATE $TABLE_MOVIES SET $COLUMN_LAST_WATCHED_EPISODE = COALESCE($COLUMN_LAST_EPISODE_WATCHED, 0)")
            } catch (e: Exception) { }
        }
    }

    fun insertItem(item: MediaItem): Long {
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_TYPE, item.type)
            put(COLUMN_IS_RELEASED, item.isReleased.toDbInt())
            put(COLUMN_RELEASE_DATE, item.releaseDate)
            put(COLUMN_EPISODES_COUNT, item.episodesCount)
            put(COLUMN_IS_WATCHED, item.isWatched.toDbInt())
            put(COLUMN_IS_FAVORITE, item.isFavorite.toDbInt())
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_GENRE, item.genre)
            put(COLUMN_REVIEW, item.review)
            put(COLUMN_IS_WATCHING, item.isWatching.toDbInt())
            put(COLUMN_LAST_WATCHED_EPISODE, item.lastWatchedEpisode)
            put(COLUMN_IS_ONGOING, item.isOngoing.toDbInt())
            put(COLUMN_RELEASE_DAY, item.releaseDay)
            put(COLUMN_IS_STARTED_WATCHING, item.isStartedWatching.toDbInt())
            put(COLUMN_TOTAL_EPISODES, item.totalEpisodes)
        }
        return writableDatabase.insert(TABLE_MOVIES, null, values)
    }

    fun getItems(query: String, filter: FilterType, sortByReleaseDate: Boolean): List<MediaItem> {
        val db = readableDatabase
        val whereParts = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (query.isNotBlank()) {
            whereParts += "$COLUMN_TITLE LIKE ?"
            args += "%$query%"
        }

        when (filter) {
            FilterType.WATCHED -> whereParts += "$COLUMN_IS_WATCHED = 1"
            FilterType.FAVORITES -> whereParts += "$COLUMN_IS_FAVORITE = 1"
            FilterType.ALL -> Unit
        }

        val whereClause = if (whereParts.isEmpty()) null else whereParts.joinToString(" AND ")
        val orderBy = if (sortByReleaseDate) "$COLUMN_RELEASE_DATE ASC" else "$COLUMN_ID DESC"

        val cursor = db.query(
            TABLE_MOVIES,
            null,
            whereClause,
            if (args.isEmpty()) null else args.toTypedArray(),
            null,
            null,
            orderBy
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(it.toItem())
                }
            }
        }
    }

    fun getFavoriteOrWatchedItems(): List<MediaItem> {
        val cursor = readableDatabase.query(
            TABLE_MOVIES,
            null,
            "$COLUMN_IS_FAVORITE = 1 OR $COLUMN_IS_WATCHED = 1",
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(it.toItem())
                }
            }
        }
    }

    fun getItemById(id: Long): MediaItem? {
        val cursor = readableDatabase.query(
            TABLE_MOVIES,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) it.toItem() else null
        }
    }

    fun updateWatched(id: Long, isWatched: Boolean): Int {
        val values = ContentValues().apply {
            put(COLUMN_IS_WATCHED, isWatched.toDbInt())
        }
        return writableDatabase.update(TABLE_MOVIES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateFavorite(id: Long, isFavorite: Boolean): Int {
        val values = ContentValues().apply {
            put(COLUMN_IS_FAVORITE, isFavorite.toDbInt())
        }
        return writableDatabase.update(TABLE_MOVIES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateWatching(id: Long, isWatching: Boolean): Int {
        val values = ContentValues().apply {
            put(COLUMN_IS_WATCHING, isWatching.toDbInt())
        }
        return writableDatabase.update(TABLE_MOVIES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateStartedWatching(id: Long, startedWatching: Boolean, totalEpisodes: Int? = null): Int {
        val values = ContentValues().apply {
            put(COLUMN_IS_STARTED_WATCHING, startedWatching.toDbInt())
            put(COLUMN_TOTAL_EPISODES, totalEpisodes)
        }
        return writableDatabase.update(TABLE_MOVIES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateLastWatchedEpisode(id: Long, episode: Int): Int {
        val item = getItemById(id) ?: return 0
        val totalEpisodes = item.totalEpisodes ?: item.episodesCount ?: 0
        val isCompleted = totalEpisodes > 0 && episode >= totalEpisodes

        val values = ContentValues().apply {
            put(COLUMN_LAST_WATCHED_EPISODE, episode)
            if (item.type == "Series") {
                put(COLUMN_IS_STARTED_WATCHING, 1)
                put(COLUMN_IS_WATCHING, if (isCompleted) 0 else 1)
                put(COLUMN_IS_WATCHED, if (isCompleted) 1 else 0)
            }
        }
        return writableDatabase.update(TABLE_MOVIES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    @Deprecated("Use updateLastWatchedEpisode")
    fun updateLastEpisodeWatched(id: Long, episode: Int): Int {
        return updateLastWatchedEpisode(id, episode)
    }

    fun updateReview(id: Long, review: String): Int {
        val values = ContentValues().apply {
            put(COLUMN_REVIEW, review)
        }
        return writableDatabase.update(TABLE_MOVIES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun insertEpisodes(movieId: Long, totalEpisodes: Int, watchedEpisodes: Set<Int> = emptySet()) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_EPISODES, "$COLUMN_EPISODE_MOVIE_ID = ?", arrayOf(movieId.toString()))
            for (episodeNumber in 1..totalEpisodes) {
                val values = ContentValues().apply {
                    put(COLUMN_EPISODE_MOVIE_ID, movieId)
                    put(COLUMN_EPISODE_NUMBER, episodeNumber)
                    put(COLUMN_EPISODE_WATCHED, if (episodeNumber in watchedEpisodes) 1 else 0)
                }
                db.insert(TABLE_EPISODES, null, values)
            }
            syncSeriesProgress(db, movieId)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getEpisodesByMovieId(movieId: Long): List<EpisodeItem> {
        val cursor = readableDatabase.query(
            TABLE_EPISODES,
            null,
            "$COLUMN_EPISODE_MOVIE_ID = ?",
            arrayOf(movieId.toString()),
            null,
            null,
            "$COLUMN_EPISODE_NUMBER ASC"
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(it.toEpisodeItem())
                }
            }
        }
    }

    fun updateEpisodeWatched(episodeId: Long, isWatched: Boolean): Int {
        val movieId = getEpisodeMovieId(episodeId)
        val values = ContentValues().apply {
            put(COLUMN_EPISODE_WATCHED, isWatched.toDbInt())
        }
        val rows = writableDatabase.update(TABLE_EPISODES, values, "$COLUMN_EPISODE_ID = ?", arrayOf(episodeId.toString()))
        if (movieId != null) {
            syncSeriesProgress(writableDatabase, movieId)
        }
        return rows
    }

    fun getEpisodeProgress(movieId: Long): Pair<Int, Int> {
        val totalCursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_EPISODES WHERE $COLUMN_EPISODE_MOVIE_ID = ?",
            arrayOf(movieId.toString())
        )
        val watchedCursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_EPISODES WHERE $COLUMN_EPISODE_MOVIE_ID = ? AND $COLUMN_EPISODE_WATCHED = 1",
            arrayOf(movieId.toString())
        )

        val total = totalCursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
        val watched = watchedCursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }
        return watched to total
    }

    fun getWatchingItems(): List<MediaItem> {
        val cursor = readableDatabase.query(
            TABLE_MOVIES,
            null,
            "$COLUMN_IS_WATCHING = 1",
            null,
            null,
            null,
            "$COLUMN_ID DESC"
        )

        return cursor.use {
            buildList {
                while (it.moveToNext()) {
                    add(it.toItem())
                }
            }
        }
    }

    fun getFirstUnwatchedItem(): MediaItem? {
        val cursor = readableDatabase.query(
            TABLE_MOVIES,
            null,
            "$COLUMN_IS_WATCHED = 0",
            null,
            null,
            null,
            "$COLUMN_ID DESC",
            "1"
        )

        return cursor.use {
            if (it.moveToFirst()) it.toItem() else null
        }
    }

    private fun android.database.Cursor.toItem(): MediaItem {
        return MediaItem(
            id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
            title = getString(getColumnIndexOrThrow(COLUMN_TITLE)),
            type = getString(getColumnIndexOrThrow(COLUMN_TYPE)),
            isReleased = getInt(getColumnIndexOrThrow(COLUMN_IS_RELEASED)) == 1,
            releaseDate = getString(getColumnIndexOrThrow(COLUMN_RELEASE_DATE)),
            episodesCount = if (isNull(getColumnIndexOrThrow(COLUMN_EPISODES_COUNT))) {
                null
            } else {
                getInt(getColumnIndexOrThrow(COLUMN_EPISODES_COUNT))
            },
            isWatched = getInt(getColumnIndexOrThrow(COLUMN_IS_WATCHED)) == 1,
            isFavorite = getInt(getColumnIndexOrThrow(COLUMN_IS_FAVORITE)) == 1,
            description = getString(getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            genre = getString(getColumnIndexOrThrow(COLUMN_GENRE)) ?: "Not Set",
            review = getString(getColumnIndexOrThrow(COLUMN_REVIEW)) ?: "",
            isWatching = getInt(getColumnIndexOrThrow(COLUMN_IS_WATCHING)) == 1,
            lastWatchedEpisode = getInt(getColumnIndexOrThrow(COLUMN_LAST_WATCHED_EPISODE)),
            isOngoing = getInt(getColumnIndexOrThrow(COLUMN_IS_ONGOING)) == 1,
            releaseDay = getString(getColumnIndexOrThrow(COLUMN_RELEASE_DAY)),
            isStartedWatching = getInt(getColumnIndexOrThrow(COLUMN_IS_STARTED_WATCHING)) == 1,
            totalEpisodes = if (isNull(getColumnIndexOrThrow(COLUMN_TOTAL_EPISODES))) {
                null
            } else {
                getInt(getColumnIndexOrThrow(COLUMN_TOTAL_EPISODES))
            }
        )
    }

    private fun android.database.Cursor.toEpisodeItem(): EpisodeItem {
        return EpisodeItem(
            id = getLong(getColumnIndexOrThrow(COLUMN_EPISODE_ID)),
            movieId = getLong(getColumnIndexOrThrow(COLUMN_EPISODE_MOVIE_ID)),
            episodeNumber = getInt(getColumnIndexOrThrow(COLUMN_EPISODE_NUMBER)),
            isWatched = getInt(getColumnIndexOrThrow(COLUMN_EPISODE_WATCHED)) == 1
        )
    }

    private fun getEpisodeMovieId(episodeId: Long): Long? {
        val cursor = readableDatabase.query(
            TABLE_EPISODES,
            arrayOf(COLUMN_EPISODE_MOVIE_ID),
            "$COLUMN_EPISODE_ID = ?",
            arrayOf(episodeId.toString()),
            null,
            null,
            null
        )

        return cursor.use {
            if (it.moveToFirst()) it.getLong(0) else null
        }
    }

    private fun syncSeriesProgress(db: SQLiteDatabase, movieId: Long) {
        val item = getItemById(movieId) ?: return
        val episodes = getEpisodesByMovieId(movieId)
        val lastWatchedEpisode = episodes.filter { it.isWatched }.maxOfOrNull { it.episodeNumber } ?: 0
        val totalEpisodes = item.totalEpisodes ?: item.episodesCount ?: episodes.size
        val isFinished = totalEpisodes > 0 && lastWatchedEpisode >= totalEpisodes

        val values = ContentValues().apply {
            put(COLUMN_LAST_WATCHED_EPISODE, lastWatchedEpisode)
            put(COLUMN_IS_STARTED_WATCHING, if (lastWatchedEpisode > 0) 1 else item.isStartedWatching.toDbInt())
            put(COLUMN_IS_WATCHING, if (isFinished) 0 else if (lastWatchedEpisode > 0) 1 else item.isWatching.toDbInt())
            put(COLUMN_IS_WATCHED, if (isFinished) 1 else item.isWatched.toDbInt())
        }
        db.update(TABLE_MOVIES, values, "$COLUMN_ID = ?", arrayOf(movieId.toString()))
    }

    private fun Boolean.toDbInt(): Int = if (this) 1 else 0

    companion object {
        private const val DATABASE_NAME = "movies_wishlist.db"
        private const val DATABASE_VERSION = 4

        const val TABLE_MOVIES = "movies"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_TYPE = "type"
        const val COLUMN_IS_RELEASED = "isReleased"
        const val COLUMN_RELEASE_DATE = "releaseDate"
        const val COLUMN_EPISODES_COUNT = "episodesCount"
        const val COLUMN_IS_WATCHED = "isWatched"
        const val COLUMN_IS_FAVORITE = "isFavorite"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_GENRE = "genre"
        const val COLUMN_REVIEW = "review"
        const val COLUMN_IS_WATCHING = "isWatching"
        const val COLUMN_LAST_EPISODE_WATCHED = "lastEpisodeWatched"
        const val COLUMN_LAST_WATCHED_EPISODE = "lastWatchedEpisode"
        const val COLUMN_IS_ONGOING = "isOngoing"
        const val COLUMN_RELEASE_DAY = "releaseDay"
        const val COLUMN_IS_STARTED_WATCHING = "isStartedWatching"
        const val COLUMN_TOTAL_EPISODES = "totalEpisodes"

        const val TABLE_EPISODES = "episodes"
        const val COLUMN_EPISODE_ID = "id"
        const val COLUMN_EPISODE_MOVIE_ID = "movieId"
        const val COLUMN_EPISODE_NUMBER = "episodeNumber"
        const val COLUMN_EPISODE_WATCHED = "isWatched"
    }
}
