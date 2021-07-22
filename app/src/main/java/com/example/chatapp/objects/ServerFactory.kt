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
//    var client : Socket = serverSocket.accept()

    fun serverConnecting(context: Context) {
        launch(Dispatchers.IO) {
                val serverSocket = ServerSocket(1337)
                val socket = serverSocket.accept()
                val outputStream = socket.getOutputStream()
                outputStream.write("test333222\n".toByteArray(Charsets.US_ASCII))
                withContext(Dispatchers.Main){
                    Toast.makeText(context, "aloga", Toast.LENGTH_SHORT).show()
                }
                socket.close()
            }



//            while (true) {
//                val client = serverSocket.accept()
//                val input = BufferedReader(InputStreamReader(client.getInputStream()))
//                val output = client.getOutputStream()
//                var stringRecebida = input.readLine()
//                output.write("Testando".toByteArray(Charsets.US_ASCII))
//                client.close()
//                withContext(Dispatchers.Main){
//                    Toast.makeText(context, "Testando:${client.inetAddress.hostAddress}", Toast.LENGTH_SHORT)
//                }
//
//            }
    }

}