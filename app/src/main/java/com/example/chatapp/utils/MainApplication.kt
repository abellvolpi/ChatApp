package com.example.chatapp.utils

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MainApplication : Application(), LifecycleObserver{

    init {
    instance = this
    applicationScope = CoroutineScope(SupervisorJob())
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
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
        fun aplicationIsInBackground(): Boolean{
            return ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED
        }


    }


}