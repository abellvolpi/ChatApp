package com.example.chatapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.MessageNotifyItemBinding
import com.example.chatapp.databinding.MessageReceivedItemBinding
import com.example.chatapp.databinding.MessageSentItemBinding
import com.example.chatapp.models.Message
import java.text.SimpleDateFormat

class ChatAdapter(var data: ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var bindingSent: MessageSentItemBinding
    private lateinit var bindingReceived: MessageReceivedItemBinding
    private lateinit var bindingNotify: MessageNotifyItemBinding

    inner class ViewHolderReceiveMessage(binding: MessageReceivedItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    inner class ViewHolderSentMessage(binding: MessageSentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    inner class ViewHolderNotifyMessage(binding: MessageNotifyItemBinding) :
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
                bindingSent.name.text = data[position].name
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
}