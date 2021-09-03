package com.example.chatapp.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentCameraQrCodeScanBinding
import com.example.chatapp.models.Message
import com.example.chatapp.utils.Extensions.toSHA256
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class CameraQrCodeScan : Fragment(), ZXingScannerView.ResultHandler {
    private lateinit var binding: FragmentCameraQrCodeScanBinding
    private lateinit var name: String
    private val profileViewModel: ProfileViewModel by activityViewModels()

    companion object {
        private const val CAMERA_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = arguments?.getString("name") ?: R.string.error_name.toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraQrCodeScanBinding.inflate(inflater, container, false)
        checkPermission()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        startCamera()
        binding.qrcodeCamera.setResultHandler(this@CameraQrCodeScan)
    }

    private fun startCamera() {
        binding.qrcodeCamera.startCamera()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION)
        }
    }

    override fun handleResult(p0: Result?) {
        val ip = p0!!.text!!.split(':').first()
        val port = p0.text!!.split(':').last()
        connect(ip, port)
    }

    private fun connect(ip: String, port: String) {
        Utils.createSocket(ip, port.toInt()) {
            val connectionFactory: ConnectionFactory by activityViewModels()
            if (it != null) {
                connectionFactory.setSocket(it)
                connectionFactory.serverOnline.postValue(true)
                profileViewModel.deleteAll {
                    var image = ""
                    ProfileSharedProfile.getProfilePhoto { bitmap ->
                        if (bitmap != null) {
                            image = ProfileSharedProfile.bitmapToByteArrayToString(bitmap)
                        }
                        val message = Message(
                            type = Message.MessageType.JOIN.code,
                            username = ProfileSharedProfile.getProfile(),
                            text = null,
                            base64Data = null,
                            join = Message.Join(
                                avatar = image,
                                password = "".toSHA256(),
                                false
                            ),
                            id = null
                        )
                        val action =
                            CameraQrCodeScanDirections.actionCameraQrCodeScanToChatFragment(message, false)
                        findNavController().navigate(action)
                    }
                }
            } else {
                val snackBar =
                    Snackbar.make(requireView(), "Server doest exists", Snackbar.LENGTH_LONG)
                snackBar.show()
            }
        }
    }
}