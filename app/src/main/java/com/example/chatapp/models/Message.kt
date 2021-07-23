package com.example.chatapp.models

import com.example.chatapp.utils.Utils
import java.util.*

class Message(val name: String,
              val message: String,
              var typeMesage: Int,
              val date: Long = Calendar.getInstance().time.time,
              val macAndress: String = Utils.getMacAndress()) {

    companion object{
        // type of messages
        const val RECEIVED_MESSAGE = 0
        const val SENT_MESSAGE = 1
        const val NOTIFY_CHAT = 2
    }
}