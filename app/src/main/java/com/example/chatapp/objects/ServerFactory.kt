package com.example.chatapp.objects

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.CoroutineContext

class ServerFactory(var context: Context, var port: Int) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket

    fun serverConnecting(onResult: () -> Unit) {
        launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            socket = serverSocket.accept()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Conexão Estabelecida", Toast.LENGTH_SHORT).show()
                onResult.invoke()
            }
        }
    }

    fun getSocket(): Socket{
        return socket
    }



    fun closeConnection() {
        socket.close()
    }


}