
package com.example.chatapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.chatapp.models.Message
import com.google.gson.Gson
import kotlinx.coroutines.*

import java.net.Inet4Address
import java.net.Socket
import kotlin.coroutines.CoroutineContext

object Utils: CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()+Dispatchers.Main

    fun getIpAndress(): String{
        return Inet4Address.getLocalHost().hostAddress
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

    fun createSocket(ip: String, port: Int, onResult: (Socket) -> Unit){
        launch(Dispatchers.IO) {
            val socket = Socket(ip, port)
            withContext(Dispatchers.Main){
                onResult.invoke(socket)
            }
        }
    }


}