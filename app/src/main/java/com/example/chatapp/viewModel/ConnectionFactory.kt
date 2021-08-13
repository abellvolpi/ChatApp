package com.example.chatapp.viewModel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.getAddressFromSocket
import com.example.chatapp.utils.Utils.getPortFromSocket
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
    var serverOnline: MutableLiveData<Boolean> = MutableLiveData()

    private fun readMessage() {
        val context = MainApplication.getContextInstance()
        observerWhenSocketClose()
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
                                when (message.type) {
                                    Message.MessageType.AUDIO.code -> {
                                        Utils.createNotification(message.username ?: "Error user name", context.getString(R.string.received_audio))
                                    }
                                    Message.MessageType.IMAGE.code -> {
                                        Utils.createNotification(message.username ?: "Error user name", context.getString(R.string.received_photo))
                                    }
                                    Message.MessageType.JOIN.code -> {
                                        Utils.createNotification(message.username ?: "Error user name", context.getString(R.string.joined_chat))
                                    }
                                    Message.MessageType.LEAVE.code -> {
                                        Utils.createNotification(message.username ?: "Error user name", context.getString(R.string.left_the_chat))
                                    }
                                    else -> {
                                        Utils.createReplyableNotification(message.username ?: "Error user name", message.text ?: "Error text")
                                    }
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

    fun getIpHost(): String {
        return socket.getAddressFromSocket()
    }

    fun getIpPort(): String {
        return socket.getPortFromSocket()
    }

    private fun observerWhenSocketClose() {
        launch(Dispatchers.IO) {
            while (true) {
                if (socket.isConnected) {
                    try {
                        if (socket.getInputStream().read() == -1) {
                            Log.e("connection factory", "Server has disconnected")
                            serverOnline.postValue(false)
                            break
                        }
                    } catch (e: Exception) {
                        serverOnline.postValue(false)
                        break
                    }
                }
                delay(1)
            }
        }
    }
}