package com.example.chatapp.ui

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.chatapp.databinding.FragmentCreateServerBinding
import com.example.chatapp.objects.ServerFactory

class CreateServer : Fragment() {
 private lateinit var binding : FragmentCreateServerBinding

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

    private fun initView(){
        with(binding){
            btnCreateServer.setOnClickListener {
               if(!verifyIfEditTextisEmpy()){
                   val serverFactory = ServerFactory(requireContext(), portField.text.toString().toInt())
                   serverFactory.serverConnecting()
               }
            }
        }
    }

    private fun verifyIfEditTextisEmpy(): Boolean{
        with(binding){
            if(portField.text.isBlank()){
                portField.error = "Please, insert a port number"
                return true
            }
            if(nameField.text.isBlank()){
                nameField.error = "Please, insert your name"
                return true
            }
            return false

        }
    }


}