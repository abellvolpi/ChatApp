package com.example.chatapp.objects

import android.content.Context
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
            socket = serverSocket.accept()

//            val outputStream = socket.getOutputStream()
//            val inputStream = socket.getInputStream()
//
//            val dataInputStream = DataInputStream(inputStream)
//            var receivedMessage = dataInputStream.readUTF()
            withContext(Dispatchers.Main){
                Toast.makeText(context, "Conexão Estabelecida", Toast.LENGTH_SHORT).show()
            }




//            val output = PrintWriter(OutputStreamWriter(socket.getOutputStream())) //ok
//            val input = BufferedReader(InputStreamReader(socket.getInputStream())) //ok
//                outputStream.write("test333222\n".toByteArray(Charsets.US_ASCII))
        }
    }


    fun closeConnection() {
        socket.close()
    }

}