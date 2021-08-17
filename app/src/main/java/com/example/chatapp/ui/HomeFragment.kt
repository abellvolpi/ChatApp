package com.example.chatapp.ui

import android.content.Intent
import android.app.Activity.RESULT_OK

import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.models.Message
import com.example.chatapp.ui.ProfileFragment.Companion.PICK_IMAGE
import com.example.chatapp.utils.Extensions.hideSoftKeyboard
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils.createSocket
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class HomeFragment : Fragment(), CoroutineScope {
    private lateinit var binding: FragmentHomeBinding
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main

    private val args: HomeFragmentArgs by navArgs()
    private val profileViewModel : ProfileViewModel by activityViewModels()
    private val connectionFactory: ConnectionFactory by activityViewModels()
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

    override fun onResume() {
        super.onResume()
        var list = activity?.intent?.data?.path?.split('/')
        var ipPort = list?.get(2)?.split(":")
        var ip = ipPort?.get(0)
        var port = ipPort?.get(1)


        with(binding) {
            ipField.setText(ip)
            radioGroupPort.forEach {
                with(it as RadioButton) {
                    if (text.equals(port)) {
                        isChecked = true
                        return@forEach
                    }
                }
            }
        }

    }

    override fun onStart() {
        super.onStart()


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            val ip = args.ip
            val port = args.port
            ipField.setText(ip)
            radioGroupPort.forEach {
                with(it as RadioButton) {
                    if (text.equals(port)) {
                        isChecked = true
                        return@forEach
                    }
                }
            }
            binding.constraintLayoutHome.setOnClickListener {
                activity?.hideSoftKeyboard()
            }
            val message = arguments?.getString("messageIfError")
            if (message != null) {
                if (message.isNotBlank()) {
                    Snackbar.make(requireContext(), requireView(), getString(R.string.server_disconnected, message), Snackbar.LENGTH_LONG).show()
                }
            }
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
            return false
        }
    }

    private fun radioGroupSelected(): String {
        binding.let {
            it.radioGroupPort.forEach { view ->
                with(view as RadioButton) {
                    if (isChecked) {
                        return text.toString()
                    }
                }
            }
        }
        return ""
    }

    private fun connect() {
        val port = radioGroupSelected().toInt()
        with(binding) {
            createSocket(ipField.text.toString(), port) {
                ProfileSharedProfile.saveProfile(nameField.text.toString())
                connectionFactory.setSocket(it)
                profileViewModel.deleteAll()
                var image = ""
                val bitmap = ProfileSharedProfile.getProfilePhoto()
                if (bitmap != null) {
                    image = ProfileSharedProfile.bitmapToByteArrayToString(bitmap)
                }
                val message = Message(
                    type = Message.MessageType.JOIN.code,
                    username = nameField.text.toString(),
                    text = null,
                    base64Data = null,
                    join = Message.Join(avatar = image, password = password.text.toString()),
                    id = null
                )
                val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(message)
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