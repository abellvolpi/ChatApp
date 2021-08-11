package com.example.chatapp.utils

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.ui.MainActivity
import com.google.gson.Gson
import kotlinx.coroutines.*
import net.glxn.qrgen.android.QRCode
import java.io.File
import java.io.FileOutputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object Utils : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main

    fun getIpAddress(): String {
        val wifiManager =
            MainApplication.getContextInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return android.text.format.Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            ?: ""
    }

    fun messageClassToJSON(dataClass: Message): String {
        val json = Gson().toJson(dataClass)
        Log.e("toJSON", json)
        return json
    }

    fun jsonToMessageClass(json: String): Message {
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
            delay(2000)
            val socket = Socket(ip, port)
            withContext(Dispatchers.Main) {
                onResult.invoke(socket)
            }
        }
    }

    fun generateQRCode(string: String): Bitmap {
        return QRCode.from(string).bitmap()
    }

    fun createNotification(tittle: String, text: String) {

        val context = MainApplication.getContextInstance()
        val notificationId = 101
        val channelId = "channel_id"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, tittle, importance).apply {
                description = text
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = MainApplication.getContextInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java)

        var builder = NotificationCompat.Builder(context, channelId).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_HIGH
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle(tittle)
            setContentText(text)
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
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun playBemTeVi() {
        val context = MainApplication.getContextInstance()
        val audioManager: AudioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
            0
        )
        MediaPlayer.create(context, R.raw.bemteviaudio).start()
    }

    fun parseAnythingToByteString(file: File, onResult: (String) -> Unit) {
        var enconded: String
        launch(Dispatchers.IO) {
            enconded = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
            withContext(Dispatchers.Main) {
                onResult.invoke(enconded)
            }
        }
    }

    fun parseByteToAudio(bytes: String, onResult: (File) -> Unit) {
        val context = MainApplication.getContextInstance()
        val output = context.cacheDir.absolutePath + "/recentAudio.mp3"
        val decoded = Base64.decode(bytes, Base64.NO_WRAP)
        launch(Dispatchers.IO) {
            val fos = FileOutputStream(output)
            fos.write(decoded)
            fos.flush()
            fos.close()
            withContext(Dispatchers.Main) {
                onResult.invoke(File(output))
            }
        }
    }
    fun copyToClipBoard(context: Context?, text: String){
        val clipBoard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label",text)
        clipBoard.setPrimaryClip(clipData)
    }

    fun getAddressFromSocket(socket: Socket): String {
        val inetSocketAddress = socket.remoteSocketAddress as InetSocketAddress
        val inet4Address = inetSocketAddress.address as Inet4Address
        return inet4Address.toString().replace("/", "")
    }

    fun getPortFromSocket(socket: Socket):String{
        val inetSocketAddress = socket.remoteSocketAddress as InetSocketAddress
        val port = inetSocketAddress.port
        return port.toString()
    }

    fun createToast(text: String){
        Toast.makeText(MainApplication.getContextInstance(),text,Toast.LENGTH_LONG).show()
    }


}