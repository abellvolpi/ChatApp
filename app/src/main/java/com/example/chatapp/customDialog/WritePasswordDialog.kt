package com.example.chatapp.customDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.chatapp.databinding.DialogWritePasswordBinding
import com.example.chatapp.models.Message
import com.example.chatapp.utils.Extensions.toSHA256
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.viewModel.ConnectionFactory

class WritePasswordDialog: DialogFragment() {
    private val connectionFactory : ConnectionFactory by activityViewModels()
    private lateinit var  binding : DialogWritePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogWritePasswordBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    override fun onStart() {
        requireDialog().window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        super.onStart()
    }

    private fun initViews(){
        isCancelable = false
        with(binding){
            sendButton.setOnClickListener {
                val message = Message(
                type = Message.MessageType.JOIN.code,
                username = ProfileSharedProfile.getProfile(),
                text = null,
                base64Data = null,
                join = Message.Join(
                    avatar = ProfileSharedProfile.getProfilePhotoBase64(),
                    password = passwordField.text.toString().toSHA256()
                ),
                id = null
            )
                connectionFactory.sendMessageToSocket(message){}
                dismiss()
            }

        }
    }
}