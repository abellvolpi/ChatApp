package com.example.chatapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chatapp.utils.MainApplication.Companion.getContextInstance

class ReceiverMessageBroadCast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context==null){
            return
        }

        if (intent?.getBooleanExtra("finishConnection",false) == true) {
            val myIntent = Intent(context, ServerBackgroundService::class.java)
            myIntent.action = ServerBackgroundService.STOP_SERVER
            LocalBroadcastManager.getInstance(context).sendBroadcast(myIntent)
        }
    }
}