package com.InvalidHamdy.moviezshow.services

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.InvalidHamdy.moviezshow.R
import com.bumptech.glide.Glide
import com.InvalidHamdy.moviezshow.databinding.CastListItemBinding
import com.InvalidHamdy.moviezshow.data.response.CastMember

class CastAdapter(private val items: List<CastMember>) :
    RecyclerView.Adapter<CastAdapter.CastVH>() {

    inner class CastVH(val binding: CastListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastVH {
        val binding = CastListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastVH(binding)
    }

    override fun onBindViewHolder(holder: CastVH, position: Int) {
        val cast = items[position]
        holder.binding.castNameTv.text = cast.name ?: ""
        holder.binding.roleTv.text = cast.character ?: ""

        val profile = cast.profile_path
        if (!profile.isNullOrEmpty()) {
            val url = if (profile.startsWith("http")) profile else "https://image.tmdb.org/t/p/w185$profile"
            Glide.with(holder.binding.posterIv.context)
                .load(url)
                .centerCrop()
                .into(holder.binding.posterIv)
        } else {
            holder.binding.posterIv.setImageResource(R.drawable.placeholder) // ضع صورة placeholder مناسبة
        }
    }

    override fun getItemCount(): Int = items.size
}
