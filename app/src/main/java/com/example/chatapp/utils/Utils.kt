package com.example.chatapp.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.util.Log
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.ui.MainActivity
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

        var builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle(tittle)
            setContentText(text)
            setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), 0))
            setAutoCancel(true)
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun verifyIfAppIsOnBackground(){

    }

}