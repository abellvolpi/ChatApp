package com.example.chatapp.viewModel


import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.utils.Extensions.getAddressFromSocket
import com.example.chatapp.utils.Extensions.getPortFromSocket
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

class ConnectionFactory : CoroutineScope, ViewModel() {
    private var isFirstAccessInThisFragment = true
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    var line: MutableLiveData<Pair<Message, String>> = MutableLiveData()
    var lastLine: String = ""
    private var backgroundMessages = arrayListOf<Message>()
    var serverOnline: MutableLiveData<Boolean> = MutableLiveData()

    fun setFirstAccessChatFragment(boolean: Boolean){
        isFirstAccessInThisFragment = boolean
    }

    fun isFirstAccessInThisFragment(): Boolean{
        return isFirstAccessInThisFragment
    }

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
                            if(line == "ping"){
                                Log.d("ConnectionFactory", "received ping from server")
                            }else {
                                val message = Utils.jsonToMessageClass(line)
                                if (MainApplication.applicationIsInBackground()) {
                                    backgroundMessages.add(message)
                                    when (message.type) {
                                        Message.MessageType.AUDIO.code -> {
                                            Utils.createNotification(
                                                message.username ?: "Error user name",
                                                context.getString(R.string.received_audio)
                                            )
                                        }
                                        Message.MessageType.IMAGE.code -> {
                                            Utils.createNotification(
                                                message.username ?: "Error user name",
                                                context.getString(R.string.received_photo)
                                            )
                                        }
                                        Message.MessageType.JOIN.code -> {
                                            Utils.createNotification(
                                                message.username ?: "Error user name",
                                                context.getString(R.string.joined_chat)
                                            )
                                        }
                                        Message.MessageType.LEAVE.code -> {
                                            Utils.createNotification(
                                                message.username ?: "Error user name",
                                                context.getString(R.string.left_the_chat)
                                            )
                                        }
                                        else -> {
                                            if (message.username != ProfileSharedProfile.getProfile()) {
                                                Utils.createReplyableNotification(
                                                    message.username ?: "Error user name",
                                                    message.text ?: "Error text"
                                                )
                                            } else return@withContext
                                        }
                                    }
                                    Utils.playBemTeVi()
                                } else {
                                    this@ConnectionFactory.line.postValue(Pair(message, line))
                                }
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            this@ConnectionFactory.line.postValue(null)
                            this@ConnectionFactory.line = MutableLiveData()
                        }
                        break
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        this@ConnectionFactory.line.postValue(null)
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
            bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray(Charsets.UTF_8))
            bw.flush()
            withContext(Dispatchers.Main) {
                Log.d("server", "Sent Message")
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

    private fun observerWhenSocketClose() = launch(Dispatchers.IO) {
        while (true) {
            if (socket.isConnected) {
                try {
                       socket.getOutputStream().bufferedWriter(Charsets.UTF_8).apply {
                            write("ping\n")
                            flush()
                       }
                } catch (e: Exception) {
                    serverOnline.postValue(false)
                    Log.e("ConnectionFactory",e.toString())
                    break
                }
            }
            delay(2000)
        }
    }
    fun closeSocket(){
        socket.close()
    }
}