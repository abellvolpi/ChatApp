package com.example.chatapp.models

import java.io.Serializable
import java.util.*

class Message(var type: Int,
              var status: Int = MessageStatus.SENT.code,
              val username: String?,
              val text: String?,
              val base64Data: String?,
              val time: Long = Calendar.getInstance().time.time,
              val id: Int?,
              val join: Join? = null): Serializable {

    data class Join(val avatar: String, val password: String): Serializable


    companion object{
        const val ACTION_DISCONNECTED = "ACTION_DISCONNECTED"
    }

    enum class MessageType(val code: Int) {
        MESSAGE(0), JOIN(1), VIBRATE(2), AUDIO(3), IMAGE(4), TICINVITE(5), TICPLAY(6), LEAVE(7), ACKNOWLEDGE(8), REVOKED(9)
    }

    enum class MessageStatus(val code: Int) {
        RECEIVED(0), SENT(1)
    }
}