package com.example.chatapp.utils

import android.app.*
import android.content.*
import android.content.Context.AUDIO_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object Utils : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    const val KEY_TEXT_REPLY = "key_text_reply"
    private const val CHANNEL_ID = "channel_id"
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
        val fromJson = adapter.fromJson(json)
        if (fromJson != null) {
            return fromJson
        }
        return Message(type = Message.MessageType.REVOKED.code, id = 2, text = null, base64Data = null, username = null) //server kick member because security system
    }

    fun createSocket(ip: String, port: Int, onResult: (Socket?) -> Unit) {
        launch(Dispatchers.IO) {
            delay(2000)
            try {
                val socket = Socket(ip, port)
                withContext(Dispatchers.Main) {
                    onResult.invoke(socket)
                }
            } catch (e: Exception) {
                Log.e("createSocket", e.toString())
                withContext(Dispatchers.Main) {
                    onResult.invoke(null)
                }
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    fun getAudioFromCache(message: Message): File? {
        if(message.base64Data != null || message.base64Data != ""){
            val output =
                File(message.base64Data)
            return output
        }
        return null
    }

    fun saveMessageAudioByteToCacheDir(message: Message, onResult: (String) -> Unit){
        val context = MainApplication.getContextInstance()
        val output =
            File(context.cacheDir.absolutePath + "/audios", "audio_${message.id}_${message.time}.mp3")
        val base64 = Base64.decode(message.base64Data, Base64.NO_WRAP)
        launch(Dispatchers.IO) {
            output.parentFile?.mkdirs()
            val fos = FileOutputStream(output)
            fos.write(base64)
            fos.flush()
            fos.close()
            withContext(Dispatchers.Main) {
                onResult.invoke(output.absolutePath)
            }
        }
    }

//    fun copyTobuttonClipBoard(context: Context?, text: String) {
//        val buttonClipBoard = context?.getSystemService(Context.buttonClipBOARD_SERVICE) as buttonClipboardManager
//        val buttonClipData = buttonClipData.newPlainText("label", text)
//        buttonClipBoard.setPrimarybuttonClip(buttonClipData)
//    }


    fun listJsonToProfiles(jsonList: String): List<Profile>? {
        val listType = Types.newParameterizedType(List::class.java, Profile::class.java)
        val adapter: JsonAdapter<List<Profile>> =
            Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(listType)
        return adapter.fromJson(jsonList)
    }

    fun byteArrayToBitMap(byteArray: String, onResult: (Bitmap) -> Unit) {
        launch(Dispatchers.Default) {
            val base64 = Base64.decode(byteArray, Base64.NO_WRAP)
            withContext(Dispatchers.Main) {
                onResult.invoke(BitmapFactory.decodeByteArray(base64, 0, base64.size))
            }
        }
    }

    fun bitmapToByteArrayToString(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG,10,byteArrayOutputStream)
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun bitmapToByteArray2(image: Drawable): String {
        val bitmap = (image as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    fun bitmapToByteArray3(image: Drawable, onResult: (String) -> Unit) {
        val bitmap = (image as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val encoded = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
        onResult.invoke(encoded)
    }


    fun uriToBitmap(uri: Uri?, contentResolver: ContentResolver): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && uri != null) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            BitmapFactory.decodeFileDescriptor(uri?.let {
                contentResolver.openFileDescriptor(it, "r")?.fileDescriptor
            })
        }
    }
}