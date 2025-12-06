package com.InvalidHamdy.moviezshow.services

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.InvalidHamdy.moviezshow.R
import com.InvalidHamdy.moviezshow.databinding.GenreCardBinding
import com.InvalidHamdy.moviezshow.data.response.Genre

class GenreAdapter(
    private val genres: List<Genre>,
    private val onGenreClick: (Genre, Int) -> Unit
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class GenreViewHolder(val binding: GenreCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = GenreCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        // Use `position` immediately for binding (safe to use right away)
        val genre = genres[position]
        holder.binding.genreBttn.text = genre.name

        // Styling for selected vs unselected (use position for immediate styling)
        if (position == selectedPosition) {
            holder.binding.genreBttn.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.red_bttn)
            )
            holder.binding.genreBttn.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        } else {
            holder.binding.genreBttn.setBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, android.R.color.transparent)
            )
            holder.binding.genreBttn.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
        }


        holder.binding.genreBttn.setOnClickListener {
            val currentPos = holder.bindingAdapterPosition
            if (currentPos == RecyclerView.NO_POSITION) return@setOnClickListener

            if (selectedPosition != currentPos) {
                val old = selectedPosition
                selectedPosition = currentPos
                if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
                notifyItemChanged(selectedPosition)
            }

            onGenreClick(genres[currentPos], currentPos)
        }
    }

    override fun getItemCount(): Int = genres.size

    fun selectPosition(pos: Int) {
        if (pos < 0 || pos >= genres.size) return
        val old = selectedPosition
        selectedPosition = pos
        if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
        notifyItemChanged(selectedPosition)
    }

    fun getSelectedGenre(): Genre? =
        if (selectedPosition in genres.indices) genres[selectedPosition] else null
}