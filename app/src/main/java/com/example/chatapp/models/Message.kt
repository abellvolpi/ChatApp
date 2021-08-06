package com.example.chatapp.models

import com.example.chatapp.utils.Utils
import java.io.Serializable
import java.util.*

class Message(val name: String,
              val message: String,
              var typeMesage: Int,
              val date: Long = Calendar.getInstance().time.time,
              val ipAndress: String = Utils.getIpAndress()): Serializable {

    companion object{
        // type of messages
        const val RECEIVED_MESSAGE = 0
        const val SENT_MESSAGE = 1
        const val NOTIFY_CHAT = 2
        const val INVITE_TICTACTOE = 3
        const val RECEIVE_PLAY = 4
        const val RECEIVED_MESSAGE_VOICE = 5
        const val SENT_MESSAGE_VOICE = 6
    }
}