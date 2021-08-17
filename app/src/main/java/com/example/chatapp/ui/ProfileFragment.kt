package com.example.chatapp.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
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
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri ->
                binding.photo.setImageURI(uri)
            }
        )
    }

    override fun onResume() {
        super.onResume()
//        val actionBar = (activity as AppCompatActivity?)?.supportActionBar
//        actionBar?.title = getString(R.string.profile)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.username.text = ProfileSharedProfile.getProfile()
        if (ProfileSharedProfile.getProfilePhoto() != null) {
            binding.photo.setImageBitmap(ProfileSharedProfile.getProfilePhoto())
        }

        binding.floatingEditButton.setOnClickListener {
            startActivityLaunch.launch("image/*")
        }



    }



}