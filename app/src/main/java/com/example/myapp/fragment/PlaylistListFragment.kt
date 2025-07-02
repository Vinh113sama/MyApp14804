package com.example.myapp.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.R
import com.example.myapp.databinding.DialogAddPlaylistBinding
import com.example.myapp.databinding.DialogDeleteConfirmBinding
import com.example.myapp.databinding.DialogRenameBinding
import com.example.myapp.databinding.FragmentPlaylistListBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getplaylist.Playlist
import com.example.myapp.process.getplaylist.PlaylistAdapter
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class PlaylistListFragment : Fragment() {
    private var _binding: FragmentPlaylistListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: PlaylistAdapter
    private val viewModel: SongViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val repository = SongRepository(RetrofitClient.apiService)
                return SongViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupUI()
        loadPlaylists()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                if (error == null) {
                    loadPlaylists()
                } else {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun setupUI() {
        binding.imgbtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter()
        binding.rcPlaylistList.adapter = adapter
        binding.rcPlaylistList.layoutManager = LinearLayoutManager(requireContext())


        adapter.setOnItemClickListener { playlist ->
            val action = PlaylistListFragmentDirections.playToMusicplay(playlist.id, playlist.name)
            findNavController().navigate(action)
        }

        adapter.setOnEditClickListener { playlist ->
            showRenameDialog(playlist)
        }

        adapter.setOnDeleteClickListener { playlist ->
            showDeleteDialog(playlist)
        }

        binding.imgbtnAdd.setOnClickListener {
            showAddPlaylistDialog()
        }
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            try {
                val playlists = viewModel.getPlaylist()
                adapter.submitList(playlists)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showRenameDialog(playlist: Playlist) {
        val dialogBinding = DialogRenameBinding.inflate(layoutInflater)
        val editText = dialogBinding.edtPlaylistName
        editText.setText(playlist.name)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val newName = editText.text.toString().trim()
            if (newName.isNotEmpty()) {
                viewModel.updatePlaylistName(
                    playlist.id,
                    newName,
                    onSuccess = { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        loadPlaylists()
                        dialog.dismiss()
                    },
                    onError = { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showAddPlaylistDialog() {
        val dialogBinding = DialogAddPlaylistBinding.inflate(layoutInflater)
        val editText = dialogBinding.edtPlaylistName

        dialogBinding.tv.text = getString(R.string.create_playlist_title)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnCreate.setOnClickListener {
            val name = editText.text.toString().trim()
            if (name.isNotEmpty()) {
                viewModel.createPlaylist(
                    name = name,
                    onSuccess = { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        loadPlaylists()
                        dialog.dismiss()
                    },
                    onError = { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    }
                )
            } else {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun showDeleteDialog(playlist: Playlist) {
        val dialogBinding = DialogDeleteConfirmBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvMessage.text = getString(R.string.delete_playlist_message, playlist.name)

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnDelete.setOnClickListener {
            viewModel.deletePlaylist(
                playlist.id,
                onSuccess = { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    loadPlaylists()
                    dialog.dismiss()
                },
                onError = { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            )
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
