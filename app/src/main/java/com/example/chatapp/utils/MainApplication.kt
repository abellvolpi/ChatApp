package com.example.chatapp.utils

import android.app.Application
import android.content.Context
import androidx.lifecycle.*

class MainApplication : Application(), LifecycleObserver {

    init {
    instance = this
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        isBackground = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        isBackground = false
    }

    companion object{
        private lateinit var instance : Context
        private var isBackground: Boolean = false

        fun getContextInstance(): Context {
            return instance.applicationContext
        }
        fun applicationIsInBackground(): Boolean {
            return isBackground
        }
    }
}