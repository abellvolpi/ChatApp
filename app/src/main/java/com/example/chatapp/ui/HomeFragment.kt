package com.example.chatapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.models.Message
import com.example.chatapp.utils.Extensions.hideSoftKeyboard
import com.example.chatapp.utils.Extensions.toSHA256
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.createSocket
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.MessageViewModel
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
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val messageViewModel : MessageViewModel by activityViewModels()
    private val connectionFactory: ConnectionFactory by activityViewModels()
    private lateinit var startActivityLaunch: ActivityResultLauncher<String>
    private val navController by lazy {
        findNavController()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        ProfileSharedProfile.clearSharedPreferences()
        setHasOptionsMenu(true)

        startActivityLaunch = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            launch(Dispatchers.Default) {
                val imageBitmap = context?.contentResolver?.let { Utils.uriToBitmap(uri, it) }
                imageBitmap?.let { ProfileSharedProfile.saveProfilePhoto(it) }
            }
            launch(Dispatchers.Main) {
                binding.photo.setImageURI(uri)
            }
        }
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
        val list = activity?.intent?.data?.path?.split('/')
        val ipPort = list?.get(2)?.split(":")
        val ip = ipPort?.get(0)
        val port = ipPort?.get(1)

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
            constraintLayoutHome.setOnClickListener {
                activity?.hideSoftKeyboard()
            }
            val message = arguments?.getString("messageIfError")
            if (message != null) {
                if (message.isNotBlank()) {
                    Snackbar.make(
                        requireContext(),
                        requireView(),
                        getString(R.string.server_disconnected, message),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            historyButton.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(null, true)
                navController.navigate(action)
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
                    ProfileSharedProfile.saveProfile(nameField.text.toString())
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToCameraQrCodeScan()
                    navController.navigate(action)
                } else {
                    nameField.error = "Please, insert your name"
                }
            }
            floatingEditButton.setOnClickListener {
                startActivityLaunch.launch("image/*")
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
                if (it != null) {
                    ProfileSharedProfile.saveProfile(nameField.text.toString())
                    connectionFactory.setSocket(it)
                    messageViewModel.deleteAll {
                        profileViewModel.deleteAll {
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
                                join = Message.Join(
                                    avatar = image,
                                    password = password.text.toString().toSHA256()
                                ),
                                id = null
                            )
                            val action =
                                HomeFragmentDirections.actionHomeFragmentToChatFragment(
                                    message,
                                    false
                                )
                            findNavController().navigate(action)
                        }
                    }
                } else {
                    val snackbar = Snackbar.make(
                        requireView(),
                        "Server doest exists",
                        Snackbar.LENGTH_LONG
                    )
                    snackbar.show()
                }
            }
        }
    }
}