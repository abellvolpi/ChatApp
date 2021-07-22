
package com.example.chatapp.objects

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import android.util.Log
import com.example.chatapp.ui.model.Message
import com.google.gson.Gson
import com.google.gson.GsonBuilder

import java.net.Inet4Address

object Utils {

    @SuppressLint("HardwareIds")
    fun getMacAndress(): String {
        val manager = MainApplication.getContextInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.connectionInfo
        return info.macAddress
    }

    fun getIpAndress(): String{
        return Inet4Address.getLocalHost().hostAddress
    }

    fun messageClassToJSON(dataClass: Message): String {
        val json = Gson().toJson(dataClass)
        Log.e("toJSON", json)
        return json
    }

    fun JSONtoMessageClass(json: String): List<Message> {
       val arrayofClass = Gson().fromJson(json, Array<Message>::class.java).toList()
        Log.e("toClass", arrayofClass.toString())
        return arrayofClass
    }
}