package com.example.chatapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.models.Message
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils.createSocket
import com.example.chatapp.utils.Utils.hideSoftKeyboard
import com.example.chatapp.viewModel.ConnectionFactory
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    val connectionFactory: ConnectionFactory by activityViewModels()
    private val navController by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.constraintLayoutHome.setOnClickListener {
            activity?.hideSoftKeyboard()
        }
        val message = arguments?.getString("messageIfError")
        if (message != null) {
            Snackbar.make(requireContext(), requireView(), message, Snackbar.LENGTH_LONG).show()
//            connectionFactory.closeSocket()
        }
    }

    private fun initViews() {
        with(binding) {
            connect.setOnClickListener {
                if (!isEditTextIsEmpty()) {
                    progressBar.alpha = 1f
                    connect()
                }
            }
            createServer.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToCreateServer()
                navController.navigate(action)
            }
            openCameraButton.setOnClickListener {
                if (nameField.text.toString().isNotBlank()) {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToCameraQrCodeScan(nameField.text.toString())
                    navController.navigate(action)
                } else {
                    nameField.error = "Please, insert your name"
                }
            }
        }
    }

    private fun isEditTextIsEmpty(): Boolean {
        with(binding) {
            if (ipField.text.isBlank()) {
                ipField.error = "Please, insert a ip"
                return true
            }
            if (nameField.text.isBlank()) {
                nameField.error = "Please, insert your name"
                return true
            }
            if (portField.text.isBlank()) {
                portField.error = "Please, insert port of server connection"
                return true
            }
            return false
        }
    }

    private fun connect() {
        with(binding) {
            createSocket(ipField.text.toString(), portField.text.toString().toInt()) {
                ProfileSharedProfile.saveProfile(nameField.text.toString())
                connectionFactory.setSocket(it)
                val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(ipField.text.toString(), portField.text.toString().toInt())
                val message = Message(
                    "",
                    ProfileSharedProfile.getProfile() + " was connected",
                    Message.NOTIFY_CHAT
                )
                val intent = Intent()
                intent.action = "com.example.message"
                intent.putExtra("message", message)
                findNavController().navigate(action)
            }
        }
    }
}