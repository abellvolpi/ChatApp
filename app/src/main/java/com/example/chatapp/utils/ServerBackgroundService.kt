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
import com.example.chatapp.utils.Extensions.getAddressFromSocket
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
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
        if (intent?.action.equals(SEND_REPLY)){
            val message = intent?.getSerializableExtra("message") as Message
            sendMessageToAllSockets(message)
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
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
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
                Log.d("service", "accepted new user ${sock.getAddressFromSocket()}")
            }
        }
    }

    @Synchronized
    private suspend fun sendMessageToAllSockets(message: Message) = withContext(Dispatchers.IO){
            sockets.forEach {
                val bw = DataOutputStream(it.value.getOutputStream())
                bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray(Charsets.UTF_8))
                bw.flush()
                Log.d("service", "Sent Message")
        }
    }

    private fun readMessageAndSendToAllSockets(socket: Socket) {
        launch(Dispatchers.IO) {
            while (true) {
                Log.d("service", "readingMessage")
                val reader = Scanner(socket.getInputStream().bufferedReader())
                val line: String
                if (reader.hasNextLine()) {
                    line = reader.nextLine()
                    val classMessage = Utils.jsonToMessageClass(line)
                    if (classMessage.type == Message.MessageType.JOIN.code) {
                        if (password != "") {
                            if (classMessage.join?.password == password) {
                                sendIdToSocket(socket, classMessage)
                                observerWhenSocketClose(socket)
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
                            sendIdToSocket(socket, classMessage)
                            observerWhenSocketClose(socket)
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
                        if (socket.getInputStream().available() == -1) {
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
            var message: Message
            mutex.withLock {
                sockets.forEach {
                    if (it.value == socket) {
                        idSocket = it.key
                    }
                }
                if (idSocket != null) {
                    sockets.remove(idSocket)
                    profiles.forEach {
                        if (it.id == idSocket) {
                            message = Message(
                                Message.MessageType.LEAVE.code,
                                text = null,
                                id = idSocket,
                                base64Data = null,
                                username = it.name
                            )
                            idSocket?.let { notifyWhenProfileDisconnected(message, it) }
                                ?: Log.e("server", "error when notify user disconnect")
                            Log.d("service", socket.getAddressFromSocket() + "disconnect")
                            profiles.remove(it)
                            return@forEach
                        }
                    }
                } else {
                    Log.e("server", "error when remove socket from socket")
                }
            }
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

    @Synchronized
    private suspend fun sendMessageToASocket(socket: Socket, message: Message) = withContext(Dispatchers.IO) {
            val bw = DataOutputStream(socket.getOutputStream())
            bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray(Charsets.UTF_8))
            bw.flush()
            Log.d("service", "Sent message to a socket")
    }

    @Synchronized
    private suspend fun sendIdToSocket(socket: Socket, message: Message) = withContext(Dispatchers.IO){
            id++
            sockets[id] = socket
            val messageAkl = Message(
                type = Message.MessageType.ACKNOWLEDGE.code,
                username = null,
                text = shareProfilesToNewMember(),
                base64Data = null,
                id = id
            )
            sendMessageToASocket(socket, messageAkl)
            Log.d("service", "Sent id to socket")
            notifyProfileConnected(message, id)
            saveProfileOnService(message, id)
    }

    private suspend fun notifyProfileConnected(message: Message, idProfile: Int) {
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

    private suspend fun notifyWhenProfileDisconnected(message: Message, idProfile: Int) {
        val messageToSend = Message(
            id = idProfile,
            type = Message.MessageType.LEAVE.code,
            username = message.username,
            join = null,
            base64Data = null,
            text = null
        )
        sendMessageToAllSockets(messageToSend)
    }

    private fun saveProfileOnService(messageJoin: Message, idSocket: Int) {
        val profile = Profile(
            idSocket,
            messageJoin.username ?: "name error",
            messageJoin.join?.avatar ?: "",
            0
        )
        profiles.add(profile)
    }

    private fun shareProfilesToNewMember(): String {
        val listType = Types.newParameterizedType(List::class.java, Profile::class.java)
        val adapter: JsonAdapter<List<Profile>> =
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(listType)
        return adapter.toJson(profiles.toList())
    }

    companion object {
        const val START_SERVER = "com.example.START_SERVER"
        const val STOP_SERVER = "com.example.STOP_SERVER"
        private const val CHANNEL_ID = "server_connection_channel_id"
        const val SEND_REPLY = "send_reply"
    }
}