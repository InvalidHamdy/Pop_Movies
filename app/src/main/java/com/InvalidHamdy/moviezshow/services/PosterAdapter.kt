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
    private val items: List<MediaItem>,
    private val onPosterClick: ((MediaItem) -> Unit)? = null
) : RecyclerView.Adapter<PosterAdapter.PosterVH>() {

    inner class PosterVH(view: View) : RecyclerView.ViewHolder(view) {
        val posterIv: ImageView = view.findViewById(R.id.poster_iv)
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
            holder.posterIv.setImageResource(R.drawable.placeholder) //placeholder
        }

        holder.posterIv.setOnClickListener {
            onPosterClick?.invoke(item)
        }
    }
}
