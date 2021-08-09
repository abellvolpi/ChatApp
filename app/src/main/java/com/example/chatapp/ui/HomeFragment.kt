package com.example.chatapp.ui

import android.content.Intent
import android.app.Activity.RESULT_OK

import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.models.Message
import com.example.chatapp.ui.ProfileFragment.Companion.PICK_IMAGE
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils.createSocket
import com.example.chatapp.utils.Utils.hideSoftKeyboard
import com.example.chatapp.viewModel.ConnectionFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class HomeFragment : Fragment(), CoroutineScope {
    private lateinit var binding: FragmentHomeBinding
    override val coroutineContext: CoroutineContext = Job()+ Dispatchers.Main

    val connectionFactory: ConnectionFactory by activityViewModels()
    private val navController by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        ProfileSharedProfile.clearSharedPreferences()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        initViews()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.constraintLayoutHome.setOnClickListener {
            activity?.hideSoftKeyboard()
        }
        val message = arguments?.getString("messageIfError")
        if (message != null) {
            Snackbar.make(requireContext(), requireView(), R.string.server_disconnected.toString(), Snackbar.LENGTH_LONG).show()
//          connectionFactory.closeSocket()
        }
    }

    private fun initViews() {
        with(binding) {
            connect.setOnClickListener {
                if (!isEditTextIsEmpty()) {
                    progressBar.alpha = 1f
                    connect()
                }
            }
            createServer.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToCreateServer()
                navController.navigate(action)
            }
            openCameraButton.setOnClickListener {
                if (nameField.text.toString().isNotBlank()) {
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToCameraQrCodeScan(nameField.text.toString())
                    navController.navigate(action)
                } else {
                    nameField.error = "Please, insert your name"
                }
            }
            floatingEditButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                startActivityForResult(intent, PICK_IMAGE)
            }
        }
    }

    private fun isEditTextIsEmpty(): Boolean {
        with(binding) {
            if (ipField.text.isBlank()) {
                ipField.error = getString(R.string.ip_error)
                return true
            }
            if (nameField.text.isBlank()) {
                nameField.error = getString(R.string.name_error)
                return true
            }
            if (portField.text.isBlank()) {
                portField.error = getString(R.string.port_error)
                return true
            }
            return false
        }
    }

    private fun connect() {
        with(binding) {
            createSocket(ipField.text.toString(), portField.text.toString().toInt()) {
                ProfileSharedProfile.saveProfile(nameField.text.toString())
                connectionFactory.setSocket(it)
                val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(ipField.text.toString(), portField.text.toString().toInt())
                var image = ""
                val bitmap = ProfileSharedProfile.getProfilePhoto()
                if (bitmap != null) {
                    image = ProfileSharedProfile.BitmapToByteArrayToString(bitmap)
                }


                val message = Message(
                    image,
                    getString(R.string.player_connected,ProfileSharedProfile.getProfile()),
                    Message.NOTIFY_CHAT
                )
                connectionFactory.sendMessageToSocket(message){}
                findNavController().navigate(action)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            launch(Dispatchers.Default) {
                val imageUri = data?.data
                val imageBitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, imageUri)
                launch(Dispatchers.Main) {
                    binding.photo.setImageBitmap(imageBitmap)
                }
                imageBitmap?.let { ProfileSharedProfile.saveProfilePhoto(it) }
            }
        }
    }
}