package com.example.chatapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.findFragment
import androidx.navigation.fragment.findNavController
import com.example.chatapp.customDialog.InviteMemberToEntryChat
import com.example.chatapp.databinding.FragmentCreateServerBinding
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.objects.ServerFactory
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.hideSoftKeyboard

class CreateServer : Fragment() {
 private lateinit var binding : FragmentCreateServerBinding

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

    private fun initView(){
        with(binding){
            btnCreateServer.setOnClickListener {
               if(!verifyIfEditTextisEmpy()){
                   createServer()
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

    private fun createServer(){
        with(binding){
            val serverFactory = ServerFactory(requireContext(), portField.text.toString().toInt())
            Utils.getIpAndress {
                val df= InviteMemberToEntryChat(it, portField.text.toString().toInt())
                df.show(childFragmentManager, "")
            }
            serverFactory.serverConnecting{
                ProfileSharedProfile.saveProfile(nameField.text.toString()){
                    val connectionFactory = ConnectionFactory(serverFactory.getSocket())
                    val action = CreateServerDirections.actionCreateServerToChatFragment(connectionFactory)
                    navController.navigate(action)
                }
            }
        }
    }
}