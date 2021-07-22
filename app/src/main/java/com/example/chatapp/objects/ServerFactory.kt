package com.example.chatapp.objects

import android.animation.TimeAnimator
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ServerFactory: CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main

    var serverSocket = ServerSocket(13)
//    var client : Socket = serverSocket.accept()

    fun serverConnecting(){
        while (true){
            var client : Socket = serverSocket.accept()
//            Toast.makeText(requireContext(),"Testando:${client.inetAddress.hostAddress}",Toast.LENGTH_SHORT)
            var saida : ObjectOutputStream = ObjectOutputStream(client.getOutputStream())
            saida.flush()
            saida.writeObject(Date())
            saida.close()
            client.close()
        }
    }

}