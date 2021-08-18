package com.example.chatapp.utils

import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.AUDIO_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Base64
import android.util.JsonReader
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.example.chatapp.R
import com.example.chatapp.models.Message
import com.example.chatapp.models.Profile
import com.example.chatapp.ui.MainActivity
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import net.glxn.qrgen.android.QRCode
import java.io.File
import java.io.FileOutputStream
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object Utils : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    const val KEY_TEXT_REPLY = "key_text_reply"
    const val CHANNEL_ID = "channel_id"
    const val NOTIFICATION_ID = 101

    fun getIpAddress(): String {
        val wifiManager =
            MainApplication.getContextInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return android.text.format.Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            ?: ""
    }

    fun messageClassToJSON(dataClass: Message): String {
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Message::class.java)
        val json = adapter.toJson(dataClass)
        Log.d("messageClassToJSON", json)
        return json
    }

    fun jsonToMessageClass(json: String): Message {
        Log.d("jsonToMessageClass", json)
        val moshi = Moshi.Builder().build()
        val adapter = moshi.adapter(Message::class.java)
        val fromJson  = adapter.fromJson(json)
        if(fromJson != null){
            return fromJson
        }
        return Message(type = Message.MessageType.REVOKED.code, id = 2, text = null, base64Data = null, username = null) //server kick member because security system
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, tittle, importance).apply {
                description = text
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = MainApplication.getContextInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
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


    fun createReplyableNotification(tittle: String, text: String) {

        val context = MainApplication.getContextInstance()
        val intent = Intent(context, ReplyReceiver::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, tittle, importance).apply {
                description = text
            }
            val notificationManager: NotificationManager = MainApplication.getContextInstance()
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val replyLabel: String = context.getString(R.string.test)

        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }
        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val action =
            NotificationCompat.Action.Builder(R.drawable.ic_send_icon, context.getString(R.string.reply), replyPendingIntent)
                .addRemoteInput(remoteInput).build()


        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            color = ContextCompat.getColor(context, R.color.blue)
            priority = NotificationCompat.PRIORITY_HIGH
            setSmallIcon(R.drawable.ic_telegram)
            setContentTitle(tittle)
            setContentText(text)
            addAction(action)
            setAutoCancel(true)
        }
        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
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

    fun copyToClipBoard(context: Context?, text: String) {
        val clipBoard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("label", text)
        clipBoard.setPrimaryClip(clipData)
    }


    fun createToast(text: String) {
        Toast.makeText(MainApplication.getContextInstance(), text, Toast.LENGTH_LONG).show()
    }

    fun listJsonToProfiles(jsonList: String): List<Profile>? {
        val listType = Types.newParameterizedType(List::class.java, Profile::class.java)
        val adapter: JsonAdapter<List<Profile>> =
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(listType)
        return adapter.fromJson(jsonList)
    }

    fun byteArrayToBitMap(byte: String): Bitmap {
        val base64 = Base64.decode(byte, Base64.NO_WRAP)
        return BitmapFactory.decodeByteArray(base64, 0, base64.size)
    }

}