package com.example.chatapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.models.Profile
import com.example.chatapp.tictactoe.ServerTicTacToeManager
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
    private lateinit var sock: Socket
    private lateinit var serverSocket: ServerSocket

    @Volatile
    private var id = 0
    private lateinit var password: String

    @Volatile
    private var sockets: HashMap<Int, Socket> = HashMap()

    @Volatile
    private var profiles: ArrayList<Profile> = arrayListOf()
    private var startId = 0

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == STOP_SERVER) {
                serverRunning = false
                stopForeground(true)
                runBlocking {
                    serverSocket.close()
                    mutex.withLock {
                        sockets.forEach {
                            it.value.close()
                        }
                    }
                }
                stopSelf(startId)
                stopSelfResult(startId)
            }
        }
    }

    override fun onCreate() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(receiver, IntentFilter(STOP_SERVER))
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals(START_SERVER)) {
            this.startId = startId
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
            serverSocket = ServerSocket(port)
            serverSocket.reuseAddress = true
            serverRunning = true
            while (true) {
                try {
                    sock = serverSocket.accept()
                    sock.soTimeout = 1500
                    readMessageAndSendToAllSockets(sock)
                    Log.d("service", "accepted new user ${sock.getAddressFromSocket()}")
                } catch (e: java.net.SocketException) {
                }
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
                if (message.type != Message.MessageType.JOIN.code) {
                    if (socket.key != message.id) {
                        bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray(Charsets.UTF_8))
                        bw.flush()
                        Log.d("service", "Sent Message to socket id:  ${socket.key}")
                    }
                } else {
                    bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray(Charsets.UTF_8))
                    bw.flush()
                    Log.d("service", "Sent Message to socket id: ${socket.key}")
                }
            } catch (e: Exception) {
                Log.e("service sendToAll", e.toString())
                removeSocket(socket.value)
            }
        }
    }

    private fun readMessageAndSendToAllSockets(socket: Socket) {
        launch(Dispatchers.IO) {
            while (true) {
                try {
                    val reader = Scanner(socket.getInputStream().bufferedReader())
                    val line: String
                    if (reader.hasNextLine()) {
                        line = reader.nextLine()
                        if (line != "ping") {
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
                        when (id) {
                            3 -> {
                                if (socket.getAddressFromSocket() == Utils.getIpAddress()) { //garante que o dono do server está expulsando alguém.
                                    if (text != "" || text != null) {
                                        if (sockets.containsKey(text?.toInt())) {
                                            sockets[text?.toInt()]?.let {
                                                sendMessageToASocket(
                                                    it,
                                                    this
                                                )
                                                removeSocket(it)
                                            }
                                        } else {
                                        }
                                    } else {
                                    }
                                    Log.e(
                                        "Server Kick Skip",
                                        "A kick has been skipped because command is not from Admin"
                                    )
                                }
                            }
                        }
                    }

                    Message.MessageType.TICINVITE.code -> {
                        if (classMessage.text == null) {
                            // == send inv
                                sockets.get(classMessage.username?.toInt())?.let {
                                        sendMessageToASocket(it, classMessage)
                                }
                        } else { // = accepted or declined, caso declined, tic messages é null
                            val ticMessages = classMessage.ticMessages
                            if (ticMessages != null) {
                                val player1 = ticMessages.player1Id
                                val player2 = ticMessages.player2Id
                                if (player1 != null && player2 != null)
                                    ServerTicTacToeManager.newGame(player1, player2)
                            }
                        }
                    }

                    Message.MessageType.TICPLAY.code -> {
                        val movement = classMessage.text
                        if (movement != null) {

                            val id = classMessage.id

                            if (id != null) {

                                if (ServerTicTacToeManager.searchInMatches(id)) {

                                    if (ServerTicTacToeManager.placeMove(id, movement.toInt())) {


                                    } else {
                                        Log.w("Error:", "This place is already being used")
                                    }
                                } else {
                                    Log.w("Error:", "This match doesn't exists")
                                }
                            }
                        }
                        val message = Message(
                            Message.MessageType.TICPLAY.code,
                            username = classMessage.username,
                            text = classMessage.text,
                            base64Data = null,
                            id = classMessage.id
                        )
                        sendMessageToASocket(socket, message)
                    }
                    else -> {
                        sendMessageToAllSockets(classMessage)
                    }
                }
                delay(1)
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
            var idSocket: Int? = null
            val socketIterator = sockets.entries.iterator()
            mutex.withLock {
                if (sockets.containsValue(socket)) {
                    while (socketIterator.hasNext()) {
                        val socketFromHash = socketIterator.next()
                        if (socketFromHash.value == socket) {
                            idSocket = socketFromHash.key
                        }
                    }
                    if (idSocket != null) {
                        runCatching {
                            launch(Dispatchers.IO) {
                                sockets[id]?.close()
                                Log.d("Remove Socket", "socket id: $id closed")
                            }
                        }
                        sockets.remove(idSocket)
                        Log.d("Remove Socket", "socket id: $id removed from socket list")
                        val iteratorProfile = profiles.iterator()
                        while (iteratorProfile.hasNext()) {
                            val profile = iteratorProfile.next()
                            if (profile.id == idSocket) {
                                idSocket?.let {
                                    notifyWhenProfileDisconnected(
                                        profile.name ?: "",
                                        it
                                    )
                                }
                                    ?: Log.e("server", "error when notify user disconnect")
                                Log.d("service", socket.getAddressFromSocket() + "disconnect")
                                profiles.remove(profile)
                                break
                            }
                        }
                    } else {
                        Log.e("server", "error when remove socket from socket")
                    }
                } else {
                    Log.e("removeSocket", "Error when remove socket because socket is not found in the current list.")
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
    private suspend fun sendMessageToASocket(socket: Socket, message: Message) {
        withContext(Dispatchers.IO) {
//            try {
            val bw = DataOutputStream(socket.getOutputStream())
            bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray(Charsets.UTF_8))
            bw.flush()
            Log.d("service", "Sent message to a socket")
//            }catch (e: Exception){
//                val messageJson = Utils.messageClassToJSON(message)
//                Log.e("error send message:", messageJson)
//                Log.e("error send message:", "error: $e")
//                removeSocket(socket)
//            }
        }
    }

    @Synchronized
    private suspend fun sendIdToSocket(socket: Socket, message: Message) =
        withContext(Dispatchers.IO) {
            id++
            mutex.withLock {
                sockets.put(id, socket)
            }
            notifyProfileConnected(message, id)
            saveProfileOnService(message, id)
            val messageAkl = Message(
                type = Message.MessageType.ACKNOWLEDGE.code,
                username = null,
                text = shareProfilesToNewMember(),
                base64Data = null,
                id = id
            )
            Log.d("service", "Message ACKNOWLEDGE generated to $id")
            sendMessageToASocket(socket, messageAkl)
            Log.d("service", "Sent id to socket")
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
        if (sockets[idSocket]?.getAddressFromSocket().equals(sock.getAddressFromSocket())) {
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
        var serverRunning = false
    }
}