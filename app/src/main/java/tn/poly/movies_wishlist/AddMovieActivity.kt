package tn.poly.movies_wishlist

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import tn.poly.movies_wishlist.data.MovieDbHelper
import tn.poly.movies_wishlist.databinding.ActivityAddMovieBinding
import tn.poly.movies_wishlist.model.MediaItem
import tn.poly.movies_wishlist.notifications.NotificationScheduler

class AddMovieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMovieBinding
    private lateinit var dbHelper: MovieDbHelper

    private val genres = listOf("Not Set", "Romance", "Horror", "Comedy", "Action", "Drama", "Sci-Fi", "Thriller")
    private val releaseDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val episodeLabels = mutableListOf<String>()
    private lateinit var episodeDropdownAdapter: ArrayAdapter<String>

    private var selectedLastWatchedEpisode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarAddMovie)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.add_new_item)

        dbHelper = MovieDbHelper(this)
        setupSpinners()
        setupEpisodeDropdown()
        setupListeners()
        updateSeriesUiVisibility()
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

    private fun setupSpinners() {
        val genreAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genres)
        genreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGenre.adapter = genreAdapter

        val daysAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, releaseDays)
        daysAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerReleaseDay.adapter = daysAdapter
    }

    private fun setupEpisodeDropdown() {
        episodeDropdownAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, episodeLabels)
        binding.dropdownLastWatchedEpisode.setAdapter(episodeDropdownAdapter)
        binding.dropdownLastWatchedEpisode.setOnItemClickListener { _, _, position, _ ->
            selectedLastWatchedEpisode = position + 1
            updateSeriesProgressViews()
        }
    }

    private fun setupListeners() {
        binding.radioGroupType.setOnCheckedChangeListener { _, _ ->
            updateSeriesUiVisibility()
        }

        binding.checkboxReleased.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutReleaseDate.visibility = if (isChecked) View.GONE else View.VISIBLE
            updateOngoingVisibility()
        }

        binding.checkboxStartedWatching.setOnCheckedChangeListener { _, isChecked ->
            updateEpisodeSelectorVisibility()
            if (!isChecked) {
                selectedLastWatchedEpisode = 0
                binding.dropdownLastWatchedEpisode.setText("", false)
                binding.checkboxWatched.isChecked = false
            }
        }

        binding.etTotalEpisodes.doAfterTextChanged {
            updateEpisodeSelectorVisibility()
        }

        binding.checkboxOngoing.setOnCheckedChangeListener { _, _ ->
            updateOngoingVisibility()
        }

        binding.btnSave.setOnClickListener {
            saveMovieOrSeries()
        }
    }

    private fun updateSeriesUiVisibility() {
        val isSeries = binding.radioSeries.isChecked
        binding.layoutSeriesFields.visibility = if (isSeries) View.VISIBLE else View.GONE
        binding.checkboxStartedWatching.visibility = if (isSeries) View.VISIBLE else View.GONE
        binding.checkboxOngoing.visibility = if (isSeries) View.VISIBLE else View.GONE
        if (!isSeries) {
            binding.checkboxStartedWatching.isChecked = false
            binding.checkboxOngoing.isChecked = false
            selectedLastWatchedEpisode = 0
            binding.dropdownLastWatchedEpisode.setText("", false)
            hideSeriesProgressViews()
        }
        updateOngoingVisibility()
        updateEpisodeSelectorVisibility()
    }

    private fun updateOngoingVisibility() {
        val showOngoing = binding.radioSeries.isChecked && binding.checkboxOngoing.isChecked
        binding.layoutReleaseDay.visibility = if (showOngoing) View.VISIBLE else View.GONE
        binding.tvOngoingStatus.visibility = if (binding.radioSeries.isChecked) View.VISIBLE else View.GONE
        binding.tvReleaseDaySummary.visibility = if (showOngoing) View.VISIBLE else View.GONE

        if (binding.radioSeries.isChecked) {
            binding.tvOngoingStatus.text = getString(
                R.string.ongoing_status_value,
                if (binding.checkboxOngoing.isChecked) getString(R.string.yes_text) else getString(R.string.no_text)
            )
        }

        updateReleaseDaySummary()
    }

    private fun updateEpisodeSelectorVisibility() {
        val isSeries = binding.radioSeries.isChecked
        val startedWatching = isSeries && binding.checkboxStartedWatching.isChecked
        val totalEpisodes = binding.etTotalEpisodes.text?.toString()?.toIntOrNull() ?: 0
        val shouldShow = startedWatching && totalEpisodes > 0

        binding.layoutEpisodeSelector.visibility = if (shouldShow) View.VISIBLE else View.GONE
        binding.tvEpisodeProgress.visibility = if (shouldShow) View.VISIBLE else View.GONE
        binding.progressEpisode.visibility = if (shouldShow) View.VISIBLE else View.GONE
        binding.tvLastWatchedEpisode.visibility = if (shouldShow) View.VISIBLE else View.GONE
        binding.tvSeriesStatus.visibility = if (shouldShow) View.VISIBLE else View.GONE

        if (shouldShow) {
            populateEpisodeDropdown(totalEpisodes)
        } else {
            selectedLastWatchedEpisode = 0
            binding.dropdownLastWatchedEpisode.setText("", false)
            hideSeriesProgressViews()
        }
    }

    private fun populateEpisodeDropdown(totalEpisodes: Int) {
        episodeLabels.clear()
        episodeLabels.addAll((1..totalEpisodes).map { "Episode $it" })
        episodeDropdownAdapter.notifyDataSetChanged()

        if (selectedLastWatchedEpisode <= 0) {
            selectedLastWatchedEpisode = 1
        } else {
            selectedLastWatchedEpisode = selectedLastWatchedEpisode.coerceIn(1, totalEpisodes)
        }

        binding.dropdownLastWatchedEpisode.setText("Episode $selectedLastWatchedEpisode", false)
        updateSeriesProgressViews()
    }

    private fun updateSeriesProgressViews() {
        val totalEpisodes = binding.etTotalEpisodes.text?.toString()?.toIntOrNull() ?: 0
        val watchedEpisodes = if (binding.radioSeries.isChecked && binding.checkboxStartedWatching.isChecked) {
            selectedLastWatchedEpisode.coerceAtLeast(0)
        } else {
            0
        }
        val isCompleted = totalEpisodes > 0 && watchedEpisodes >= totalEpisodes

        if (binding.radioSeries.isChecked && binding.checkboxStartedWatching.isChecked && totalEpisodes > 0) {
            binding.tvEpisodeProgress.text = getString(R.string.episode_progress, watchedEpisodes, totalEpisodes)
            binding.progressEpisode.max = totalEpisodes
            binding.progressEpisode.progress = watchedEpisodes.coerceIn(0, totalEpisodes)
            binding.tvLastWatchedEpisode.text = if (watchedEpisodes > 0) {
                getString(R.string.last_episode_value, watchedEpisodes)
            } else {
                getString(R.string.series_status_not_started)
            }
            binding.tvSeriesStatus.text = when {
                isCompleted -> getString(R.string.series_status_completed)
                watchedEpisodes > 0 -> getString(R.string.series_status_watching)
                else -> getString(R.string.series_status_not_started)
            }
            binding.checkboxWatched.isChecked = isCompleted
        } else {
            hideSeriesProgressViews()
        }

        if (binding.radioSeries.isChecked) {
            updateReleaseDaySummary()
            updateOngoingVisibility()
        }
    }

    private fun updateReleaseDaySummary() {
        val showOngoing = binding.radioSeries.isChecked && binding.checkboxOngoing.isChecked
        if (!showOngoing) {
            binding.tvReleaseDaySummary.visibility = View.GONE
            return
        }

        val releaseDay = binding.spinnerReleaseDay.selectedItem?.toString().orEmpty()
        binding.tvReleaseDaySummary.visibility = View.VISIBLE
        binding.tvReleaseDaySummary.text = getString(R.string.release_day_value, releaseDay)
    }

    private fun hideSeriesProgressViews() {
        binding.tvEpisodeProgress.text = getString(R.string.episode_progress, 0, 0)
        binding.progressEpisode.max = 100
        binding.progressEpisode.progress = 0
        binding.tvLastWatchedEpisode.text = getString(R.string.series_status_not_started)
        binding.tvSeriesStatus.text = getString(R.string.series_status_not_started)
        binding.tvEpisodeProgress.visibility = View.GONE
        binding.progressEpisode.visibility = View.GONE
        binding.tvLastWatchedEpisode.visibility = View.GONE
        binding.tvSeriesStatus.visibility = View.GONE
    }

    private fun saveMovieOrSeries() {
        val title = binding.etTitle.text?.toString().orEmpty().trim()
        if (title.isBlank()) {
            binding.etTitle.error = getString(R.string.required_field)
            return
        }

        val isSeries = binding.radioSeries.isChecked
        val startedWatching = binding.checkboxStartedWatching.isChecked
        val totalEpisodes = binding.etTotalEpisodes.text?.toString()?.toIntOrNull()
        val lastWatchedEpisode = if (isSeries && startedWatching) selectedLastWatchedEpisode else 0

        if (isSeries && (totalEpisodes == null || totalEpisodes <= 0)) {
            binding.etTotalEpisodes.error = getString(R.string.episodes_required)
            return
        }

        if (isSeries && startedWatching && selectedLastWatchedEpisode <= 0) {
            Toast.makeText(this, R.string.select_episode, Toast.LENGTH_SHORT).show()
            return
        }

        val isSeriesCompleted = isSeries && startedWatching && totalEpisodes != null && lastWatchedEpisode >= totalEpisodes

        val item = MediaItem(
            title = title,
            type = if (isSeries) "Series" else "Movie",
            isReleased = binding.checkboxReleased.isChecked,
            releaseDate = binding.etReleaseDate.text?.toString()?.trim().takeIf { !it.isNullOrBlank() },
            episodesCount = totalEpisodes,
            isWatched = if (isSeries) isSeriesCompleted else binding.checkboxWatched.isChecked,
            isFavorite = binding.checkboxFavorite.isChecked,
            description = binding.etDescription.text?.toString().orEmpty().trim(),
            genre = binding.spinnerGenre.selectedItem?.toString().orEmpty(),
            review = binding.etReview.text?.toString().orEmpty().trim(),
            isWatching = if (isSeries) startedWatching && !isSeriesCompleted else false,
            isStartedWatching = startedWatching,
            totalEpisodes = totalEpisodes,
            lastWatchedEpisode = if (isSeries) lastWatchedEpisode else 0,
            isOngoing = isSeries && binding.checkboxOngoing.isChecked && !isSeriesCompleted,
            releaseDay = if (isSeries && binding.checkboxOngoing.isChecked && !isSeriesCompleted) {
                binding.spinnerReleaseDay.selectedItem?.toString()
            } else {
                null
            }
        )

        val movieId = dbHelper.insertItem(item)
        if (movieId > 0 && isSeries && startedWatching && totalEpisodes != null && totalEpisodes > 0) {
            dbHelper.insertEpisodes(movieId, totalEpisodes, (1..lastWatchedEpisode).toSet())
        }

        if (isSeries && item.isOngoing && !item.releaseDay.isNullOrBlank()) {
            NotificationScheduler.scheduleEpisodeNotification(this, movieId, title, item.releaseDay)
        }

        setResult(RESULT_OK)
        finish()
    }

    private fun View.setVisibleAnimated(visible: Boolean) {
        if (visible) {
            if (visibility != View.VISIBLE) {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(160L).start()
            }
        } else if (visibility == View.VISIBLE) {
            animate().alpha(0f).setDuration(160L).withEndAction {
                visibility = View.GONE
                alpha = 1f
            }.start()
        }
    }
}
