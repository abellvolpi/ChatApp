package com.example.chatapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.objects.ServerFactory

class HomeFragment : Fragment() {
    private lateinit var binding : FragmentHomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        initViews()

        ConnectionFactory.clientConnecting()
        return binding.root
    }

    private fun initViews(){
        with(binding){
            connect.setOnClickListener {

            }

            createServer.setOnClickListener {

            }
        }
    }



}