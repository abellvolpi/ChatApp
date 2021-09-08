package com.example.chatapp.ui.toolbarfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.ParticipantsAdapter
import com.example.chatapp.databinding.FragmentParticipantsBinding
import com.example.chatapp.models.Message
import com.example.chatapp.models.Profile
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.ProfileViewModel
import kotlinx.coroutines.runBlocking


class ParticipantsFragment : Fragment() {

    private lateinit var binding: FragmentParticipantsBinding
    private val profileController: ProfileViewModel by activityViewModels()
    private val connectionFactory: ConnectionFactory by activityViewModels()
    private lateinit var participantsAdapter: ParticipantsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentParticipantsBinding.inflate(inflater, container, false)

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        }

        profileController.profiles.value.let {
            if (it != null) {
                val distinctList = arrayListOf<Profile>()
                distinctList.addAll(it.distinct())
                participantsAdapter = ParticipantsAdapter(distinctList, connectionFactory)
                with(binding.recyclerView) {
                    adapter = participantsAdapter
                    Log.d("participantsFragment", "updated list")
                }
            } else {
                Log.e("participantsFragment", "Arraylist is empty")
            }
        }
        initObservers()
        return binding.root
    }

    private fun initObservers() {
        connectionFactory.line.observe(viewLifecycleOwner) {
            with(it.first) {
                when (type) {
                    Message.MessageType.JOIN.code -> {
                        if (id != null) {
                            val profile =
                                Profile(id, username, join?.avatar, 0, true, join?.isAdmin)
                            runBlocking {
                                profileController.insert(profile)
                            }
                            profileController.getProfile(profile.id){
                                if(it!=null) {
                                    participantsAdapter.addProfile(it)
                                }else{
                                    Log.e("ParticipantsFragment", "Error get profile from data base")
                                }
                            }

                        }
                    }
                    Message.MessageType.LEAVE.code -> {
                        if(it.first.id != null) {
                            participantsAdapter.removeProfile(it.first.id!!)
                        }
                    }
                }
            }
        }
    }
}