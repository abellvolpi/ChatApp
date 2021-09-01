package com.example.chatapp.adapters

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.*
import com.example.chatapp.models.Message
import com.example.chatapp.room.withs.MessagesWithProfile
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.Utils
import com.example.chatapp.viewModel.UtilsViewModel
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ChatAdapter(
    private val data: ArrayList<Message>,
    val liveDataToObserve: UtilsViewModel,
    val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<ChatAdapter.BaseViewHolder>() {

    private val context = MainApplication.getContextInstance()
    private lateinit var mediaPlayer: MediaPlayer
    private var positionMessageAudioRunning: Int = -1

    abstract inner class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(msg: Message)
    }

    inner class ViewHolderReceivedImage(private val binding: MessageReceivedImageBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                Log.w("Image: ", msg.base64Data.toString())
                name.text = msg.text
                time.text = timeFormatter(msg.time)
                msg.base64Data?.let {
                    val file = File(it)
                    Utils.uriToBitmap(file.toUri(), context.contentResolver) { bitmap ->
                        receivedImage.setImageBitmap(bitmap)
                    }
                }
                receivedImage.setOnClickListener { view ->
                    val uri = msg.base64Data
                    val extras = FragmentNavigatorExtras(receivedImage to "image_big")
                    findNavController(view).navigate(
                        R.id.action_chatFragment_to_imageFragment,
                        bundleOf("image" to uri),
                        null,
                        extras
                    )
                }

            }
        }
    }

    inner class ViewHolderSentImage(private val binding: MessageSentImageBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                Log.w("" +
                        "Image: ", msg.base64Data.toString())
                name.text = msg.text
                time.text = timeFormatter(msg.time)
                msg.base64Data?.let {
                    val file = File(it)
                    Utils.uriToBitmap(file.toUri(), context.contentResolver) { bitmap ->
                        sentImage.setImageBitmap(bitmap)
                    }
                    sentImage.setOnClickListener { view ->
                        val uri = msg.base64Data
                        val extras = FragmentNavigatorExtras(sentImage to "image_big")
                        findNavController(view).navigate(
                            R.id.action_chatFragment_to_imageFragment,
                            bundleOf("image" to uri),
                            null,
                            extras
                        )
                    }
                }
            }
        }
    }

    inner class ViewHolderReceivedMessage(private val binding: MessageReceivedItemBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                message.text = msg.text
                name.text = msg.username
                time.text = timeFormatter(msg.time)
            }
        }
    }

    inner class ViewHolderSentMessage(private val binding: MessageSentItemBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                time.text = timeFormatter(msg.time)
                if (msg.base64Data == null) {
                    message.text = msg.text
                } else {
                    message.text = msg.base64Data
                }
            }
        }
    }

    inner class ViewHolderNotifyMessage(private val binding: MessageNotifyItemBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                when (msg.type) {
                    Message.MessageType.JOIN.code -> {
                        message.text = MainApplication.getContextInstance()
                            .getString(R.string.player_connected, msg.username)
                    }
                    Message.MessageType.LEAVE.code -> {
                        message.text = MainApplication.getContextInstance()
                            .getString(R.string.player_disconnected, msg.username)
                    }
                }
            }
        }
    }

    inner class ViewHolderReceivedAudioMessage(private val binding: MessageReceivedAudioBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                if (positionMessageAudioRunning != -1) {
                    if (msg == data[positionMessageAudioRunning]) {
                        startAudio.visibility = View.GONE
                        stopAudio.visibility = View.VISIBLE
                    } else {
                        startAudio.visibility = View.VISIBLE
                        stopAudio.visibility = View.GONE
                    }
                } else {
                    startAudio.visibility = View.VISIBLE
                    stopAudio.visibility = View.GONE
                }
                liveDataToObserve.getHasAudioRunning().observe(lifecycleOwner, {
                    if (msg == data[it.first]) {
                        if (it.second) {
                            startAudio.visibility = View.GONE
                            stopAudio.visibility = View.VISIBLE
                        } else {
                            startAudio.visibility = View.VISIBLE
                            stopAudio.visibility = View.GONE
                        }
                    } else {
                        startAudio.visibility = View.VISIBLE
                        stopAudio.visibility = View.GONE
                    }
                })
                name.text = msg.username
                getAudio(msg) {
                    message.text = context.getString(
                        R.string.audio,
                        getTimeAudioInString(it.duration.toLong())
                    )
                    seekBarAudio.max = it.duration
                }

                time.text = timeFormatter(msg.time)
                startAudio.setOnClickListener {
                    startAudio(
                        msg,
                        layoutPosition,
                        seekBarAudio.progress
                    ) { long: Long ->
                        if (msg == data[positionMessageAudioRunning]) {
                            positionMessageAudioRunning = layoutPosition
                            reproduceTimeAudio.text = getTimeAudioInString(long)
                            seekBarAudio.progress = long.toInt()
                        }
                        mediaPlayer.setOnCompletionListener {
                            liveDataToObserve.changeAudioRunning(false, layoutPosition)
                            positionMessageAudioRunning = -1
                            reproduceTimeAudio.text = getTimeAudioInString(0)
                            seekBarAudio.progress = 0
                        }
                        seekBarAudio.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                if (mediaPlayer.isPlaying && fromUser && msg == data[positionMessageAudioRunning]) {
                                    mediaPlayer.seekTo(progress)
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                        })
                    }
                }

                stopAudio.setOnClickListener {
                    stopAudio()
                    liveDataToObserve.changeAudioRunning(false, layoutPosition)
                }
            }
        }
    }

    inner class ViewHolderSentAudioMessage(private val binding: MessageSentAudioBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                if (positionMessageAudioRunning != -1) {
                    if (msg == data[positionMessageAudioRunning]) {
                        startAudio.visibility = View.GONE
                        stopAudio.visibility = View.VISIBLE
                    } else {
                        startAudio.visibility = View.VISIBLE
                        stopAudio.visibility = View.GONE
                    }
                } else {
                    startAudio.visibility = View.VISIBLE
                    stopAudio.visibility = View.GONE
                }
                liveDataToObserve.getHasAudioRunning().observe(lifecycleOwner, {
                    if (msg == data[it.first]) {
                        if (it.second) {
                            startAudio.visibility = View.GONE
                            stopAudio.visibility = View.VISIBLE
                        } else {
                            startAudio.visibility = View.VISIBLE
                            stopAudio.visibility = View.GONE
                        }
                    } else {
                        startAudio.visibility = View.VISIBLE
                        stopAudio.visibility = View.GONE
                    }
                })
                name.text = context.getString(R.string.you)
                getAudio(msg) {
                    message.text = context.getString(
                        R.string.audio,
                        getTimeAudioInString(it.duration.toLong())
                    )
                    seekBarAudio.max = it.duration
                }
                time.text = timeFormatter(msg.time)
                startAudio.setOnClickListener {
                    startAudio(
                        msg,
                        layoutPosition,
                        seekBarAudio.progress
                    ) { long: Long ->
                        if (msg == data[positionMessageAudioRunning]) {
                            positionMessageAudioRunning = layoutPosition
                            reproduceTimeAudio.text = getTimeAudioInString(long)
                            seekBarAudio.progress = long.toInt()
                        }
                        mediaPlayer.setOnCompletionListener {
                            liveDataToObserve.changeAudioRunning(false, layoutPosition)
                            positionMessageAudioRunning = -1
                            reproduceTimeAudio.text = getTimeAudioInString(0)
                            seekBarAudio.progress = 0
                        }
                        seekBarAudio.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                if (mediaPlayer.isPlaying && fromUser && msg == data[positionMessageAudioRunning]) {
                                    mediaPlayer.seekTo(progress)
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                        })
                    }
                }
                stopAudio.setOnClickListener {
                    stopAudio()
                    liveDataToObserve.changeAudioRunning(false, layoutPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        with(viewType) {
            val viewTypeWithoutFirstNumber = viewType.toString().drop(1).toInt()
            if (viewTypeWithoutFirstNumber == Message.MessageType.JOIN.code || viewTypeWithoutFirstNumber == Message.MessageType.LEAVE.code) {
                return ViewHolderNotifyMessage(
                    MessageNotifyItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            if (toString().first() == '1') {
                when (viewTypeWithoutFirstNumber) {
                    Message.MessageType.MESSAGE.code -> {
                        return ViewHolderSentMessage(
                            MessageSentItemBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                    Message.MessageType.AUDIO.code -> {
                        return ViewHolderSentAudioMessage(
                            MessageSentAudioBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                    Message.MessageType.IMAGE.code -> {
                        return ViewHolderSentImage(
                            MessageSentImageBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                    else -> return ViewHolderNotifyMessage(
                        MessageNotifyItemBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                        )
                    )
                }
            } else {
                when (viewTypeWithoutFirstNumber) {
                    Message.MessageType.MESSAGE.code -> {
                        return ViewHolderReceivedMessage(
                            MessageReceivedItemBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                    Message.MessageType.AUDIO.code -> {
                        return ViewHolderReceivedAudioMessage(
                            MessageReceivedAudioBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                    Message.MessageType.IMAGE.code -> {
                        return ViewHolderReceivedImage(
                            MessageReceivedImageBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                            )
                        )
                    }
                    else -> return ViewHolderNotifyMessage(
                        MessageNotifyItemBinding.inflate(
                            LayoutInflater.from(parent.context), parent, false
                        )
                    )
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].status == Message.MessageStatus.SENT.code) {
            "1${data[position].type}".toInt() //when sent message
        } else {
            "2${data[position].type}".toInt()//when recent message
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val msg = data[position]
        holder.bind(msg)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("SimpleDateFormat")
    private fun timeFormatter(time: Long): String {
        val dtf = SimpleDateFormat("HH:mm")
        return dtf.format(time)
    }

    fun addData(message: Message) {
        data.add(message)
        notifyItemInserted(data.size - 1)
    }


    private fun startAudio(
        message: Message,
        position: Int,
        progressSeekBar: Int,
        onResult: (Long) -> Unit
    ) {
        getAudio(message) {
            if (positionMessageAudioRunning != -1) {
                stopAudio()
            }
            mediaPlayer = it
            mediaPlayer.seekTo(progressSeekBar)
            mediaPlayer.start()
            positionMessageAudioRunning = position
            liveDataToObserve.changeAudioRunning(true, position)
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    if (mediaPlayer.isPlaying) {
                        withContext(Dispatchers.Main) {
                            try {
                                onResult.invoke(mediaPlayer.currentPosition.toLong())
                            } catch (e: Exception) {
                                stopAudio()
                            }
                        }
                    } else {
                        break
                    }
                    delay(250)
                }
            }
        }
    }

    private fun stopAudio() {
        if (mediaPlayer.isPlaying) {
            liveDataToObserve.changeAudioRunning(false, positionMessageAudioRunning)
            positionMessageAudioRunning = -1
            mediaPlayer.stop()
        }
    }

    private fun getTimeAudioInString(long: Long): String {
        return SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date(long))
    }

    private fun getAudio(msg: Message, onResult: (MediaPlayer) -> Unit) {
        val an: MediaPlayer = MediaPlayer.create(
            MainApplication.getContextInstance(),
            Utils.getAudioFromCache(msg)?.toUri()
        )
        onResult.invoke(an)
    }
}