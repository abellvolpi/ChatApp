package com.example.chatapp.ui

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.utils.ProfileSharedProfile
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class ProfileFragment : Fragment(), CoroutineScope {

    private lateinit var binding: FragmentProfileBinding
    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)?.supportActionBar?.show()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.username.text = ProfileSharedProfile.getProfile()
        if(ProfileSharedProfile.getProfilePhoto()!=null) {
            binding.photo.setImageBitmap(ProfileSharedProfile.getProfilePhoto())
        }

        binding.floatingEditButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)

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

    companion object {
        const val PICK_IMAGE = 1
    }


}