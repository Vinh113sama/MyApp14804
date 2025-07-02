package com.example.myapp.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.activity.PlaySongActivity
import com.example.myapp.databinding.FragmentPlaylistBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.SongAdapter
import com.example.myapp.process.login.SongType
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
        Log.d("PlaylistFragment", "playlistId = $playlistId")
        binding.tvNamePlaylist.text = playlistName
        binding.tvNamePlaylist.text = playlistName
        setupRecyclerView()
        observePlaylistSongs()
        viewModel.loadSongs(SongType.PLAYLISTSONG, playlistId = playlistId)

        adapter.setOnFavoriteClickListener { song ->
            viewModel.deletePlaylistSong(
                playlistId = playlistId,
                songId = song.id,
                onSuccess = { message ->
                    viewModel.refresh(SongType.PLAYLISTSONG, playlistId = playlistId)
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.rcPlaylistSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()


                if (!viewModel.isLoading.value && !viewModel.isLastPage.value &&
                    totalItemCount <= lastVisibleItem + 2
                ) {
                    viewModel.loadSongs(SongType.PLAYLISTSONG, playlistId = playlistId)
                }
            }
        })

        binding.imgbtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter(false, true)
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
