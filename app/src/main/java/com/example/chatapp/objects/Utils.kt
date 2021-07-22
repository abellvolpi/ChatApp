
package com.example.chatapp.objects

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.util.Log
import com.example.chatapp.ui.model.Message
import com.google.gson.Gson
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
        return Gson().toJson(dataClass)
    }

    fun JSONtoMessageClass(json: String): Message {
        return Gson().fromJson(json, Message::class.java)
    }
}