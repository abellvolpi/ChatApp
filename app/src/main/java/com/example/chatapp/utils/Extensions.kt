package com.example.chatapp.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import androidx.core.util.ObjectsCompat
import java.io.File
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

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
    fun String.toSHA256(): String {
        val messageDigest = MessageDigest.getInstance("SHA-256").digest(toByteArray())
        return messageDigest.fold("", { str, it -> str + "%02x".format(it) })
    }




}