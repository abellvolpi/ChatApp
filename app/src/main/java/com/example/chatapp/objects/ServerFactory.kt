package com.example.chatapp.objects

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object ServerFactory : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    lateinit var socket: Socket

    fun serverConnecting(context: Context, port: Int) {
        launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            while (true){
                socket = serverSocket.accept()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Conexão Estabelecida", Toast.LENGTH_SHORT).show()
                }
                val input = DataInputStream(BufferedInputStream(socket.getInputStream()))
                val message = input.readLine()
                Log.e("OutPutMessage1", message)
            }
        }
    }


    fun closeConnection() {
        socket.close()
    }

    fun readMessages() {

    }


}