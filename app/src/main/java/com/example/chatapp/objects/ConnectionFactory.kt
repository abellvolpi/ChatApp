package com.example.chatapp.objects

import com.example.chatapp.ui.model.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object ConnectionFactory: CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    fun clientConnecting(ip: String, porta: Int){
        launch (Dispatchers.IO) {
            socket = Socket(ip, porta)
//            val outputStream = socket.getOutputStream()
//            val inputStream = socket.getInputStream()

//            val dataOutputStream = DataOutputStream(outputStream)
//            dataOutputStream.write("teste".toByteArray(Charsets.US_ASCII))
//            dataOutputStream.flush()
//            dataOutputStream.close()
//
////            val output = PrintWriter(OutputStreamWriter(socket.getOutputStream())) //ok
////            val input = BufferedReader(InputStreamReader(socket.getInputStream())) //ok
////            val str = input.readLine()
////            outputStream.write(str.toByteArray(Charsets.US_ASCII))
//
//
//            socket.close()
        }
    }

    fun readMessage(){

    }
    fun sendMessage(message: Message){
        val bw = socket.getOutputStream()
        bw.write(Utils.messageClassToJSON(message).toByteArray())
//        socket.getOutputStream().bufferedWriter().use {
//            it.appendLine(Utils.messageClassToJSON(message)+"@%#")
//        }
    }


}