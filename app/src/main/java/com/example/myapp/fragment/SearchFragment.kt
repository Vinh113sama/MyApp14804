package com.example.myapp.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import com.example.myapp.R
import com.example.myapp.activity.PlaySongActivity
import com.example.myapp.databinding.FragmentSearchBinding
import com.example.myapp.process.RetrofitClient
import com.example.myapp.process.getsong.SongAdapter
import com.example.myapp.repository.SongRepository
import com.example.myapp.repository.SongViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongAdapter
    private val handler = Handler(Looper.getMainLooper())

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
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchListener()
        observeSearchResults()

        binding.edtSearch.requestFocus()
        binding.imgbtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        adapter = SongAdapter()
        binding.rcSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rcSearchResults.adapter = adapter

        adapter.setOnItemClickListener { song, position ->
            val intent = Intent(requireContext(), PlaySongActivity::class.java)
            intent.putParcelableArrayListExtra("playlist", ArrayList(adapter.currentList))
            intent.putExtra("position", position)
            startActivity(intent)
        }
    }

    private fun setupSearchListener() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    val query = s?.toString().orEmpty().trim()
                    if (query.isEmpty()) {
                        adapter.submitList(emptyList())
                        binding.tvResults.text = ""
                    } else {
                        viewModel.searchSong(query)
                    }
                }, 300)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeSearchResults() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collectLatest { songList ->
                adapter.submitList(songList)
                binding.tvResults.text = if (songList.isEmpty()) {
                    getString(R.string.search_no_result)
                } else {
                    getString(R.string.search_result)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
