package com.example.myapp.process.getsong

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapp.databinding.ItemSongBinding
import java.util.Locale

class SongAdapter : ListAdapter<Song, SongAdapter.SongViewHolder>(DiffCallback) {
    private var onItemClick: ((Song, Int) -> Unit)? = null

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
        holder.binding.tvDuration.text = fomatDuration(song.duration)

        Glide.with(holder.itemView.context)
            .load(song.imageUrl)
            .into(holder.binding.imgSong)

        holder.binding.root.setOnClickListener {
            onItemClick?.invoke(song, position)
        }
    }

    private fun fomatDuration(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format(Locale.getDefault() ,"%02d:%02d", minutes, remainingSeconds)

    }

    fun setOnItemClickListener(listener: (Song, Int) -> Unit) {
        onItemClick = listener
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