package tn.poly.movies_wishlist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tn.poly.movies_wishlist.adapter.MovieAdapter
import tn.poly.movies_wishlist.data.MovieDbHelper
import tn.poly.movies_wishlist.databinding.ActivityMainBinding
import tn.poly.movies_wishlist.model.MediaItem
import tn.poly.movies_wishlist.network.QuoteResponse
import tn.poly.movies_wishlist.network.RetrofitClient
import tn.poly.movies_wishlist.notifications.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dbHelper: MovieDbHelper
    private lateinit var adapter: MovieAdapter

    private var currentQuery = ""
    private var currentFilter = FilterType.ALL
    private var sortByReleaseDate = false

    private val addMovieLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            loadItems()
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = MovieDbHelper(this)
        NotificationHelper.createChannel(this)
        requestNotificationPermissionIfNeeded()

        setupRecycler()
        setupUi()
        fetchMotivationQuote()
        loadItems()
    }

    override fun onResume() {
        super.onResume()
        loadItems()
        maybeNotifyFirstUnwatched()
    }

    private fun setupRecycler() {
        adapter = MovieAdapter(
            onItemClick = { item ->
                val intent = Intent(this, MovieDetailActivity::class.java)
                intent.putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, item.id)
                startActivity(intent)
            },
            onWatchedToggle = { item, isChecked ->
                dbHelper.updateWatched(item.id, isChecked)
                loadItems()
            },
            onFavoriteToggle = { item ->
                dbHelper.updateFavorite(item.id, !item.isFavorite)
                loadItems()
            }
        )

        binding.recyclerMovies.layoutManager = LinearLayoutManager(this)
        binding.recyclerMovies.adapter = adapter
    }

    private fun setupUi() {
        binding.btnAddMovie.setOnClickListener {
            addMovieLauncher.launch(Intent(this, AddMovieActivity::class.java))
        }

        binding.btnMovieList.setOnClickListener {
            // Already on movie list, just reset filters
            currentFilter = FilterType.ALL
            currentQuery = ""
            binding.searchMovies.setQuery("", false)
            loadItems()
        }

        binding.btnFavorites.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        binding.btnWatching.setOnClickListener {
            startActivity(Intent(this, WatchingActivity::class.java))
        }

        binding.searchMovies.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentQuery = query.orEmpty()
                loadItems()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText.orEmpty()
                loadItems()
                return true
            }
        })

        binding.chipAll.setOnClickListener {
            currentFilter = FilterType.ALL
            loadItems()
        }

        binding.chipWatched.setOnClickListener {
            currentFilter = FilterType.WATCHED
            loadItems()
        }

        binding.chipFavorites.setOnClickListener {
            currentFilter = FilterType.FAVORITES
            loadItems()
        }

        binding.switchSortDate.setOnCheckedChangeListener { _, isChecked ->
            sortByReleaseDate = isChecked
            loadItems()
        }

        binding.btnRefreshQuote.setOnClickListener {
            fetchMotivationQuote()
        }
    }

    private fun loadItems() {
        val items = dbHelper.getItems(
            query = currentQuery,
            filter = currentFilter,
            sortByReleaseDate = sortByReleaseDate
        )

        adapter.submitList(items)
        binding.tvEmpty.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun fetchMotivationQuote() {
        RetrofitClient.quoteApi.getRandomQuote().enqueue(object : Callback<QuoteResponse> {
            override fun onResponse(call: Call<QuoteResponse>, response: Response<QuoteResponse>) {
                if (response.isSuccessful) {
                    val quote = response.body()
                    if (quote != null) {
                        binding.tvQuote.text = "\"${quote.quote}\" — ${quote.author}"
                    }
                }
            }

            override fun onFailure(call: Call<QuoteResponse>, t: Throwable) {
                binding.tvQuote.text = getString(R.string.quote_fallback)
            }
        })
    }

    private fun maybeNotifyFirstUnwatched() {
        val unwatched = dbHelper.getFirstUnwatchedItem() ?: return
        if (!hasNotifiedInSession) {
            NotificationHelper.showWatchReminder(this, unwatched.title)
            hasNotifiedInSession = true
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    companion object {
        private var hasNotifiedInSession = false
    }
}

enum class FilterType {
    ALL,
    WATCHED,
    FAVORITES
}