package com.example.chatapp.objects

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ServerFactory : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    lateinit var socket: Socket

    fun serverConnecting(context: Context, port: Int) {
        launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            socket = serverSocket.accept()



            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Conexão Estabelecida", Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun closeConnection() {
        socket.close()
    }

    fun readMessages() {

    }


}