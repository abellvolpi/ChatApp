package com.example.chatapp.adapters

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.*
import com.example.chatapp.models.Message
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.Utils
import com.example.chatapp.viewModel.UtilsViewModel
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val data: ArrayList<Message>,
    val liveDataToObserve: UtilsViewModel,
    val lifecycleOwner: LifecycleOwner
) :
    RecyclerView.Adapter<ChatAdapter.BaseViewHolder>() {
    private lateinit var mediaPlayer: MediaPlayer
    private var positionMessageAudioRunning: Int = -1

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(msg: Message)
    }

    inner class ViewHolderReceiveMessage(private val binding: MessageReceivedItemBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                message.text = msg.message
                name.text = msg.name
                time.text = timeFormatter(msg.date)
            }
        }
    }

    inner class ViewHolderSentMessage(private val binding: MessageSentItemBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                message.text = msg.message
                name.text = msg.name
                time.text = timeFormatter(msg.date)
            }
        }
    }

    inner class ViewHolderNotifyMessage(private val binding: MessageNotifyItemBinding) :
        BaseViewHolder(binding.root) {
        override fun bind(msg: Message) {
            with(binding) {
                message.text = msg.message
            }
        }
    }

    inner class ViewHolderReceiveAudioMessage(private val binding: MessageReceivedAudioBinding) :
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
                liveDataToObserve.getHasAudioRunning().observe(lifecycleOwner, Observer {
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
                name.text = msg.name
                name.text = "You"
                getAudio(msg.message) {
                    message.text = "Audio (${getTimeAudioInString(it.duration.toLong())})"
                    seekBarAudio.max = it.duration
                }
                time.text = timeFormatter(msg.date)
                startAudio.setOnClickListener {
                    startAudio(msg.message, layoutPosition, seekBarAudio.progress) {long: Long ->
                        if(msg == data[positionMessageAudioRunning]){
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
                        seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                if(mediaPlayer.isPlaying && fromUser && msg == data[positionMessageAudioRunning]){
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
                liveDataToObserve.getHasAudioRunning().observe(lifecycleOwner, Observer {
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
                name.text = msg.name
                name.text = "You"
                getAudio(msg.message) {
                    message.text = "Audio (${getTimeAudioInString(it.duration.toLong())})"
                    seekBarAudio.max = it.duration
                }
                time.text = timeFormatter(msg.date)
                startAudio.setOnClickListener {
                    startAudio(msg.message, layoutPosition, seekBarAudio.progress) {long: Long ->
                        if(msg == data[positionMessageAudioRunning]){
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
                        seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                if(mediaPlayer.isPlaying && fromUser && msg == data[positionMessageAudioRunning]){
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
        when (viewType) {
            Message.SENT_MESSAGE -> {
                return ViewHolderSentMessage(
                    MessageSentItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            Message.RECEIVED_MESSAGE -> {
                return ViewHolderReceiveMessage(
                    MessageReceivedItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            Message.NOTIFY_CHAT -> {
                return ViewHolderNotifyMessage(
                    MessageNotifyItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
            }

            Message.SENT_MESSAGE_VOICE -> {
                return ViewHolderSentAudioMessage(
                    MessageSentAudioBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }

            Message.RECEIVED_MESSAGE_VOICE -> {
                return ViewHolderReceiveAudioMessage(
                    MessageReceivedAudioBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> return ViewHolderNotifyMessage( //apenas para validar um else
                MessageNotifyItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].typeMesage
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

    private fun startAudio(message: String, position: Int, progressSeekBar: Int, onResult: (Long) -> Unit) {
        Utils.parseBytoToAudio(message) {
            if (positionMessageAudioRunning != -1) {
                stopAudio()
            }
            mediaPlayer =
                MediaPlayer.create(MainApplication.getContextInstance(), Uri.fromFile(it))
            mediaPlayer.seekTo(progressSeekBar)
            mediaPlayer.start()
            positionMessageAudioRunning = position
            liveDataToObserve.changeAudioRunning(true, position)
            CoroutineScope(Dispatchers.IO).launch {
                while (true) {
                    if(mediaPlayer.isPlaying) {
                        withContext(Dispatchers.Main) {
                            onResult.invoke(mediaPlayer.currentPosition.toLong())
                        }
                    }else{
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

    private fun getAudio(msg: String, onResult: (MediaPlayer) -> Unit) {
        var an: MediaPlayer
        Utils.parseBytoToAudio(msg) {
            an = MediaPlayer.create(MainApplication.getContextInstance(), Uri.fromFile(it))
            onResult.invoke(an)
        }
    }
}