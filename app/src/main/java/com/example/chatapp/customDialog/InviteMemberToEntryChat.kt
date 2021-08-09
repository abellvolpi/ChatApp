package com.example.chatapp.customDialog

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.InviteMemberToEntryChatBinding
import com.example.chatapp.ui.CreateServerDirections
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.viewModel.ConnectionFactory

class InviteMemberToEntryChat : DialogFragment() {
    private lateinit var qrCode : Bitmap
    private lateinit var binding : InviteMemberToEntryChatBinding
    private var port : Int = 0
    private lateinit var ip: String
//    private val connectionFactory: ConnectionFactory by activityViewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        binding = InviteMemberToEntryChatBinding.inflate(inflater, container, false)
        port = arguments?.getInt("port") ?: 0
        ip = arguments?.getString("ip")?: ""
        initView()
        return binding.root

    }

    override fun onStart() {
        requireDialog().window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        super.onStart()

    }
    private fun generateQRCode(){
        this.qrCode = Utils.generateQRCode("${ip}:${port}")
    }

    @SuppressLint("SetTextI18n")
    private fun initView(){
        generateQRCode()
        with(binding){
            qrcodeImage.setImageBitmap(qrCode)
            ipAddress.text = "${ip}:${port}"
        }
    }
}