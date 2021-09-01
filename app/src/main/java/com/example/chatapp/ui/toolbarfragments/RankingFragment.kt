package com.example.chatapp.ui.toolbarfragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.RankingAdapter
import com.example.chatapp.databinding.FragmentRankingBinding
import com.example.chatapp.viewModel.ProfileViewModel


class RankingFragment : Fragment() {

    private lateinit var binding: FragmentRankingBinding
    private val profileController : ProfileViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRankingBinding.inflate(inflater, container, false)


        profileController.ranking.observe(viewLifecycleOwner) {
            if(it != null){
                val adapter = RankingAdapter(it)
                with(binding.recyclerView){
                    setAdapter(adapter)
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
                }
            }else{
                Log.e("participantsFragment", "Arraylist is empty")
            }
        }
        return binding.root
    }


}