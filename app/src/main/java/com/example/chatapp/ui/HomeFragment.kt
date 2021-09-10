package com.example.chatapp.ui

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.models.Message
import com.example.chatapp.utils.Extensions.hideSoftKeyboard
import com.example.chatapp.utils.Extensions.toSHA256
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils.createSocket
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.MessageViewModel
import com.example.chatapp.viewModel.ProfileViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class HomeFragment : Fragment(), CoroutineScope {
    private lateinit var binding: FragmentHomeBinding
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private val args: HomeFragmentArgs by navArgs()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val messageViewModel: MessageViewModel by activityViewModels()
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
//                val imageBitmap = context?.contentResolver?.let { Utils.uriToBitmap(uri, it) }
//                imageBitmap?.let { ProfileSharedProfile.saveProfilePhoto(it) }

                uri?.let { ProfileSharedProfile.saveUriProfilePhoto(uri) }

            }
            launch(Dispatchers.Main) {
                uri?.let { binding.photo.setImageURI(it) }
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
                        getString(R.string.server_disconnected),
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

            ProfileSharedProfile.getUriProfilePhoto()?.let {
                photo.setImageURI(it)
            }

            photo.setOnClickListener {
                val uri: String = if (ProfileSharedProfile.getUriProfilePhoto()!=null) {
                    ProfileSharedProfile.getUriProfilePhoto().toString()
                } else{
                    "android.resource://" + requireActivity().packageName + "/" + R.drawable.ic_profile
                }

                val extras = FragmentNavigatorExtras(photo to "image_big")
                navController.navigate(
                    R.id.action_homeFragment_to_imageFragment,
                    bundleOf("image" to uri),
                    null,
                    extras
                )


//                var drawable = photo.drawable.toBitmap()
//                Utils.openImageLikeDialog(requireContext(), drawable)

//                zoomImageFromThumb(photo,photo.drawable)
            }
            connect.setOnClickListener {
                if (!isEditTextIsEmpty()) {
                    progressBar.visibility = View.VISIBLE
                    connect()
                }
            }
            createServer.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToCreateServer()
                navController.navigate(action)
            }
            ipInputLayout.setEndIconOnClickListener {
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
            if (ipField.text.toString().isBlank()) {
                ipField.error = getString(R.string.ip_error)
                return true
            }
            if (nameField.text.toString().isBlank()) {
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
                    connectionFactory.serverOnline.postValue(true)
                    messageViewModel.deleteAll {
                        profileViewModel.deleteAll {
                            var image = ""
                            ProfileSharedProfile.getProfilePhoto { bitmap ->
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
                                        password = password.text.toString().toSHA256(), false
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
                    }
                } else {
                    val snackBar = Snackbar.make(
                        requireView(),
                        "Server doest exists",
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.show()
                    progressBar.visibility = View.INVISIBLE
                }
            }
        }
    }
}