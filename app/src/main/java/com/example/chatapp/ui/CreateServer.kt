package com.example.chatapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentCreateServerBinding
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.ServerBackgroundService
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.createSocket
import com.example.chatapp.utils.Utils.hideSoftKeyboard

class CreateServer : Fragment() {
    private lateinit var binding: FragmentCreateServerBinding
    private val connectionFactory: ConnectionFactory by activityViewModels()

    private val navController by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                if (!verifyIfEditTextisEmpty()) {
                    createServer()
                }
            }
        }
    }

    private fun verifyIfEditTextisEmpty(): Boolean {
        with(binding) {
            if (portField.text.isBlank()) {
                portField.error = getString(R.string.port_error)
                return true
            }
            if (nameField.text.isBlank()) {
                nameField.error = getString(R.string.name_error)
                return true
            }
            return false
        }
    }

    private fun createServer() {
        with(binding) {
            val ipAndress = Utils.getipAddress()
            ProfileSharedProfile.saveProfile(nameField.text.toString())
                val action = CreateServerDirections.actionCreateServerToHomeFragment(null)
                val intent = Intent(requireContext(), ServerBackgroundService::class.java)
                intent.putExtra("socketConfigs", portField.text.toString().toInt())
                intent.action = "com.example.startserver"
                requireContext().startService(intent)
                createSocket(ipAndress, portField.text.toString().toInt()){
                    connectionFactory.setSocket(it)
                findNavController().navigate(action)
            }
        }
    }
}