package tn.poly.movies_wishlist

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import tn.poly.movies_wishlist.adapter.MovieAdapter
import tn.poly.movies_wishlist.data.MovieDbHelper
import tn.poly.movies_wishlist.databinding.ActivityFavoritesBinding

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var dbHelper: MovieDbHelper
    private lateinit var adapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar with back button
        setSupportActionBar(binding.toolbarFavorites)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.favorites)

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
            onItemClick = { item ->
                startActivity(
                    Intent(this, MovieDetailActivity::class.java)
                        .putExtra(MovieDetailActivity.EXTRA_MOVIE_ID, item.id)
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

        binding.recyclerFavorites.layoutManager = LinearLayoutManager(this)
        binding.recyclerFavorites.adapter = adapter
    }

    private fun loadItems() {
        val items = dbHelper.getFavoriteOrWatchedItems()
        adapter.submitList(items)
        binding.tvEmptyFavorites.visibility =
            if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }
}
