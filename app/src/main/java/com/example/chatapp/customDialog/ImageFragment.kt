package com.example.chatapp.customDialog


import android.os.Bundle
import android.transition.TransitionInflater
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.chatapp.databinding.FragmentImageBinding
import com.example.chatapp.utils.ProfileSharedProfile

class ImageFragment : Fragment() {

    private lateinit var binding: FragmentImageBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImageBinding.inflate(inflater, container, false)



        ProfileSharedProfile.getUriProfilePhoto()?.let {
            binding.photoImageFragment.setImageURI(it)
        }
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){

            imageFragmentToolbar.setNavigationOnClickListener {
                activity?.onBackPressed()
            }

        }

    }





}