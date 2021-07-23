package com.example.chatapp.objects

import android.util.Log
import com.example.chatapp.models.Message
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ConnectionFactory : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    fun clientConnecting(ip: String, porta: Int){
        launch (Dispatchers.IO) {
            socket = Socket(ip, porta)
        }
    }


    fun readMessage(onResult: (List<Message>) -> Unit) {
        launch(Dispatchers.IO) {
            val bfr = BufferedReader(InputStreamReader(socket.getInputStream()))
            while(bfr.ready()){
                val string = bfr.readText()
                Log.e("OutPutMessage", string)
            }
        }
    }
        fun sendMessage(message: Message) {
            launch(Dispatchers.IO) {
                val bw = socket.getOutputStream()
                bw.write(Utils.messageClassToJSON(message).toByteArray())
            }
        }


}