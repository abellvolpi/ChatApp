package com.example.chatapp.customDialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.BottomshetShareLinkBinding

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ShareLinkBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var binding: BottomshetShareLinkBinding
    private var port: Int = 0
    private lateinit var ip: String
    private val navController by lazy {
        findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomshetShareLinkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            cardViewQrcode.setOnClickListener {
                port = arguments?.getInt("port") ?: 0
                ip = arguments?.getString("ip") ?: ""
                val action =
                    ShareLinkBottomSheetDialogFragmentDirections.actionShareLinkBottomSheetDialogFragmentToInviteMemberToEntryChat(ip, port)
                navController.navigate(action)
            }
            cardViewShareLink.setOnClickListener {
                port = arguments?.getInt("port") ?: 0
                ip = arguments?.getString("ip") ?: ""
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "http://www.mychatapp.com/home/$ip:$port"
                    )
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, ""))
                dismiss()
            }
        }
    }
}