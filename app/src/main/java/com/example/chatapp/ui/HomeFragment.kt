package com.example.chatapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.objects.ServerFactory
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

    private fun initViews() {
        with(binding) {

            constraintLayoutHome.setOnClickListener {
                activity?.hideSoftKeyboard()
            }

            connect.setOnClickListener {
                if (!isEditTextIsEmpty()) {
                    progressBar.alpha = 1f
                    val connectionFactory = ConnectionFactory(ipField.text.toString(), portField.text.toString().toInt())
                    val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(connectionFactory)
                    findNavController().navigate(action)
                }
            }

            createServer.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToCreateServer()
                findNavController().navigate(action)
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
}