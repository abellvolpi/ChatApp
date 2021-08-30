package com.example.chatapp.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RadioButton
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0
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
//                val imageBitmap = context?.contentResolver?.let { Utils.uriToBitmap(uri, it) }
//                imageBitmap?.let { ProfileSharedProfile.saveProfilePhoto(it) }
            ProfileSharedProfile.saveUriProfilePhoto(uri)
            binding.photo.setImageURI(uri)
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

            ProfileSharedProfile.getUriProfilePhoto()?.let {
                photo.setImageURI(it)
            }

            photo.setOnClickListener {
                val extras = FragmentNavigatorExtras(photo to "image_big")
                navController.navigate(
                    R.id.action_homeFragment_to_imageFragment,
                    null,
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

    private fun zoomImageFromThumb(thumbView: View, imageDrawable: Drawable) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        val expandedImageView: ImageView = requireActivity().findViewById(R.id.expanded_image)
        expandedImageView.setImageDrawable(imageDrawable)

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        requireActivity().findViewById<View>(R.id.constraint_layout_home)
            .getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f
        expandedImageView.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.pivotX = 0f
        expandedImageView.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    expandedImageView,
                    View.X,
                    startBounds.left,
                    finalBounds.left
                )
            ).apply {
                with(
                    ObjectAnimator.ofFloat(
                        expandedImageView,
                        View.Y,
                        startBounds.top,
                        finalBounds.top
                    )
                )
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        expandedImageView.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        expandedImageView.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }


}