package com.example.chatapp.ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.chatapp.databinding.FragmentCameraQrCodeScanBinding
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.utils.ProfileSharedProfile
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.net.Socket

class CameraQrCodeScan : Fragment(), ZXingScannerView.ResultHandler {
    private lateinit var binding : FragmentCameraQrCodeScanBinding
    private lateinit var name: String
    companion object{
        private const val CAMERA_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = arguments?.getString("name")?: "ERROR NAME"

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraQrCodeScanBinding.inflate(inflater, container, false)
        checkPermission(android.Manifest.permission.CAMERA, CAMERA_PERMISSION)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        startCamera()
        binding.qrcodeCamera.setResultHandler(this@CameraQrCodeScan)
    }

    private fun startCamera(){
        binding.qrcodeCamera.startCamera()
    }

    private fun checkPermission(permission: String, requestCode: Int){
        if(ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), requestCode)
        }
    }

    override fun handleResult(p0: Result?) {
        val ip = p0!!.text!!.split(':').first()
        val port = p0.text!!.split(':').last()
        connect(ip, port)
    }

    private fun connect(ip: String, port: String){
        ProfileSharedProfile.saveProfile(name){
            val connectionFactory = ConnectionFactory()
            connectionFactory.setSocket(Socket(ip, port.toInt()))
            val action = CameraQrCodeScanDirections.actionCameraQrCodeScanToChatFragment(connectionFactory)
            findNavController().navigate(action)
        }
    }

}