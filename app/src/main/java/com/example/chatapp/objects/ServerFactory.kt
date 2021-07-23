package com.example.chatapp.objects

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ServerFactory : CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    lateinit var socket: Socket

    fun serverConnecting(context: Context, port: Int) {
        launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            socket = serverSocket.accept()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Conexão Estabelecida", Toast.LENGTH_SHORT).show()

            }

            val br = BufferedReader(InputStreamReader(socket.getInputStream()))
            var scanner = Scanner(socket.getInputStream())

            var msg = ""
            var msg2 = ""

            while (msg!="sair") {

                if (br.ready()){
                    msg2 = br.readLine()
                }

                msg = scanner.nextLine()

                scanner.close()
                br.close()

                Log.e("OutPutMessage2", msg)
                Log.e("OutPutMessage2", msg2)


            }
        }

    }

    fun closeConnection() {
        socket.close()
    }

    fun readMessages() {

    }


}