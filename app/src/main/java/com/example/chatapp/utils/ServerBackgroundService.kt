package com.example.chatapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.models.Profile
import com.example.chatapp.ui.MainActivity
import com.example.chatapp.utils.Utils.getAddressFromSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.collections.HashMap
import kotlin.coroutines.CoroutineContext

class ServerBackgroundService : Service(), CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.Main
    private var port: Int = 0
    private val mutex = Mutex()

    @Volatile
    private var id = 0
    private lateinit var password: String

    @Volatile
    private var sockets: HashMap<Int, Socket> = HashMap()

    @Volatile
    private var profiles: ArrayList<Profile> = arrayListOf()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action.equals(START_SERVER)) {
            port = intent?.getIntExtra("socketConfigs", 0) ?: 0
            password = intent?.getStringExtra("password") ?: ""
            start()
            return START_NOT_STICKY
        }

        if (intent?.action.equals(STOP_SERVER)) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {

        val context = MainApplication.getContextInstance()
        val notificationId = 1005
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, "Server open", importance).apply {
                description = "Server was opened"
            }
            val notificationManager: NotificationManager =
                MainApplication.getContextInstance().getSystemService(
                    Context.NOTIFICATION_SERVICE
                ) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val broadCastIntent = Intent(context, ReceiverMessageBroadCast::class.java).apply {
            putExtra("finishConnection", true)
        }
        val actionIntent = PendingIntent.getBroadcast(
            context,
            0,
            broadCastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val intent = Intent(context, MainActivity::class.java)
        val builder = NotificationCompat.Builder(context, Companion.CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle(getString(R.string.server_opened))
            setContentText(getString(R.string.server_configs, Utils.getIpAddress(), port))
            setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            setAutoCancel(true)
            addAction(R.mipmap.ic_launcher, getString(R.string.stop), actionIntent)
        }
        startForeground(
            notificationId, builder.build()
        )
        serverConnecting(port)
    }

    private fun serverConnecting(port: Int) {
        launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            while (true) {
                val sock = serverSocket.accept()
                readMessageAndSendToAllSockets(sock)
                Log.e("service", "accepted new user ${sock.getAddressFromSocket()}")
            }
        }
    }

    @Synchronized
    private fun sendMessageToAllSockets(message: Message) {
        launch(Dispatchers.IO) {
            sockets.forEach {
                val bw = DataOutputStream(it.value.getOutputStream())
                bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray())
                bw.flush()
                Log.e("service", "Sent Message")
            }
        }
    }

    private fun readMessageAndSendToAllSockets(socket: Socket) {
        launch(Dispatchers.IO) {
            while (true) {
                Log.e("service", "readingMessage")
                val reader = Scanner(socket.getInputStream().bufferedReader())
                val line: String
                if (reader.hasNextLine()) {
                    line = reader.nextLine()
                    val classMessage = Utils.jsonToMessageClass(line)
                    if (classMessage.type == Message.MessageType.JOIN.code) {
                        if (password != "") {
                            if (classMessage.join?.password == password) {
                                sendIdToSocket(socket) {
                                    saveProfile(classMessage, it)
                                    notifyProfileConnected(classMessage, it)
                                }
                            } else {
                                val message = Message(
                                    Message.MessageType.REVOKED.code,
                                    username = null,
                                    text = null,
                                    base64Data = null,
                                    id = 1
                                )
                                sendMessageToASocket(socket, message)
                            }
                        } else {
                            sendIdToSocket(socket) {
                                saveProfile(classMessage, it)
                                notifyProfileConnected(classMessage, it)
                            }
                        }
                    } else {
                        sendMessageToAllSockets(classMessage)
                    }
                }
                delay(1)
            }
        }
    }

    private fun observerWhenSocketClose(socket: Socket) {
        launch(Dispatchers.IO) {
            while (true) {
                if (socket.isConnected) {
                    try {
                        if (socket.getInputStream().read() == -1) {
                            Log.e("service", "observer when disconnected triggered")
                            removeSocket(socket)
                            break
                        }
                    } catch (e: Exception) {
                        break
                    }
                }
                delay(1)
            }
        }
    }

    private fun removeSocket(socket: Socket) {
        launch(Dispatchers.Default) {
            var idSocket: Int? = null
            sockets.forEach {
                if (it.value == socket) {
                    idSocket = it.key
                }
            }
            mutex.withLock {
                if (idSocket != null) {
                    sockets.remove(idSocket)
                } else {
                    Log.e("server", "error when remove socket from socket")
                }
            }
            val message = Message(
                Message.MessageType.LEAVE.code,
                text = null,
                id = idSocket,
                base64Data = null,
                username = null
            )
            Log.e("service", socket.getAddressFromSocket() + "disconnect")
            idSocket?.let { notifyWhenProfileDisconnected(message, it) }
                ?: Log.e("server", "error when notify user disconnect")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel("teste")
        stopSelf()
        sockets.forEach {
            it.value.close()
        }
    }

    private suspend fun sendMessageToASocket(socket: Socket, message: Message) =
        withContext(Dispatchers.IO) {
            launch(Dispatchers.IO) {
                val bw = DataOutputStream(socket.getOutputStream())
                bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray())
                bw.flush()
                Log.e("service", "Sent id to socket")
            }
        }

    private fun sendIdToSocket(socket: Socket, onResume: (Int) -> Unit) {
        launch(Dispatchers.Default) {
            id++
            observerWhenSocketClose(socket)
            sockets[id] = socket
            val message = Message(
                Message.MessageType.ACKNOWLEDGE.code,
                username = null,
                text = null,
                base64Data = null,
                id = id
            )
            sendMessageToASocket(socket, message)
            Log.e("service", "Sent id to socket")
            withContext(Dispatchers.Main) {
                onResume.invoke(id)
            }
        }
    }

    private fun notifyProfileConnected(message: Message, idProfile: Int) {
        val join = Message.Join(message.join?.avatar ?: "", "")
        val messageToSend = Message(
            id = idProfile,
            type = Message.MessageType.JOIN.code,
            username = message.username,
            join = join,
            text = null,
            base64Data = null
        )
        sendMessageToAllSockets(messageToSend)
    }

    private fun notifyWhenProfileDisconnected(message: Message, idProfile: Int){
        val messageToSend = Message(id = idProfile, type = Message.MessageType.LEAVE.code, username = message.username, join = null, base64Data = null, text = null)
        sendMessageToAllSockets(messageToSend)
    }

    private fun saveProfile(messageJoin: Message, idSocket: Int) {
//        val context = MainApplication.getContextInstance()
//        val output = File(context.cacheDir.absolutePath+"/photosProfile","profilePhoto_${idSocket}.jpg")
//        val base64 = Base64.decode(messageJoin.join?.avatar ?: "", Base64.NO_WRAP)
//        launch(Dispatchers.IO) {
//            output.parentFile.mkdirs()
//            val fos = FileOutputStream(output)
//            fos.write(base64)
//            fos.flush()
//            fos.close()
        val profile = Profile(
            idSocket,
            messageJoin.username ?: "name error",
            messageJoin.join?.avatar ?: "",
            0
        )
//            //salvar no banco de dados
//            profile.photoProfile = messageJoin.join?.avatar ?: ""
        profiles.add(profile)
    }

    companion object {
        const val START_SERVER = "com.example.START_SERVER"
        const val STOP_SERVER = "com.example.STOP_SERVER"
        private const val CHANNEL_ID = "server_connection_channel_id"
    }
}