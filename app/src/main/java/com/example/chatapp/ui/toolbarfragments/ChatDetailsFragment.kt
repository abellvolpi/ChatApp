package com.example.chatapp.ui.toolbarfragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.chatapp.R
import com.example.chatapp.adapters.ViewPagerAdapter
import com.example.chatapp.databinding.FragmentChatDetailsBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class ChatDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_chat_details, container, false)

        val fragmentList = arrayListOf(
            ParticipantsFragment(),
            RankingFragment()
        )
        val myAdapter = ViewPagerAdapter(fragmentList, requireActivity().supportFragmentManager, lifecycle)
        val viewPager = view.findViewById(R.id.viewpager) as ViewPager2
        viewPager.adapter = myAdapter

        val tabLayout = view.findViewById(R.id.tab_layout) as TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0->{
                    tab.text = getString(R.string.participants)
                }
                1 -> {
                    tab.text = getString(R.string.ranking)
                }

            }
        }.attach()
        return view
    }
}