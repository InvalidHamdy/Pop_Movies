package com.InvalidHamdy.moviezshow.services

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.InvalidHamdy.moviezshow.R
import com.InvalidHamdy.moviezshow.data.response.MediaItem
import com.bumptech.glide.Glide

class PosterAdapter(
    initialItems: List<MediaItem>,
    private val onPosterClick: ((MediaItem) -> Unit)? = null,
    private val onFavClick: ((MediaItem) -> Unit)? = null,
    private val onSaveClick: ((MediaItem) -> Unit)? = null
) : RecyclerView.Adapter<PosterAdapter.PosterVH>() {

    private val items: MutableList<MediaItem> = initialItems.toMutableList()
    
    // Track local state for icons
    private var favIds: Set<Int> = emptySet()
    private var savedMap: Map<Int, String> = emptyMap()

    inner class PosterVH(view: View) : RecyclerView.ViewHolder(view) {
        val posterIv: ImageView = view.findViewById(R.id.poster_iv)
        val favIv: ImageView = view.findViewById(R.id.fav_iv)
        val saveIv: ImageView = view.findViewById(R.id.save_iv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.poster_card_list_item, parent, false)
        return PosterVH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PosterVH, position: Int) {
        val item = items[position]
        val posterPath = item.poster_path
        val fullUrl = if (!posterPath.isNullOrEmpty()) "https://image.tmdb.org/t/p/w500$posterPath" else null

        if (fullUrl != null) {
            Glide.with(holder.posterIv.context)
                .load(fullUrl)
                .centerCrop()
                .into(holder.posterIv)
        } else {
            holder.posterIv.setImageResource(R.drawable.placeholder)
        }

        // Set icon states based on local data
        holder.favIv.setImageResource(
            if (favIds.contains(item.id)) R.drawable.filled_fav_ic else R.drawable.fav_ic
        )
        holder.saveIv.setImageResource(
            if (savedMap.containsKey(item.id)) R.drawable.filled_saved_ic else R.drawable.saved_ic
        )

        // CRITICAL: Use itemView for base click, NOT posterIv
        holder.itemView.setOnClickListener {
            onPosterClick?.invoke(item)
        }
        holder.favIv.setOnClickListener {
            onFavClick?.invoke(item)
        }
        holder.saveIv.setOnClickListener {
            onSaveClick?.invoke(item)
        }
    }

    fun addItems(newItems: List<MediaItem>) {
        val startPos = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPos, newItems.size)
    }
    
    fun updateItems(newItems: List<MediaItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    
    fun updateLocalState(newFavIds: Set<Int>, newSavedMap: Map<Int, String>) {
        favIds = newFavIds
        savedMap = newSavedMap
        notifyDataSetChanged()
    }
}
