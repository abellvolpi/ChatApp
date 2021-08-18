package com.example.chatapp.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import java.io.File
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.Socket

object Extensions {

    fun Activity.hideSoftKeyboard() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).apply {
            hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    fun Socket.getPortFromSocket(): String {
        val inetSocketAddress = this.remoteSocketAddress as InetSocketAddress
        val port = inetSocketAddress.port
        return port.toString()
    }


    fun Socket.getAddressFromSocket(): String {
        val inetSocketAddress = this.remoteSocketAddress as InetSocketAddress
        val inet4Address = inetSocketAddress.address as Inet4Address
        return inet4Address.toString().replace("/", "")
    }




}