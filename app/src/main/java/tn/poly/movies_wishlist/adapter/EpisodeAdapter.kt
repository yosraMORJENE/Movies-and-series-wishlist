package tn.poly.movies_wishlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tn.poly.movies_wishlist.databinding.ItemEpisodeBinding
import tn.poly.movies_wishlist.model.EpisodeItem

class EpisodeAdapter(
    private val onEpisodeCheckedChange: (EpisodeItem, Boolean) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    private val items = mutableListOf<EpisodeItem>()

    fun submitList(newItems: List<EpisodeItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class EpisodeViewHolder(private val binding: ItemEpisodeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EpisodeItem) {
            binding.tvEpisodeNumber.text = "Episode ${item.episodeNumber}"
            binding.checkboxEpisode.setOnCheckedChangeListener(null)
            binding.checkboxEpisode.isChecked = item.isWatched
            binding.checkboxEpisode.setOnCheckedChangeListener { _, isChecked ->
                onEpisodeCheckedChange(item, isChecked)
            }
        }
    }
}
