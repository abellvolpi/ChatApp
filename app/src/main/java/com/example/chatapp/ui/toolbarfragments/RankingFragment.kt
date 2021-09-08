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
import com.example.chatapp.models.Message
import com.example.chatapp.models.Profile
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.ProfileViewModel
import kotlinx.coroutines.runBlocking


class RankingFragment() : Fragment() {

    private lateinit var binding: FragmentRankingBinding
    private val profileController : ProfileViewModel by activityViewModels()
    private val connectionFactory: ConnectionFactory by activityViewModels()
    private lateinit var rankingAdapter: RankingAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRankingBinding.inflate(inflater, container, false)


        profileController.ranking.value.let {
            if(it != null){
                rankingAdapter = RankingAdapter(it)
                with(binding.recyclerView){
                    setAdapter(rankingAdapter)
                    layoutManager = LinearLayoutManager(requireContext())
                    addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
                }
            }else{
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
                                if(it != null) {
                                    rankingAdapter.addProfile(it)
                                }else{
                                    Log.e("RankingFragment", "error when getProfile from data base")
                                }
                            }

                        }
                    }
                    Message.MessageType.LEAVE.code -> {
                        if(it.first.id != null) {
                            rankingAdapter.removeProfile(it.first.id!!)
                        }
                    }
                }
            }
        }
    }


}