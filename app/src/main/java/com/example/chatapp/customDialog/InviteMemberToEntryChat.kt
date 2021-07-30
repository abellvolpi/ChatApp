package com.example.chatapp.customDialog

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.chatapp.databinding.InviteMemberToEntryChatBinding
import com.example.chatapp.utils.Utils

class InviteMemberToEntryChat(val ip: String, val port: Int) : DialogFragment() {
    private lateinit var qrCode : Bitmap
    private lateinit var binding : InviteMemberToEntryChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        binding = InviteMemberToEntryChatBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onStart() {
        requireDialog().window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        super.onStart()

    }
    private fun generateQRcode(){
        this.qrCode = Utils.generateQRCode("${ip}:${port}")
    }

    @SuppressLint("SetTextI18n")
    private fun initView(){
        generateQRcode()
        with(binding){
            qrcodeImage.setImageBitmap(qrCode)
            ipAndress.text = "${ip}:${port}"
        }
    }

}