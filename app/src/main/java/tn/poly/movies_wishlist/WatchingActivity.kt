package tn.poly.movies_wishlist

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import tn.poly.movies_wishlist.adapter.MovieAdapter
import tn.poly.movies_wishlist.data.MovieDbHelper
import tn.poly.movies_wishlist.databinding.ActivityWatchingBinding

class WatchingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWatchingBinding
    private lateinit var dbHelper: MovieDbHelper
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar with back button
        setSupportActionBar(binding.toolbarWatching)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Now Watching"

        dbHelper = MovieDbHelper(this)
        setupRecycler()
    }

    override fun onResume() {
        super.onResume()
        loadItems()
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

    private fun setupRecycler() {
        adapter = MovieAdapter(
            onItemClick = { movie ->
                startActivity(
                    Intent(this, MovieDetailActivity::class.java)
                        .putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, movie.id)
                )
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

        binding.recyclerWatching.layoutManager = LinearLayoutManager(this)
        binding.recyclerWatching.adapter = adapter
    }

    private fun loadItems() {
        val items = dbHelper.getWatchingItems()
        adapter.submitList(items)
        binding.tvEmptyWatching.visibility =
            if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}
