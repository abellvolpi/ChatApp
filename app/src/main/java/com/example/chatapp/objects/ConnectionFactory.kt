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

    fun clientConnecting(ip: String = "", porta: Int = 0){
        launch (Dispatchers.IO) {
            val socket: Socket = Socket("192.168.11.78", 1337)
            val outputStream = socket.getOutputStream()
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val str = input.readLine()
            outputStream.write(str.toByteArray(Charsets.US_ASCII))
            socket.close()
        }
    }


}