package com.example.chatapp.viewModel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.models.Message
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.*
import java.io.DataOutputStream
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
            while (true) {
                if (socket.isConnected) {
                    val reader = Scanner(socket.getInputStream().bufferedReader())
                    val line: String
                    if (reader.hasNextLine()) {
                        line = reader.nextLine()
                        withContext(Dispatchers.Main) {
                            if (MainApplication.applicationIsInBackground()) {
                                val message = Utils.jsonToMessageClass(line)
                                backgroundMessages.add(message)
                                if(message.typeMessage == Message.RECEIVED_MESSAGE_VOICE || message.typeMessage == Message.SENT_MESSAGE_VOICE){
                                    Utils.createNotification(message.name, "Received audio message")
                                }else{
                                    Utils.createNotification(message.name, message.message)
                                }
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

    @Synchronized
    fun sendMessageToSocket(message: Message, onResult: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val bw = DataOutputStream(socket.getOutputStream())
            bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray())
            bw.flush()
            withContext(Dispatchers.Main) {
                Log.e("server", "Sent Message")
                onResult.invoke()
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

    fun emptyBackgroundMessages() {
        backgroundMessages = arrayListOf()
    }

    fun getIpHost(): String{
        return Utils.getAddressFromSocket(socket)
    }

    fun getIpPort(): String{
        return Utils.getPortFromSocket(socket)
    }

}