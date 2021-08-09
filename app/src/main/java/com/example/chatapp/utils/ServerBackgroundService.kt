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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*

class ServerBackgroundService : Service() {
    private var port: Int = 0
    private val startserver = "com.example.startserver"
    private var socket: ArrayList<Socket> = arrayListOf()

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
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle("Server open")
            setContentText("Server was opened ip: ${Utils.getIpAndress()}:$port")
            setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            setAutoCancel(true)
        }
        startForeground(
            notificationId, builder.build()
        )
        readMessageAndSendToAllSockets()
        serverConnecting(port)
    }

    private fun serverConnecting(port: Int) {
        GlobalScope.launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            while (true) {
                socket.add(serverSocket.accept())
                Log.e("service", "accepted new user")
            }
        }
    }

    @Synchronized
    private fun sendMessage(message: Message) {
        socket.forEach {
            val bw = DataOutputStream(it.getOutputStream())
            bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray())
            bw.flush()
            Log.e("service", "Sent Message")
        }
    }

    private fun readMessageAndSendToAllSockets() {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                socket.forEach {
                    Log.e("service", "readingMessage")
                    val reader = Scanner(it.getInputStream().bufferedReader())
                    val line: String
                    if (reader.hasNextLine()) {
                        line = reader.nextLine()
                        val message = Utils.JSONtoMessageClass(line)
                        sendMessage(message)
                    }
                }
            }
        }
    }

    private fun verifyIfMemberLeaves() {
        var oldSocket = arrayListOf<Socket>()
        oldSocket.addAll(socket)
        GlobalScope.launch(Dispatchers.Default) {
            while (true) {
                if (oldSocket != socket) {
                    val membersLeaves = arrayListOf<Socket>()
                    oldSocket.forEach {
                        if (!socket.contains(it)) { //verifica membros diferentes do antigo array de socket guardado em memória
                            membersLeaves.add(it)
                        }
                    }
                    membersLeaves.forEach {
                        val message = Message(
                            "",
                            "${it.localSocketAddress}:was disconnected",
                            typeMesage = Message.NOTIFY_CHAT
                        )
                        sendMessage(message)
                    }
                    oldSocket = arrayListOf()
                    oldSocket.addAll(socket)
                }
                delay(250)
            }
        }
    }
}