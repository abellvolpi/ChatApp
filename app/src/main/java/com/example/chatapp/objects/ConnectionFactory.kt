package com.example.chatapp.objects

import com.example.chatapp.ui.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object ConnectionFactory: CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    fun clientConnecting(ip: String, porta: Int){
        launch (Dispatchers.IO) {
            socket = Socket(ip, porta)
        }
    }

    fun readMessage(){

    }
    fun sendMessage(message: Message) {
        launch(Dispatchers.IO) {
            val bw = socket.getOutputStream()
            bw.write(Utils.messageClassToJSON(message).toByteArray())
        }
    }


}