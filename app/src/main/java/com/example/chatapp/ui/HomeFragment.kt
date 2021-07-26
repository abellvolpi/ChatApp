package com.example.chatapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.models.Message
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils.createSocket
import com.example.chatapp.utils.Utils.hideKeyboard
import com.example.chatapp.utils.Utils.hideSoftKeyboard
import kotlinx.coroutines.Dispatchers

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
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
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        initViews()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.constraintLayoutHome.setOnClickListener {
            activity?.hideSoftKeyboard()
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

    private fun createMessageICameIn(): Message {
        return Message("", "My name Entrou", Message.NOTIFY_CHAT)
    }

    private fun connect() {
        with(binding) {
            createSocket(ipField.text.toString(), portField.text.toString().toInt()) {
                ProfileSharedProfile.saveProfile(nameField.text.toString())
                val connectionFactory = ConnectionFactory(it)
                val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(connectionFactory)
                connectionFactory.sendMessage(createMessageICameIn()) {}
                findNavController().navigate(action)
            }
        }
    }
}