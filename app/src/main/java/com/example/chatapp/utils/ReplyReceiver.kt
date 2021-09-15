package com.example.chatapp.utils

import android.app.NotificationManager
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.chatapp.models.Message

class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val textReceived = RemoteInput.getResultsFromIntent(intent)?.getCharSequence(Utils.KEY_TEXT_REPLY).toString()
        val message =
            Message(
                Message.MessageType.MESSAGE.code,
                username = ProfileSharedProfile.getProfile(),
                text = textReceived,
                id = ProfileSharedProfile.getIdProfile(),
                dataBuffer = null,
                partNumber = null,
                dataSize = null
            )
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Utils.NOTIFICATION_ID)

        val myIntent = Intent(context, ServerBackgroundService::class.java).apply {
            action = ServerBackgroundService.SEND_REPLY
            putExtra("message", message)
        }
        MainApplication.getContextInstance().startService(myIntent)
    }
}