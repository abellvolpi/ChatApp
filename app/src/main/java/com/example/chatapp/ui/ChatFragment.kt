package com.example.chatapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.ui.model.Message


class ChatFragment : Fragment() {
    private lateinit var binding : FragmentChatBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView(){
        with(binding){
            buttonSend.setOnClickListener {
                if(messageField.text.isNotBlank()){
                    val message = Message("Testando", messageField.text.toString())
                    ConnectionFactory.sendMessage(message)
                    ConnectionFactory.readMessage {
                        it.forEach {
                            println(it.message)
                        }
                    }
                }else{
                    Toast.makeText(requireContext(), "Mensage cannot be blank", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}