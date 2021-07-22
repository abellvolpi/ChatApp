package com.example.chatapp.objects

import android.animation.TimeAnimator
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ServerFactory : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main

    var serverSocket = ServerSocket(1025)
    private lateinit var socket: Socket

    fun serverConnecting(context: Context) {
        launch(Dispatchers.IO) {
                socket = serverSocket.accept()
                val outputStream = socket.getOutputStream()
                outputStream.write("test333222\n".toByteArray(Charsets.US_ASCII))
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "aloga", Toast.LENGTH_SHORT).show()
                }

            }
    }

    fun closeConnection(){
        socket.close()
    }

}