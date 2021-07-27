package com.example.chatapp.utils

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MainApplication : Application(){

    init {
    instance = this
    applicationScope = CoroutineScope(SupervisorJob())
    }
    companion object{
        private lateinit var instance : Context
        private lateinit var applicationScope: CoroutineScope
        fun getContextInstance(): Context {
            return instance.applicationContext
        }
        fun getCoroutineScope(): CoroutineScope{
            return applicationScope
        }


    }
}