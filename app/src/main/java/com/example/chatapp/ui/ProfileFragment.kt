package com.example.chatapp.ui


import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.utils.ProfileSharedProfile
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ProfileFragment : Fragment(), CoroutineScope {

    private lateinit var binding: FragmentProfileBinding
    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()
    private lateinit var startActivityLaunch: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity?)?.supportActionBar?.hide()


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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){

            username.text = ProfileSharedProfile.getProfile()
            if (ProfileSharedProfile.getUriProfilePhoto() != null) {
                binding.photo.setImageURI(ProfileSharedProfile.getUriProfilePhoto())
            }
            floatingEditButton.setOnClickListener {
                startActivityLaunch.launch("image/*")
            }
            profileToolbar.setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }
    }
}