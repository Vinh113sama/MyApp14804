package com.example.myapp.process.getplaylist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.databinding.ItemPlaylistBinding

class PlaylistAdapter : ListAdapter<PlaylistResponse, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback) {


    private var onItemClick: ((PlaylistResponse) -> Unit)? = null
    private var onEditClick: ((PlaylistResponse) -> Unit)? = null
    private var onDeleteClick: ((PlaylistResponse) -> Unit)? = null


    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlaylistResponse) {
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

    fun setOnItemClickListener(listener: (PlaylistResponse) -> Unit) {
        onItemClick = listener
    }

    fun setOnEditClickListener(listener: (PlaylistResponse) -> Unit) {
        onEditClick = listener
    }

    fun setOnDeleteClickListener(listener: (PlaylistResponse) -> Unit) {
        onDeleteClick = listener
    }

    companion object {
        private val PlaylistDiffCallback = object : DiffUtil.ItemCallback<PlaylistResponse>() {
            override fun areItemsTheSame(oldItem: PlaylistResponse, newItem: PlaylistResponse): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: PlaylistResponse, newItem: PlaylistResponse): Boolean {
                return oldItem == newItem
            }
        }
    }
}
