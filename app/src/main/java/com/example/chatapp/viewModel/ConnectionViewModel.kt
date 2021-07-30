package com.example.chatapp.viewModel


import androidx.lifecycle.ViewModel
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.*
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

class ConnectionViewModel : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()+Dispatchers.Main
    private lateinit var socket : Socket


    private fun readMessage(onResult: (String?) -> Unit){
        GlobalScope.launch(Dispatchers.IO) {
            delay(2000)
            while (true) {
                if (socket.isConnected) {
                    val reader = Scanner(socket.getInputStream().bufferedReader())
                    val line: String
                    if (reader.hasNextLine()) {
                        line = reader.nextLine()
                        withContext(Dispatchers.Main) {
                            if(MainApplication.aplicationIsInBackground()){
                                Utils.createNotification("New Message", line)
                            }
                            onResult.invoke(line)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            onResult.invoke(null)
                        }
                        break
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult.invoke(null)
                    }
                }
            }
        }
    }

    fun setSocket(socket: Socket){
        this.socket = socket
    }


}