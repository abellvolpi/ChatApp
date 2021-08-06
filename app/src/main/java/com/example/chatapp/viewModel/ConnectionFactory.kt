package com.example.chatapp.viewModel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.models.Message
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.*
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

class ConnectionFactory : CoroutineScope, ViewModel() {


    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    var line: MutableLiveData<String> = MutableLiveData()
    private var backgroundMessages = arrayListOf<Message>()

    private fun readMessage() {
        GlobalScope.launch(Dispatchers.IO) {
            delay(2000)
            while (true) {
                if (socket.isConnected) {
                    val reader = Scanner(socket.getInputStream().bufferedReader())
                    val line: String
                    if (reader.hasNextLine()) {
                        line = reader.nextLine()
                        withContext(Dispatchers.Main) {
                            if (MainApplication.aplicationIsInBackground()) {
                                val message = Utils.JSONtoMessageClass(line)
                                backgroundMessages.add(message)
                                Utils.createNotification(message.name, message.message)
                                Utils.playBemTeVi()
                            } else {
                                this@ConnectionFactory.line.postValue(line)
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            this@ConnectionFactory.line.postValue("error")
                            this@ConnectionFactory.line = MutableLiveData()
                        }
                        break
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        this@ConnectionFactory.line.postValue("error")
                        this@ConnectionFactory.line = MutableLiveData()
                    }
                }
            }
        }
    }

    fun setSocket(socket: Socket) {
        this.socket = socket
    }

    fun startListenerMessages() {
        readMessage()
    }

    fun getBackgroundMessages(): ArrayList<Message> {
        return backgroundMessages
    }

    fun empyBackgroundMessages() {
        backgroundMessages = arrayListOf()
    }

    fun closeSocket() {
        socket.close()
    }

}