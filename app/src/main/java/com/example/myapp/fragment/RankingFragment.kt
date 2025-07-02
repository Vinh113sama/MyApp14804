package com.example.myapp.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.myapp.R
import com.example.myapp.databinding.FragmentRankingBinding

class RankingFragment : Fragment() {
    private var _binding: FragmentRankingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRankingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgbtnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.imgbtnVn.setOnClickListener {
            findNavController().navigate(R.id.action_ratingFragment_to_ratingvnFragment)
        }
        binding.imgbtnUsuk.setOnClickListener {
            findNavController().navigate(R.id.action_ratingFragment_to_ratingusukFragment)
        }
        binding.imgbtnKpop.setOnClickListener {
            findNavController().navigate(R.id.action_ratingFragment_to_ratingkpopFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
