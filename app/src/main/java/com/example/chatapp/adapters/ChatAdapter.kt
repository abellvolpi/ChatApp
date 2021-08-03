package com.example.chatapp.adapters

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.*
import com.example.chatapp.models.Message
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatAdapter(var data: ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var bindingSent: MessageSentItemBinding
    private lateinit var bindingReceived: MessageReceivedItemBinding
    private lateinit var bindingNotify: MessageNotifyItemBinding
    private lateinit var bindingSentAudio: MessageSentAudioBinding
    private lateinit var bindingReceivedAudio: MessageReceivedAudioBinding
    private var mediaPlayer = MediaPlayer()


    inner class ViewHolderReceiveMessage(binding: MessageReceivedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    inner class ViewHolderSentMessage(binding: MessageSentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    inner class ViewHolderNotifyMessage(binding: MessageNotifyItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
    inner class ViewHolderReceiveAudioMessage(binding: MessageReceivedAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }
    inner class ViewHolderSentAudioMessage(binding: MessageSentAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            Message.SENT_MESSAGE -> {
                bindingSent = MessageSentItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolderSentMessage(bindingSent)
            }

            Message.RECEIVED_MESSAGE -> {
                bindingReceived = MessageReceivedItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ViewHolderReceiveMessage(bindingReceived)
            }
            Message.NOTIFY_CHAT -> {
                bindingNotify = MessageNotifyItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                return ViewHolderNotifyMessage(bindingNotify)
            }

            Message.SENT_MESSAGE_VOICE ->{
                bindingSentAudio = MessageSentAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolderSentAudioMessage(bindingSentAudio)
            }

            Message.RECEIVED_MESSAGE_VOICE -> {
                bindingReceivedAudio = MessageReceivedAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ViewHolderReceiveAudioMessage(bindingReceivedAudio)
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


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            Message.SENT_MESSAGE -> {
                bindingSent.message.text = data[position].message
                bindingSent.name.text = "You"
                bindingSent.time.text = timeFormatter(data[position].date)
            }
            Message.RECEIVED_MESSAGE -> {
                bindingReceived.message.text = data[position].message
                bindingReceived.name.text = data[position].name
                bindingReceived.time.text = timeFormatter(data[position].date)
            }
            Message.NOTIFY_CHAT -> {
                bindingNotify.message.text = data[position].message
            }
            Message.SENT_MESSAGE_VOICE ->{
                bindingSentAudio.name.text = data[position].name
                bindingSentAudio.startAudio.visibility = View.VISIBLE
                bindingSentAudio.stopAudio.visibility = View.GONE
                bindingSentAudio.name.text = "You"
                bindingSentAudio.message.text = "Audio"
                bindingSentAudio.time.text = timeFormatter(data[position].date)
                bindingSentAudio.startAudio.setOnClickListener {
                    startAudio(data[position].message)
                    CoroutineScope(Dispatchers.IO).launch {
                        while(true){
                            if(mediaPlayer.isPlaying){
                                withContext(Dispatchers.Main){
                                    bindingSentAudio.startAudio.visibility = View.GONE
                                    bindingSentAudio.stopAudio.visibility = View.VISIBLE
                                }
                            }else{
                                withContext(Dispatchers.Main){
                                    bindingSentAudio.startAudio.visibility = View.VISIBLE
                                    bindingSentAudio.stopAudio.visibility = View.GONE
                                }
                                break
                            }
                        }
                    }
                }
                bindingSentAudio.stopAudio.setOnClickListener {
                    stopAudio()
                }
            }
            Message.RECEIVED_MESSAGE_VOICE ->{
                bindingReceivedAudio.name.text = data[position].name
                bindingReceivedAudio.startAudio.visibility = View.VISIBLE
                bindingReceivedAudio.stopAudio.visibility = View.GONE
                bindingReceivedAudio.name.text = data[position].name
                bindingReceivedAudio.message.text = "Audio"
                bindingReceivedAudio.time.text = timeFormatter(data[position].date)
                bindingReceivedAudio.startAudio.setOnClickListener {
                    startAudio(data[position].message)
                    CoroutineScope(Dispatchers.IO).launch {
                        while(true){
                            if(mediaPlayer.isPlaying){
                                withContext(Dispatchers.Main){
                                    bindingReceivedAudio.startAudio.visibility = View.GONE
                                    bindingReceivedAudio.stopAudio.visibility = View.VISIBLE
                                }
                            }else{
                                withContext(Dispatchers.Main){
                                    bindingReceivedAudio.startAudio.visibility = View.VISIBLE
                                    bindingReceivedAudio.stopAudio.visibility = View.GONE
                                }
                                break
                            }
                        }
                    }
                }
                bindingReceivedAudio.stopAudio.setOnClickListener {
                    stopAudio()
                }
            }
        }
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
        notifyItemInserted(data.size-1)
    }
    private fun startAudio(message: String) {
        Utils.parseBytoToAudio(message){
            stopAudio()
            mediaPlayer = MediaPlayer.create(MainApplication.getContextInstance(), Uri.fromFile(it))
            mediaPlayer.start()
        }
    }
    private fun stopAudio(){
        if(mediaPlayer.isPlaying){
            mediaPlayer.stop()
        }
    }
}