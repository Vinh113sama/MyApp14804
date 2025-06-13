package com.example.myapp.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapp.activity.PlaySongActivity
import com.example.myapp.databinding.FragmentHistoryBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.SongAdapter
import kotlinx.coroutines.launch


class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongAdapter()
        binding.rcHistorySongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rcHistorySongs.adapter = adapter

        adapter.setOnItemClickListener { song, position ->
            val intent = Intent(requireContext(), PlaySongActivity::class.java)
            intent.putParcelableArrayListExtra("playlist", ArrayList(adapter.currentList))
            intent.putExtra("position", position)
            startActivity(intent)
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
                Toast.makeText(requireContext(), "Danh sách trống", Toast.LENGTH_SHORT).show()
            }
        }
        binding.imgbtnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        fetchSongs()
    }

    private fun fetchSongs() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getHistorySongs()
                val playedSongs = response.data
                val songs = playedSongs.map { it.song }
                adapter.submitList(songs)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchSongs()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}