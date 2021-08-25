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
import com.example.chatapp.utils.Extensions.toSHA256
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
    private lateinit var sock : Socket

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
            stopSelfResult(startId)
            return START_NOT_STICKY
        }
        if (intent?.action.equals(SEND_REPLY)) {
            val message = intent?.getSerializableExtra("message") as Message
            launch(Dispatchers.IO) {
                sendMessageToAllSockets(message)
            }
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
                sock = serverSocket.accept()
                sock.soTimeout = 1500
                readMessageAndSendToAllSockets(sock)
                Log.d("service", "accepted new user ${sock.getAddressFromSocket()}")
            }
        }
    }

    @Synchronized
    private suspend fun sendMessageToAllSockets(message: Message) = withContext(Dispatchers.IO) {
        val socketIterator = sockets.entries.iterator()
        while (socketIterator.hasNext()) {
            val socket = socketIterator.next()
            try {
                val bw = DataOutputStream(socket.value.getOutputStream())
                bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray(Charsets.UTF_8))
                bw.flush()
                Log.d("service", "Sent Message")
            }catch (e:Exception){
                Log.e("service sendToAll", e.toString())
                removeSocket(socket.value)
                socketIterator.remove()
            }
        }
    }

    @Synchronized
    private fun readMessageAndSendToAllSockets(socket: Socket) {
        launch(Dispatchers.IO) {
            while (true) {
                Log.d("service", "readingMessage")
                try {
                    val reader = Scanner(socket.getInputStream().bufferedReader())
                    val line: String
                    if (reader.hasNextLine()) {
                        line = reader.nextLine()
                        if (line == "ping") {
                        } else {
                            val classMessage = Utils.jsonToMessageClass(line)
                            treatMessage(classMessage, socket)
                        }
                    }
                    delay(1)
                } catch (e: Exception) {
                    Log.e("Error Read Socket Line", e.toString())
                    removeSocket(socket)
                    break
                }
            }
        }
    }

    @Synchronized
    private suspend fun treatMessage(classMessage: Message, socket: Socket) =
        withContext(Dispatchers.IO) {
            with(classMessage) {
                when (type) {
                    Message.MessageType.JOIN.code -> {
                        if (password != "".toSHA256()) {
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
                    }
                    Message.MessageType.REVOKED.code -> {
                        if (text != "" || text != null) {
                            if (sockets.containsKey(text?.toInt())) {
                                sockets[text?.toInt()]?.let { sendMessageToASocket(it, this) }
                            } else {
                            }
                        } else {
                        }
                    }
                    else -> {
                        sendMessageToAllSockets(classMessage)
                    }
                }
            }
        }

    @Synchronized
    private fun observerWhenSocketClose(socket: Socket) {
        launch(Dispatchers.IO) {
            while (true) {
                if (socket.isConnected) {
                    try {
                        socket.getOutputStream().bufferedWriter(Charsets.UTF_8).apply {
                            write("ping\n")
                            flush()
                        }
                    } catch (e: Exception) {
                        Log.e("service", "observer when disconnected triggered")
                        removeSocket(socket)
                        break
                    }
                }
                delay(2000)
            }
        }
    }

    @Synchronized
    private suspend fun removeSocket(socket: Socket) {
        withContext(Dispatchers.Default) {
            if (sockets.containsValue(socket)) {
                var idSocket: Int? = null
                val socketIterator = sockets.entries.iterator()
                mutex.withLock {
                    while(socketIterator.hasNext()) {
                        val socketFromHash = socketIterator.next()
                        if (socketFromHash.value == socket) {
                            idSocket = socketFromHash.key
                        }
                    }
                    if (idSocket != null) {
                        withContext(Dispatchers.IO) {
                            sockets[id]?.close()
                        }
                        sockets.remove(idSocket)
                        profiles.forEach {profile ->
                            if (profile.id == idSocket) {
                                idSocket?.let { notifyWhenProfileDisconnected(profile.name, it) }
                                    ?: Log.e("server", "error when notify user disconnect")
                                Log.d("service", socket.getAddressFromSocket() + "disconnect")
                                profiles.remove(profile)
                                return@forEach
                            }
                        }
                    } else {
                        Log.e("server", "error when remove socket from socket")
                    }
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
    private suspend fun sendMessageToASocket(socket: Socket, message: Message) =
        withContext(Dispatchers.IO) {
            val bw = DataOutputStream(socket.getOutputStream())
            bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray(Charsets.UTF_8))
            bw.flush()
            Log.d("service", "Sent message to a socket")
        }

    @Synchronized
    private suspend fun sendIdToSocket(socket: Socket, message: Message) =
        withContext(Dispatchers.IO) {
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
            saveProfileOnService(message, id)
            notifyProfileConnected(message, id)
        }

    @Synchronized
    private suspend fun notifyProfileConnected(message: Message, idProfile: Int) {
        val join = Message.Join(message.join?.avatar ?: "", "", false)
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

    @Synchronized
    private suspend fun notifyWhenProfileDisconnected(username: String, idProfile: Int) {
        val messageToSend = Message(
            id = idProfile,
            type = Message.MessageType.LEAVE.code,
            username = username,
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
            0, null, false
        )
        if(sockets[idSocket]?.getAddressFromSocket().equals(sock.getAddressFromSocket())){
            profile.isAdmin = true
            profiles.add(profile)
            return
        }
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