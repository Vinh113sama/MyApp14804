package com.example.myapp.process.getplaylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.databinding.ItemSelectPlaylistBinding

class SelectPlaylistAdapter(
    private val onClick: (PlaylistResponse) -> Unit
) : ListAdapter<PlaylistResponse, SelectPlaylistAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<PlaylistResponse>() {
        override fun areItemsTheSame(oldItem: PlaylistResponse, newItem: PlaylistResponse): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: PlaylistResponse, newItem: PlaylistResponse): Boolean {
            return oldItem == newItem
        }
    }

    inner class ViewHolder(private val binding: ItemSelectPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PlaylistResponse) {
            binding.tvPlaylistName.text = item.name
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}
