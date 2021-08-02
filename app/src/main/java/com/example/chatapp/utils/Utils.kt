package com.example.chatapp.utils

import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.ui.HomeFragment
import com.example.chatapp.ui.MainActivity
import com.example.chatapp.ui.SplashFragment
import com.google.gson.Gson
import kotlinx.coroutines.*
import net.glxn.qrgen.android.QRCode
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object Utils : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main

    fun getIpAndress(onResult: (String) -> Unit) {
        launch(Dispatchers.IO) {
            val wifiManager = MainApplication.getContextInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = android.text.format.Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            withContext(Dispatchers.Main) {
                onResult.invoke(ip)
            }
        }
    }

    fun messageClassToJSON(dataClass: Message): String {
        val json = Gson().toJson(dataClass)
        Log.e("toJSON", json)
        return json
    }

    fun JSONtoMessageClass(json: String): Message {
        val jsonToClass = Gson().fromJson(json, Message::class.java)
        Log.e("toClass", jsonToClass.toString())
        return jsonToClass
    }

    fun Activity.hideSoftKeyboard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    fun createSocket(ip: String, port: Int, onResult: (Socket) -> Unit) {
        launch(Dispatchers.IO) {
            val socket = Socket(ip, port)
            withContext(Dispatchers.Main) {
                onResult.invoke(socket)
            }
        }
    }

    fun generateQRCode(string: String): Bitmap {
        val bitmap = QRCode.from(string).bitmap()
        return bitmap
    }

    fun createNotification(tittle: String, text: String) {

        val context = MainApplication.getContextInstance()
        val notificationId = 101
        val CHANNEL_ID = "channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, tittle, importance).apply {
                description = text
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = MainApplication.getContextInstance().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        var builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_HIGH
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle(tittle)
            setContentText(text)
            setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0))
            setAutoCancel(true)
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun playBemTeVi(){
        val context = MainApplication.getContextInstance()
        MediaPlayer.create(context,R.raw.bemteviaudio).start()
    }

/*
passar um bundle com ip, porta, etc.. e remontar o chat na main
ou só abrir o app caso background
*/
}