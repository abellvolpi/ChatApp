package com.example.chatapp.objects

import com.example.chatapp.models.Message
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.xml.sax.DTDHandler
import java.io.*
import java.net.Inet4Address
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ConnectionFactory : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    fun clientConnecting(ip: String, porta: Int) {
        launch(Dispatchers.IO) {
            socket = Socket(ip, porta)
        }
    }


    fun readMessage(onResult: (List<Message>) -> Unit) {
        launch(Dispatchers.IO) {
            socket.getOutputStream()
        }
    }
        fun sendMessage(message: Message) {
            launch(Dispatchers.IO) {
                val bw = socket.getOutputStream()
                bw.write(Utils.messageClassToJSON(message).toByteArray())
            }
        }


    }