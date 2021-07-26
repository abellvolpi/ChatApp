
package com.example.chatapp.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.net.wifi.WifiManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.chatapp.models.Message
import com.google.gson.Gson
import kotlinx.coroutines.*
import net.glxn.qrgen.android.QRCode
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object Utils: CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()+Dispatchers.Main

    fun getIpAndress(onResult: (String) -> Unit){
        launch(Dispatchers.IO) {
            val wifiManager = MainApplication.getContextInstance().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val ip = android.text.format.Formatter.formatIpAddress(wifiManager.connectionInfo.ipAddress)
            withContext(Dispatchers.Main){
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

    fun Activity.hideSoftKeyboard(){
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


    fun createSocket(ip: String, port: Int, onResult: (Socket) -> Unit){
        launch(Dispatchers.IO) {
            val socket = Socket(ip, port)
            withContext(Dispatchers.Main){
                onResult.invoke(socket)
            }
        }
    }

    fun generateQRCode(string: String): Bitmap {
        val bitmap = QRCode.from(string).bitmap()
        return bitmap
    }
}