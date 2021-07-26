package com.example.chatapp.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.models.Message
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.hideSoftKeyboard


class ChatFragment() : Fragment(), View.OnClickListener {
    private lateinit var binding : FragmentChatBinding
    private lateinit var connectionFactory: ConnectionFactory
    private lateinit var adapter: ChatAdapter
    private var data = arrayListOf<Message>()
    private lateinit var profileName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectionFactory = arguments?.get("connection") as ConnectionFactory
        ProfileSharedProfile.getProfile {
            profileName = it
        }
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
            connectionFactory.readMessage {
                val messageClass = Utils.JSONtoMessageClass(it)
                messageClass.typeMesage = Message.RECEIVED_MESSAGE
                refreshChat(messageClass)
                Log.e("ouvindo: ", it)
            }

            buttonSend.setOnClickListener {
                if (messageField.text.isNotBlank()) {
                    val message =
                        Message(profileName, messageField.text.toString(), Message.SENT_MESSAGE)
                    connectionFactory.sendMessage(message) {
                        messageField.text.clear()
                        refreshChat(message)
                    }
                } else {
                    Toast.makeText(requireContext(), "Menssage cannot be blank", Toast.LENGTH_LONG)
                        .show()
                }
            }
            adapter = ChatAdapter(data)
            messagesRecyclerview.adapter = adapter
            messagesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun refreshChat(message: Message){
        binding.messagesRecyclerview.canScrollVertically(1)
        adapter.addData(message)
        with(binding.messagesRecyclerview){
            if(!canScrollVertically(1)){
                scrollToPosition(data.size-1)
            }
        }
    }

    override fun onClick(v: View?) {
            if (v != EditText(requireContext())){
                activity?.hideSoftKeyboard()
            }

    }
}