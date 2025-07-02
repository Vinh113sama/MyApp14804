package com.example.myapp.process.getplaylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.databinding.ItemPlaylistBinding

class PlaylistAdapter : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback) {

    private var onItemClick: ((Playlist) -> Unit)? = null
    private var onEditClick: ((Playlist) -> Unit)? = null
    private var onDeleteClick: ((Playlist) -> Unit)? = null

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Playlist) {
            binding.tvPlaylistName.text = item.name

            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }

            binding.imgbtnEdit.setOnClickListener {
                onEditClick?.invoke(item)
            }

            binding.imgbtnDelete.setOnClickListener {
                onDeleteClick?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setOnItemClickListener(listener: (Playlist) -> Unit) {
        onItemClick = listener
    }

    fun setOnEditClickListener(listener: (Playlist) -> Unit) {
        onEditClick = listener
    }

    fun setOnDeleteClickListener(listener: (Playlist) -> Unit) {
        onDeleteClick = listener
    }

    companion object {
        private val PlaylistDiffCallback = object : DiffUtil.ItemCallback<Playlist>() {
            override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
                return oldItem == newItem
            }
        }
    }
}
