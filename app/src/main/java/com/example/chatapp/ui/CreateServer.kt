package com.example.chatapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentCreateServerBinding
import com.example.chatapp.utils.Extensions.hideSoftKeyboard
import com.example.chatapp.utils.ServerBackgroundService
import com.example.chatapp.viewModel.ConnectionFactory


class   CreateServer : Fragment() {
    private lateinit var binding: FragmentCreateServerBinding
    private val connectionFactory: ConnectionFactory by activityViewModels()

    private val navController by lazy {
        findNavController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCreateServerBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.constraintLayoutCreateServer.setOnClickListener {
            activity?.hideSoftKeyboard()
        }
    }

    private fun initView() {
        with(binding) {
            btnCreateServer.setOnClickListener {
                createServer()
            }
        }
    }

    private fun createServer() {
        with(binding) {
            val action = CreateServerDirections.actionCreateServerToHomeFragment("")
            val intent = Intent(requireContext(), ServerBackgroundService::class.java)
            radioGroupPort.forEach {
                with(it as RadioButton){
                    if(isChecked){
                        intent.putExtra("socketConfigs", text.toString().toInt())
                        intent.putExtra("password", password.text.toString())
                        intent.action = ServerBackgroundService.START_SERVER
                        requireContext().startService(intent)
                        findNavController().navigate(action)
                        return@forEach
                    }
                }
            }
        }
    }
}