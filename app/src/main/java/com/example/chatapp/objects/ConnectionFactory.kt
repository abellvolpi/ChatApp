package com.example.chatapp.objects


import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import com.example.chatapp.models.Message
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.Utils
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.io.Serializable
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

class ConnectionFactory() : CoroutineScope, Serializable, Parcelable{

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private lateinit var socket: Socket
    private var externalScope = MainApplication.getCoroutineScope()

    constructor(parcel: Parcel) : this() {

    }

    fun readMessage(onResult: (String?) -> Unit) {
        launch(Dispatchers.IO) {
            withContext(externalScope.coroutineContext) {
                Thread.sleep(2500)
                while (true) {
                    if (socket.isConnected) {
                        val reader = Scanner(socket.getInputStream().bufferedReader())
                        val line: String
                        if (reader.hasNextLine()) {
                            line = reader.nextLine()
                            withContext(Dispatchers.Main) {
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
    }

    fun sendMessage(message: Message, onResult: () -> Unit) {
        launch(Dispatchers.IO) {
            val bw = DataOutputStream(socket.getOutputStream())
            bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray())
            bw.flush()
            withContext(Dispatchers.Main) {
                onResult.invoke()
            }
        }
    }

    fun serverConnecting(port: Int, context: Context, onResult: () -> Unit) {
        launch(Dispatchers.IO) {
            withContext(externalScope.coroutineContext) {
                val serverSocket = ServerSocket(port)
                while (true){
                    socket = serverSocket.accept()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Conexão Estabelecida", Toast.LENGTH_SHORT).show()
                        onResult.invoke()
                    }
                }
            }
        }
    }

    fun setSocket(socket: Socket){
        this.socket = socket
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ConnectionFactory> {
        override fun createFromParcel(parcel: Parcel): ConnectionFactory {
            return ConnectionFactory(parcel)
        }

        override fun newArray(size: Int): Array<ConnectionFactory?> {
            return arrayOfNulls(size)
        }
    }
}