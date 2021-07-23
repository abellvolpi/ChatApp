package com.example.chatapp.objects

import android.util.Log
import com.example.chatapp.models.Message
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ConnectionFactory : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    fun clientConnecting(ip: String, porta: Int) {
        launch(Dispatchers.IO) {
            socket = Socket(ip, porta)
            
        }
    }

    fun readMessage(onResult: (List<Message>) -> Unit) {
        launch(Dispatchers.IO) {

            var msg =""
            var scanner = Scanner(socket.getInputStream())
            while (scanner.hasNextLine()){
                    msg = scanner.nextLine()
            }
            scanner.close()
            Log.e("OutPutMessage2", msg)

        }
    }

    fun sendMessage(message: Message, onResult: () -> Unit) {
        launch(Dispatchers.IO) {
            val bw = socket.getOutputStream()
            bw.write(Utils.messageClassToJSON(message).toByteArray())
            bw.flush()
            onResult.invoke()
        }
    }
}