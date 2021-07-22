package com.example.chatapp.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.MessageReceivedItemBinding
import com.example.chatapp.ui.model.Message
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val context = holder.itemView.context
        when (data[position].typeMesage) {
            0 -> {
                setSentMessageParams(context, position)
            }
            1 -> {
                setReceivedMessageParams(context, position)
            }
            2 -> {
                setNotificationLayoutParams(context, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setNotificationLayoutParams(context: Context, position: Int) {
        with(binding) {
            mainLinearLayoutChat.gravity = Gravity.CENTER
            childLinearLayoutChat.background = context.getDrawable(R.drawable.notify_shape)
            message.text = data[position].message
            name.visibility = View.GONE
            time.visibility = View.GONE
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setReceivedMessageParams(context: Context, position: Int) {
        val actualData = data[position]
        with(binding) {
            mainLinearLayoutChat.gravity = Gravity.START
            childLinearLayoutChat.background = context.getDrawable(R.drawable.received_message_shape)
            time.text = timeFormatter(actualData.date)
            name.text = actualData.name
            name.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
            message.text = actualData.message
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setSentMessageParams(context: Context, position: Int) {
        val actualData = data[position]
        with(binding) {
            mainLinearLayoutChat.gravity = Gravity.END
            childLinearLayoutChat.background = context.getDrawable(R.drawable.sent_message_shape)
            time.text = timeFormatter(actualData.date)
            name.text = "You"
            message.text = actualData.message
        }
    }
    @SuppressLint("SimpleDateFormat")
    private fun timeFormatter(time: Long): String{
        val dtf = SimpleDateFormat("dd/MM/yyyy HH:mm")
        return dtf.format(time)
    }

}