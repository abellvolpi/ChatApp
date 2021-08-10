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
    private val job = Job()
    override val coroutineContext: CoroutineContext = job + Dispatchers.Main
    private var port: Int = 0
    private val startServer = "com.example.startserver"
    private val stopServer = "com.example.stopserver"

    @Volatile
    private var sockets: ArrayList<Socket> = arrayListOf()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action.equals(startServer)) {
            val arguments = intent?.getIntExtra("socketConfigs", 0)
            port = arguments ?: 0
            start()
            return START_NOT_STICKY
        }

        if (intent?.action.equals(stopServer)) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
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
            setContentTitle(getString(R.string.server_open))
            setContentText(getString(R.string.server_configs, Utils.getipAddress(), port))
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
//        readMessageAndSendToAllSockets()
        serverConnecting(port)
    }

    private fun serverConnecting(port: Int) {
        launch(Dispatchers.IO) {
            val serverSocket = ServerSocket(port)
            while (true) {
                val sock = serverSocket.accept()
                sock.soTimeout = 5000
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
        launch(Dispatchers.IO) {
            while (true) {
                try {
                    Log.e("service", "readingMessage")
                    val reader = Scanner(socket.getInputStream().bufferedReader())
                    val line: String
                    if (reader.hasNextLine()) {
                        line = reader.nextLine()
                        val message = Utils.JSONtoMessageClass(line)
                        sendMessage(message)

                    }
                    delay(1)
                } catch (e: Exception) {
                    sockets.remove(socket)
                    break
                }
            }
        }
    }

    private fun verifyIfMemberLeaves() {
        var oldSocket = arrayListOf<Socket>()
        oldSocket.addAll(sockets)
        launch(Dispatchers.Default) {
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
                            getString(R.string.player_disconnected, it.localSocketAddress),
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel("teste")
        stopSelf()
        sockets.forEach{
            it.close()
        }
    }

//    override fun stopService(name: Intent?): Boolean {
//        job.cancel()
//        return super.stopService(name)
//    }
}