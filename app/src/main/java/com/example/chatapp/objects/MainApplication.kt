package com.example.chatapp.objects

import android.app.Application
import android.content.Context

class MainApplication : Application(){
    init {
    instance = this
    }
    companion object{
        private lateinit var instance : Context
        fun getContextInstance(): Context {
            return instance.applicationContext
        }
    }
}