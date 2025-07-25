package com.example.myapp.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.activity.PlaySongActivity
import com.example.myapp.databinding.FragmentRankingKpopBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.SongAdapter
import com.example.myapp.process.login.SongType
import com.example.myapp.repository.PlaylistHelper
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.getValue


@Suppress("UNCHECKED_CAST")
class RankingKpopFragment : Fragment() {
    private var _binding: FragmentRankingKpopBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter

    private val viewModel: SongViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                val repository = SongRepository(RetrofitClient.apiService)
                return SongViewModel(repository) as T
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRankingKpopBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeSongs()
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter()
        binding.rcRatingSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcRatingSongs.adapter = adapter

        binding.rcRatingSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!viewModel.isLoading.value && !viewModel.isLastPage.value &&
                    totalItemCount <= lastVisibleItem + 2
                ) {
                    viewModel.loadSongs(SongType.KPOP)
                }
            }
        })
    }

    private fun setupListeners() {
        binding.imgbtnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnPlayAll.setOnClickListener {
            val songs = adapter.currentList
            if (songs.isNotEmpty()) {
                val intent = Intent(requireContext(), PlaySongActivity::class.java).apply {
                    putParcelableArrayListExtra("playlist", ArrayList(songs))
                    putExtra("song", songs[0])
                    putExtra("position", 0)
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Empty list", Toast.LENGTH_SHORT).show()
            }
        }
        adapter.setOnItemClickListener { song, position ->
            val intent = Intent(requireContext(), PlaySongActivity::class.java)
            intent.putParcelableArrayListExtra("playlist", ArrayList(adapter.currentList))
            intent.putExtra("position", position)
            startActivity(intent)
        }

        adapter.setOnMoreClickListener { song ->
            PlaylistHelper.showAddToPlaylistDialog(
                fragment = this,
                song = song,
                viewModel = viewModel
            )
        }
    }

    private fun observeSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.songs.collectLatest { songList ->
                adapter.submitList(songList.toList())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh(SongType.KPOP)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}