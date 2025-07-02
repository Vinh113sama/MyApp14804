package com.example.myapp.repository

import android.app.AlertDialog
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.DialogAddToPlaylistBinding
import com.example.myapp.process.getplaylist.Playlist
import com.example.myapp.process.getplaylist.SelectPlaylistAdapter
import com.example.myapp.process.getsong.Song
import kotlinx.coroutines.launch

object PlaylistHelper {

    fun showAddToPlaylistDialog(
        fragment: Fragment,
        song: Song,
        viewModel: SongViewModel,
        onSongAdded: ((Playlist) -> Unit)? = null
    ) {
        val binding = DialogAddToPlaylistBinding.inflate(fragment.layoutInflater)
        val dialog = AlertDialog.Builder(fragment.requireContext())
            .setView(binding.root)
            .create()

        binding.tvSongName.text = song.title

        val adapter = SelectPlaylistAdapter { playlist ->
            viewModel.addSongToPlaylist(
                playlistId = playlist.id,
                songId = song.id
            ) { isSuccess, message ->
                Toast.makeText(fragment.requireContext(), message, Toast.LENGTH_SHORT).show()
                if (isSuccess) {
                    onSongAdded?.invoke(playlist)
                    dialog.dismiss()
                }
            }
        }
        binding.rcPlaylists.apply {
            layoutManager = LinearLayoutManager(fragment.requireContext())
            this.adapter = adapter
        }

        fragment.viewLifecycleOwner.lifecycleScope.launch {
            try {
                val playlists = viewModel.getPlaylist()

                if (playlists.isEmpty()) {
                    binding.layoutEmpty.visibility = View.VISIBLE
                    binding.rcPlaylists.visibility = View.GONE
                } else {
                    binding.layoutEmpty.visibility = View.GONE
                    binding.rcPlaylists.visibility = View.VISIBLE
                    adapter.submitList(playlists)
                }

            } catch (_: Exception) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.rcPlaylists.visibility = View.GONE
            }
        }

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
