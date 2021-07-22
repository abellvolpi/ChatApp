package com.example.chatapp.objects

import android.widget.Toast
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

    fun clientConnecting(ip: String, porta: Int){
        launch (Dispatchers.IO) {
            val socket: Socket = Socket(ip, porta)
            val outputStream = socket.getOutputStream()
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            while (input.ready()){
               input.readLines().last()
            }
            val str = input.readLine()
            outputStream.write(str.toByteArray(Charsets.US_ASCII))
            socket.close()
        }
    }


}