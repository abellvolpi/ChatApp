package com.example.chatapp.objects

import android.animation.TimeAnimator
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
            while (true) {
                var client = serverSocket.accept()
                var input = BufferedReader(InputStreamReader(client.getInputStream()))
                var output = client.getOutputStream()
                var stringRecebida = input.readLine()
                output.write("Testando".toByteArray(Charsets.US_ASCII))
                client.close()
                Toast.makeText(context, "Testando:${client.inetAddress.hostAddress}", Toast.LENGTH_SHORT)
            }
        }
    }

}