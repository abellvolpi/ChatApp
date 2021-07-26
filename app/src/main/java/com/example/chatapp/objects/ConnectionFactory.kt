package com.example.chatapp.objects


import com.example.chatapp.models.Message
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

class ConnectionFactory(val ip: String, val porta: Int) : CoroutineScope, Serializable {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    fun clientConnecting() {
        launch(Dispatchers.IO) {
            socket = Socket(ip, porta)
            socket.tcpNoDelay = true
        }
    }

    fun readMessage(onResult: (String) -> Unit) {
        launch(Dispatchers.IO) {
            Thread.sleep(2500)
            while (true){
                val reader = Scanner(socket.getInputStream().bufferedReader())
                val line = reader.nextLine()
                withContext(Dispatchers.Main){
                    onResult.invoke(line)
                }
            }
        }
    }

    fun sendMessage(message: Message, onResult: () -> Unit) {
        launch(Dispatchers.IO) {
            val bw = DataOutputStream(socket.getOutputStream())
            bw.write((Utils.messageClassToJSON(message)+"\n").toByteArray())
            bw.flush()
            withContext(Dispatchers.Main){
                onResult.invoke()
            }
        }
    }
}