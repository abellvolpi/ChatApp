package com.example.chatapp.utils

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Base64
import android.util.Base64InputStream
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.ui.HomeFragment
import com.example.chatapp.ui.MainActivity
import com.example.chatapp.ui.SplashFragment
import com.example.chatapp.viewModel.ConnectionFactory
import com.google.gson.Gson
import kotlinx.coroutines.*
import net.glxn.qrgen.android.QRCode
import java.io.*
import java.net.Socket
import java.nio.file.Files
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
        val intent = Intent(context, MainActivity::class.java).apply {
//            putExtra("PORTA","testeporta")
//            putExtra("IP","testeip")
        }
        var builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_HIGH
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle(tittle)
            setContentText(text)
            setExtras(bundleOf(
//                Pair("PORTA",port)
//                Pair("IP",ip)
            ))
            setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
            setAutoCancel(true)
        }
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun playBemTeVi(){
        val context = MainApplication.getContextInstance()
        val audioManager: AudioManager = context.getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),0)
        MediaPlayer.create(context,R.raw.bemteviaudio).start()

    }

    fun parseAnythingToByteString(file: File, onResult: (String) -> Unit){
        var enconded : String
        launch(Dispatchers.IO){
            enconded = Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
            withContext(Dispatchers.Main){
                onResult.invoke(enconded)
            }
        }
    }

    fun parseBytoToAudio(bytes : String, onResult: (File) -> Unit){
        val context = MainApplication.getContextInstance()
        val output = context.cacheDir.absolutePath+"/recentAudio.mp3"
        val decoded = Base64.decode(bytes, Base64.NO_WRAP)
        launch(Dispatchers.IO) {
            val fos = FileOutputStream(output)
            fos.write(decoded)
            fos.flush()
            fos.close()
            withContext(Dispatchers.Main){
                onResult.invoke(File(output))
            }
        }
    }

/*
passar um bundle com ip, porta, etc.. e remontar o chat na main
ou só abrir o app caso background
*/
}