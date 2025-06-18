package com.example.myapp.process.getsong

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapp.databinding.ItemSongBinding
import java.util.Locale
import com.example.myapp.R


class SongAdapter(private val showFavoriteButton: Boolean = false) :
    ListAdapter<Song, SongAdapter.SongViewHolder>(DiffCallback) {

    private var onItemClick: ((Song, Int) -> Unit)? = null
    private var onFavoriteClick: ((Song) -> Unit)? = null

    inner class SongViewHolder(val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = getItem(position)

        holder.binding.tvSongName.text = song.title
        holder.binding.tvArtistName.text = song.artist.name
        holder.binding.tvDuration.text = fomartDuration(song.duration)

        Glide.with(holder.itemView.context)
            .load(song.imageUrl)
            .into(holder.binding.imgSong)

        holder.binding.imgbtnFavorite.setImageResource(
            if (showFavoriteButton) R.drawable.ic_delete_favorite else R.drawable.ic_more_vert
        )

        holder.binding.root.setOnClickListener {
            onItemClick?.invoke(song, position)
        }
        holder.binding.imgbtnFavorite.setOnClickListener {
            onFavoriteClick?.invoke(song)
        }

    }

    private fun fomartDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)

    }

    fun setOnItemClickListener(listener: (Song, Int) -> Unit) {
        onItemClick = listener
    }

    fun setOnFavoriteClickListener(listener: (Song) -> Unit) {
        onFavoriteClick = listener
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem == newItem
            }
        }
    }
}