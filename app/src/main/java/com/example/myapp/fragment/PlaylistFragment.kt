package com.example.myapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.activity.PlaySongActivity
import com.example.myapp.databinding.FragmentPlaylistBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.SongAdapter
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter
    private val args: PlaylistFragmentArgs by navArgs()
    private var playlistId: Int = 0
    private var playlistName: String = ""

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
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = args.playlistId
        playlistName = args.playlistName
        binding.tvNamePlaylist.text = playlistName
        binding.tvNamePlaylist.text = playlistName
        setupRecyclerView()
        observePlaylistSongs()
        viewModel.loadSongsInPlaylist(playlistId)

        binding.imgbtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter()
        binding.rcPlaylistSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPlaylistSongs.adapter = adapter

        adapter.setOnItemClickListener { song, position ->
            val intent = Intent(requireContext(), PlaySongActivity::class.java)
            intent.putParcelableArrayListExtra("playlist", ArrayList(adapter.currentList))
            intent.putExtra("position", position)
            startActivity(intent)
        }
    }

    private fun observePlaylistSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playlistSongs.collectLatest { songList ->
                adapter.submitList(songList)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
