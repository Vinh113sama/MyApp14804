package com.example.myapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.databinding.FragmentPlaylistListBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getplaylist.PlaylistAdapter
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import kotlinx.coroutines.launch

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
        }

        adapter.setOnDeleteClickListener { playlist ->
        }
    }

    private fun loadPlaylists() {
        lifecycleScope.launch {
            try {
                val userId = viewModel.getuserInformation().id
                val playlists = viewModel.getPlaylist(userId)
                adapter.submitList(playlists)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
