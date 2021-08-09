package com.example.chatapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReceiverMessageBroadCast : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.getBooleanExtra("finishConnection",false) == true) {
            val intent = Intent(context, ServerBackgroundService::class.java)
            intent.action = "com.example.stopserver"
            MainApplication.getContextInstance().startService(intent)
        }
    }
}