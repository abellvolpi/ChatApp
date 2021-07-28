package com.example.chatapp.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.MessageReceivedItemBinding
import com.example.chatapp.utils.Utils
import com.example.chatapp.models.Message
import java.text.SimpleDateFormat

class ChatAdapter(var data: ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var binding: MessageReceivedItemBinding

    inner class ViewHolder(binding: MessageReceivedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding =
            MessageReceivedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (data[position].typeMesage) {
            Message.SENT_MESSAGE -> {
                setSentMessageParams(context, data[position])
            }
            Message.RECEIVED_MESSAGE -> {
                setReceivedMessageParams(context, data[position])
            }
            Message.NOTIFY_CHAT -> {
                setNotificationLayoutParams(context, data[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setNotificationLayoutParams(context: Context, messageF: Message) {
        with(binding) {
            messageF.let {
                mainLinearLayoutChat.gravity = Gravity.CENTER
                childLinearLayoutChat.background = context.getDrawable(R.drawable.notify_shape)
                name.textAlignment = View.TEXT_ALIGNMENT_CENTER
                message.text = it.message
                name.visibility = View.GONE
                time.visibility = View.GONE
                name.text = ""
                time.text = ""
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setReceivedMessageParams(context: Context, messageF: Message) {
        with(binding) {
            messageF.let {
                mainLinearLayoutChat.gravity = Gravity.START
                childLinearLayoutChat.background =
                    context.getDrawable(R.drawable.received_message_shape)
                time.text = timeFormatter(it.date)
                name.text = it.name
                name.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                message.text = it.message
                name.visibility = View.VISIBLE
                time.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSentMessageParams(context: Context, messageF: Message) {
        with(binding) {
            messageF.let {
                mainLinearLayoutChat.gravity = Gravity.END
                childLinearLayoutChat.background =
                    context.getDrawable(R.drawable.sent_message_shape)
                time.text = timeFormatter(it.date)
                name.text = "You"
                name.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                message.text = it.message
                name.visibility = View.VISIBLE
                time.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun timeFormatter(time: Long): String{
        val dtf = SimpleDateFormat("HH:mm")
        return dtf.format(time)
    }

    fun addData(message: Message){
        data.add(message)
    }
}