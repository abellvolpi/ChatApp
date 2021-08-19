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
import com.example.chatapp.adapters.ParticipantsAdapter
import com.example.chatapp.databinding.FragmentParticipantsBinding
import com.example.chatapp.viewModel.ProfileViewModel


class ParticipantsFragment : Fragment() {

    private lateinit var binding : FragmentParticipantsBinding
    private val profileController : ProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentParticipantsBinding.inflate(inflater, container, false)

        var adapter : ParticipantsAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        profileController.profiles.observe(viewLifecycleOwner) {
            if(it != null){
                adapter = ParticipantsAdapter(it)
                with(binding.recyclerView){
                    setAdapter(adapter)
                    adapter.notifyDataSetChanged()
                    Log.d("participantsFragment", "updated list")
                }
            }else{
                Log.e("participantsFragment", "Arraylist is empty")
            }
        }
        return binding.root
    }
}