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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.activity.HomeActivity
import com.example.myapp.activity.PlaySongActivity
import com.example.myapp.databinding.FragmentSongListBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.SongAdapter
import com.example.myapp.process.login.SongType
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest


@Suppress("UNCHECKED_CAST")
class SongListFragment : Fragment() {

    private var _binding: FragmentSongListBinding? = null
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imgbtnMenu.setOnClickListener {
            (activity as? HomeActivity)?.openDrawer()
        }

        binding.rcPopular.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()


                if (!viewModel.isLoading.value && !viewModel.isLastPage.value &&
                    totalItemCount <= lastVisibleItem + 2
                ) {
                    viewModel.loadSongs(SongType.ALL)
                }
            }
        })

        setupRecyclerView()
        observeSongs()
        viewModel.loadSongs(SongType.ALL)
    }

    @OptIn(UnstableApi::class)
    private fun observeSongs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.songs.collectLatest { songList ->
                adapter.submitList(songList)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun setupRecyclerView() {
        adapter = SongAdapter()
        binding.rcPopular.layoutManager = LinearLayoutManager(requireContext())
        binding.rcPopular.adapter = adapter

        adapter.setOnItemClickListener { song, position ->
            val intent = Intent(requireContext(), PlaySongActivity::class.java).apply {
                putParcelableArrayListExtra("playlist", ArrayList(adapter.currentList))
                putExtra("song", song)
                putExtra("position", position)
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


