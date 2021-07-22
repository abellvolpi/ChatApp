package com.example.chatapp.ui.model

class Message(val name: String,
              val message: String,
              val typeMesage: Int,
              val date: Long,
              val macAndress: String) {

    companion object{
        // type of messages
        const val RECEIVED_MESSAGE = 0
        const val SENT_MESSAGE = 1
        const val NOTIFY_CHAT = 2
    }
}