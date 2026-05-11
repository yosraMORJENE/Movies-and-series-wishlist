package tn.poly.movies_wishlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tn.poly.movies_wishlist.databinding.ItemMovieBinding
import tn.poly.movies_wishlist.model.MediaItem

class MovieAdapter(
    private val onItemClick: (MediaItem) -> Unit,
    private val onWatchedToggle: (MediaItem, Boolean) -> Unit,
    private val onFavoriteToggle: (MediaItem) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    private val items = mutableListOf<MediaItem>()

    fun submitList(newItems: List<MediaItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class MovieViewHolder(private val binding: ItemMovieBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaItem) {
            binding.tvTitle.text = item.title
            binding.tvGenre.text = item.genre
            binding.tvType.text = item.type
            binding.tvDescription.text = item.description
            binding.tvDescription.visibility =
                if (item.description.isBlank()) View.GONE else View.VISIBLE

            // Show watched icon
            binding.imgWatched.visibility = if (item.isWatched) View.VISIBLE else View.GONE

            // Show watching icon and status
            if (item.isWatching || item.isStartedWatching) {
                binding.imgWatching.visibility = View.VISIBLE
                binding.tvWatchingStatus.text = if (item.isStartedWatching) {
                    item.totalEpisodes?.let { total -> "Started Watching • $total eps" } ?: "Started Watching"
                } else {
                    "Now Watching"
                }
                binding.tvWatchingStatus.visibility = View.VISIBLE
            } else {
                binding.imgWatching.visibility = View.GONE
                binding.tvWatchingStatus.visibility = View.GONE
            }

            binding.checkboxWatched.setOnCheckedChangeListener(null)
            binding.checkboxWatched.isChecked = item.isWatched
            binding.checkboxWatched.setOnCheckedChangeListener { _, isChecked ->
                onWatchedToggle(item, isChecked)
            }

            binding.btnFavorite.setImageResource(
                if (item.isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            binding.btnFavorite.setOnClickListener {
                onFavoriteToggle(item)
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
