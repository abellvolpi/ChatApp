package com.example.chatapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentCreateServerBinding
import com.example.chatapp.utils.Extensions.hideSoftKeyboard
import com.example.chatapp.utils.Extensions.toSHA256
import com.example.chatapp.utils.ServerBackgroundService

class CreateServer : Fragment() {
    private lateinit var binding: FragmentCreateServerBinding

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
                if (!ServerBackgroundService.serverRunning) {
                    createServer()
                    ServerBackgroundService.serverRunning = true
                } else {
                    Toast.makeText(requireContext(),getString(R.string.server_already_initialized), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun createServer() {
        with(binding) {
            val action = CreateServerDirections.actionCreateServerToHomeFragment("")
            val intent = Intent(requireContext(), ServerBackgroundService::class.java)
            radioGroupPort.forEach {
                with(it as RadioButton) {
                    if (isChecked) {
                        intent.putExtra("socketConfigs", text.toString().toInt())
                        intent.putExtra("password", password.text.toString().toSHA256())
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