package com.example.chatapp.objects

import android.content.Context
import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object ServerFactory : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket

    fun serverConnecting(context: Context, port: Int) {
        launch(Dispatchers.IO) {
                val serverSocket = ServerSocket(port)
                socket = serverSocket.accept()
                val outputStream = socket.getOutputStream()
//                outputStream.write("test333222\n".toByteArray(Charsets.US_ASCII))
            }
    }

    fun closeConnection(){
        socket.close()
    }

}