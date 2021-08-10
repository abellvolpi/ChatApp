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
import com.example.chatapp.ui.MainActivity
import kotlinx.coroutines.*
import java.io.DataOutputStream
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

class ServerBackgroundService : Service(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private var port: Int = 0
    private val startserver = "com.example.startserver"
    private val stopserver = "com.example.stopserver"

    @Volatile
    private var sockets: ArrayList<Socket> = arrayListOf()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals(startserver)) {
            val arguments = intent?.getIntExtra("socketConfigs", 0)
            port = arguments ?: 0
            start()
            return START_STICKY
        }
        if (intent?.action.equals(stopserver)) {
            stopForeground(true)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val context = MainApplication.getContextInstance()
        val notificationId = 1005
        val CHANNEL_ID = "server_connection_channel_id"
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
        val intent = Intent(context, MainActivity::class.java)
        val broadCastIntent = Intent(context, ReceiverMessageBroadCast::class.java).apply {
            putExtra("finishConnection", true)
        }
        val actionIntent = PendingIntent.getBroadcast(
            context,
            0,
            broadCastIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle("Server open")
            setContentText("Server was opened ip: ${Utils.getipAddress()}:$port")
            setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            setAutoCancel(true)
            addAction(R.mipmap.ic_launcher, "Stop", actionIntent)
        }
        startForeground(
            notificationId, builder.build()
        )
//        readMessageAndSendToAllSockets()
        serverConnecting(port)
    }

    private fun serverConnecting(port: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            while (true) {
                val sock = serverSocket.accept()
                sock.soTimeout = 2000
                readMessageAndSendToAllSockets(sock)
                sockets.add(sock)
                Log.e("service", "accepted new user")
            }
        }
    }

    @Synchronized
    private fun sendMessage(message: Message) {
        sockets.forEach {
            val bw = DataOutputStream(it.getOutputStream())
            bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray())
            bw.flush()
            Log.e("service", "Sent Message")
        }
    }

    private fun readMessageAndSendToAllSockets(socket: Socket) {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                try{
                    Log.e("service", "readingMessage")
                    val reader = Scanner(socket.getInputStream().bufferedReader())
                    val line: String
                    if (reader.hasNextLine()) {
                        line = reader.nextLine()
                        val message = Utils.JSONtoMessageClass(line)
                        sendMessage(message)
                    }
                }catch (e: Exception){
                    sockets.remove(socket)
                }

            }
        }
    }

    private fun verifyIfMemberLeaves() {
        var oldSocket = arrayListOf<Socket>()
        oldSocket.addAll(sockets)
        GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                if (oldSocket != sockets) {
                    val membersLeaves = arrayListOf<Socket>()
                    oldSocket.forEach {
                        if (!sockets.contains(it)) { //verifica membros diferentes do antigo array de socket guardado em memória
                            membersLeaves.add(it)
                        }
                    }
                    membersLeaves.forEach {
                        val message = Message(
                            "",
                            "${it.localSocketAddress}:was disconnected",
                            typeMessage = Message.NOTIFY_CHAT
                        )
                        sendMessage(message)
                    }
                    oldSocket = arrayListOf()
                    oldSocket.addAll(sockets)
                }
                delay(250)
            }
        }
    }
}