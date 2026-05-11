package tn.poly.movies_wishlist

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import tn.poly.movies_wishlist.adapter.EpisodeAdapter
import tn.poly.movies_wishlist.data.MovieDbHelper
import tn.poly.movies_wishlist.databinding.ActivityMovieDetailBinding
import tn.poly.movies_wishlist.model.EpisodeItem
import tn.poly.movies_wishlist.model.MediaItem
import tn.poly.movies_wishlist.notifications.NotificationHelper

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var dbHelper: MovieDbHelper
    private lateinit var episodeAdapter: EpisodeAdapter

    private var movieId: Long = -1L
    private var currentItem: MediaItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarDetail)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dbHelper = MovieDbHelper(this)
        movieId = intent.getLongExtra(EXTRA_MOVIE_ID, -1L)

        if (movieId <= 0L) {
            Toast.makeText(this, R.string.invalid_item, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupEpisodeRecycler()
        setupListeners()
        loadItem()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadItem()
    }

    private fun setupEpisodeRecycler() {
        episodeAdapter = EpisodeAdapter { episode, isChecked ->
            dbHelper.updateEpisodeWatched(episode.id, isChecked)
            loadItem()
        }
        binding.recyclerEpisodes.layoutManager = LinearLayoutManager(this)
        binding.recyclerEpisodes.adapter = episodeAdapter
    }

    private fun setupListeners() {
        binding.btnToggleWatched.setOnClickListener {
            val item = currentItem ?: return@setOnClickListener
            dbHelper.updateWatched(item.id, !item.isWatched)
            loadItem()
        }

        binding.btnToggleFavorite.setOnClickListener {
            val item = currentItem ?: return@setOnClickListener
            dbHelper.updateFavorite(item.id, !item.isFavorite)
            loadItem()
        }

        binding.btnStartWatching.setOnClickListener {
            val item = currentItem ?: return@setOnClickListener
            val started = !item.isStartedWatching
            dbHelper.updateStartedWatching(item.id, started, item.totalEpisodes ?: item.episodesCount)
            dbHelper.updateWatching(item.id, started)
            if (started && (item.totalEpisodes ?: item.episodesCount ?: 0) > 0) {
                dbHelper.insertEpisodes(item.id, item.totalEpisodes ?: item.episodesCount ?: 0, emptySet())
            }
            loadItem()
        }

        binding.btnRemind.setOnClickListener {
            val item = currentItem ?: return@setOnClickListener
            if (item.isWatched) {
                Toast.makeText(this, R.string.already_watched_no_reminder, Toast.LENGTH_SHORT).show()
            } else {
                NotificationHelper.showWatchReminder(this, item.title)
                Toast.makeText(this, R.string.reminder_sent, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnEditReview.setOnClickListener {
            val item = currentItem ?: return@setOnClickListener
            showReviewDialog(item)
        }
    }

    private fun showReviewDialog(item: MediaItem) {
        val input = EditText(this).apply {
            setText(item.review)
            minLines = 4
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.edit_review)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                dbHelper.updateReview(item.id, input.text.toString())
                loadItem()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun loadItem() {
        val item = dbHelper.getItemById(movieId)
        if (item == null) {
            Toast.makeText(this, R.string.item_not_found, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentItem = item
        supportActionBar?.title = item.title

        binding.tvTitle.text = item.title
        binding.tvGenre.text = getString(R.string.genre_value, item.genre)
        binding.tvType.text = getString(R.string.type_value, item.type)
        binding.tvReleased.text = getString(
            R.string.released_value,
            if (item.isReleased) getString(R.string.yes_text) else getString(R.string.no_text)
        )
        binding.tvReleaseDate.text = getString(
            R.string.release_date_value,
            item.releaseDate ?: getString(R.string.not_set)
        )
        binding.tvEpisodes.text = getString(
            R.string.episodes_value,
            (item.totalEpisodes ?: item.episodesCount)?.toString() ?: getString(R.string.not_applicable)
        )
        binding.tvWatched.text = getString(
            R.string.watched_value,
            if (item.isWatched) getString(R.string.yes_text) else getString(R.string.no_text)
        )
        binding.tvFavorite.text = getString(
            R.string.favorite_value,
            if (item.isFavorite) getString(R.string.yes_text) else getString(R.string.no_text)
        )
        binding.tvWatching.text = getString(
            R.string.watching_value,
            if (item.isStartedWatching) getString(R.string.yes_text) else getString(R.string.no_text)
        )
        binding.tvDescription.text = item.description.ifBlank { getString(R.string.no_description) }

        binding.tvReview.text = if (item.review.isBlank()) getString(R.string.no_review) else item.review

        val releaseDayText = item.releaseDay
        if (item.isOngoing && !releaseDayText.isNullOrBlank()) {
            binding.tvReleaseDay.text = getString(R.string.new_episodes_every, releaseDayText)
            binding.tvReleaseDay.visibility = View.VISIBLE
        } else {
            binding.tvReleaseDay.visibility = View.GONE
        }

        binding.btnToggleWatched.text = if (item.isWatched) {
            getString(R.string.mark_unwatched)
        } else {
            getString(R.string.mark_watched)
        }

        binding.btnToggleFavorite.text = if (item.isFavorite) {
            getString(R.string.remove_favorite)
        } else {
            getString(R.string.add_favorite)
        }

        binding.btnStartWatching.text = if (item.isStartedWatching) {
            getString(R.string.stop_watching)
        } else {
            getString(R.string.start_watching)
        }

        if (item.type == "Series" && item.isStartedWatching) {
            val episodes = dbHelper.getEpisodesByMovieId(item.id)
            val progress = dbHelper.getEpisodeProgress(item.id)
            binding.tvEpisodeProgress.visibility = View.VISIBLE
            binding.recyclerEpisodes.visibility = View.VISIBLE
            binding.tvEpisodeProgress.text = getString(R.string.episode_progress, progress.first, progress.second)
            episodeAdapter.submitList(episodes)
        } else {
            binding.tvEpisodeProgress.visibility = View.GONE
            binding.recyclerEpisodes.visibility = View.GONE
            episodeAdapter.submitList(emptyList())
        }
    }

    companion object {
        const val EXTRA_MOVIE_ID = "extra_movie_id"
    }
}
