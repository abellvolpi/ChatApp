package com.example.chatapp.objects


import com.example.chatapp.models.Message
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

class ConnectionFactory() : CoroutineScope, Serializable {
    constructor(socket: Socket): this(){
        this.socket = socket
    }

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket

    fun readMessage(onResult: (String?) -> Unit) {
        launch(Dispatchers.IO) {
            Thread.sleep(2500)
            while (true){
                if(socket.isConnected){
                    val reader = Scanner(socket.getInputStream().bufferedReader())
                    val line : String
                    if(reader.hasNextLine()){
                        line = reader.nextLine()
                        withContext(Dispatchers.Main){
                            onResult.invoke(line)
                        }
                    }else{
                        withContext(Dispatchers.Main){
                            onResult.invoke(null)
                        }
                        break
                    }
                }else{
                    withContext(Dispatchers.Main) {
                        onResult.invoke(null)
                    }
                }
            }
        }
    }

    fun sendMessage(message: Message, onResult: () -> Unit) {
        launch(Dispatchers.IO) {
            val bw = DataOutputStream(socket.getOutputStream())
            bw.write((Utils.messageClassToJSON(message)+"\n").toByteArray())
            bw.flush()
            withContext(Dispatchers.Main){
                onResult.invoke()
            }
        }
    }
}