package com.example.chatapp.objects

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Proxy
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

class ServerFactory(var context: Context, var port: Int) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket

    fun serverConnecting() {
        launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            socket = serverSocket.accept()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Conexão Estabelecida", Toast.LENGTH_SHORT).show()
            }

            val br = BufferedReader(InputStreamReader(socket.getInputStream()))
            val scanner = Scanner(socket.getInputStream())

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
//        launch(Dispatchers.IO) {
//            val serverSocket = ServerSocket(port)
//            socket = serverSocket.accept()
//            socket.tcpNoDelay = true
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Conexão Estabelecida", Toast.LENGTH_SHORT).show()
//            }
//        }
    }



    fun closeConnection() {
        socket.close()
    }


}