package com.example.chatapp.objects

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.xml.sax.DTDHandler
import java.io.*
import java.net.Inet4Address
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ConnectionFactory: CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main

    fun clientConnecting(){
        launch (Dispatchers.IO) {
            var socket: Socket = Socket("algum.ip", 1025)
            val outputStream = socket.getOutputStream()
            //ok

            var input = BufferedReader(InputStreamReader(socket.getInputStream()))
            var str = input.readLine()
            outputStream.write(str.toByteArray(Charsets.US_ASCII))
            println("Teste: " + input.readLine())
            socket.close()
        }

    }


}