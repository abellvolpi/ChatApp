package com.example.chatapp.service

import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.utils.MainApplication

class BackgroundConnection(val connection: ConnectionFactory, workerparams: WorkerParameters): Worker(MainApplication.getContextInstance(), workerparams)  {



    override fun doWork(): Result {
        TODO("Not yet implemented")
    }


}