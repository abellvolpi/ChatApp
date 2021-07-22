package com.example.chatapp.ui.model

import com.example.chatapp.objects.Utils
import java.util.*

class Message(val name: String,
              val message: String,
              var typeMesage: Int? = null,
              val date: Long = Calendar.getInstance().time.time,
              val macAndress: String = Utils.getMacAndress()) {

    companion object{
        // type of messages
        const val RECEIVED_MESSAGE = 0
        const val SENT_MESSAGE = 1
        const val NOTIFY_CHAT = 2
    }
}