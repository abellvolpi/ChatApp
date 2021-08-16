package com.example.chatapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReceiverMessageBroadCast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getBooleanExtra("finishConnection",false) == true) {
            val myIntent = Intent(context, ServerBackgroundService::class.java)
            myIntent.action = ServerBackgroundService.STOP_SERVER
            MainApplication.getContextInstance().stopService(myIntent)
        }
    }
}