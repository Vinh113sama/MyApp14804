package com.example.myapp.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.activity.HomeActivity
import com.example.myapp.activity.PlaySongActivity
import com.example.myapp.databinding.FragmentSongListBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.SongAdapter
import kotlinx.coroutines.launch

class SongListFragment : Fragment() {

    private var _binding: FragmentSongListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter

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
        setupRecyclerView()
        fetchSongs()
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

    private fun fetchSongs() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getSongs()
                adapter.submitList(response.data)
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


