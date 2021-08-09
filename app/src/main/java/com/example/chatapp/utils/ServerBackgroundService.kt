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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.ui.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList

class ServerBackgroundService : Service() {
    private var port: Int = 0
    private val startserver = "com.example.startserver"
    private val stopserver = "com.example.stopserver"
    private val recentMessage = "com.example.message"
    private var socket: ArrayList<Socket> = arrayListOf()
    private var isAnybodyOnline = false
    private val localBroadCastManager = LocalBroadcastManager.getInstance(MainApplication.getContextInstance())
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
        if(intent?.action.equals(stopserver)){
            stopForeground(true)

        }
        if (intent?.action.equals(recentMessage)) {
            val message = intent?.getSerializableExtra("message") as Message
            sendMessage(message) {}
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
        val broadCastIntent = Intent(context,ReceiverMessageBroadCast::class.java).apply{
            putExtra("finishConnection",true)
        }
        val actionIntent = PendingIntent.getBroadcast(context,0,broadCastIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle("Server open")
            setContentText("Server was opened")
            setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            setAutoCancel(true)
            addAction(R.mipmap.ic_launcher,"Stop",actionIntent)
        }
        startForeground(
            notificationId, builder.build()
        )
        serverConnecting(port) {}
    }

    private fun serverConnecting(port: Int, onResult: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
//            try {
            socket.add(serverSocket.accept())
            Log.e("connection", "accepted new user")
            withContext(Dispatchers.Main) {
                if (!isAnybodyOnline) {
                    isAnybodyOnline = true
                }
                onResult.invoke()
            }
//            } catch (e: Exception) {
//                Log.e("Error connection", e.toString())
//            }
        }
    }

    @Synchronized
    private fun sendMessage(message: Message, onResult: () -> Unit) {
        GlobalScope.launch(Dispatchers.IO) {
            socket.forEach {
                val bw = DataOutputStream(it.getOutputStream())
                bw.write((Utils.messageClassToJSON(message) + "\n").toByteArray())
                bw.flush()
                withContext(Dispatchers.Main) {
                    Log.e("server", "Sent Message")
                    onResult.invoke()
                }
            }
        }
    }




}