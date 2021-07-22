package com.example.chatapp.objects

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.xml.sax.DTDHandler
import java.io.ObjectInputStream
import java.net.Socket
import java.util.*
import kotlin.coroutines.CoroutineContext

object ConnectionFactory: CoroutineScope {

    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main

    fun clientConnecting(){
        var client : Socket = Socket("nome",13)
        var entrada : ObjectInputStream = ObjectInputStream(client.getInputStream())
        var receive = entrada.readObject()
        entrada.close()

    }


}