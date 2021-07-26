package com.example.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

object ProfileSharedProfile : CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main

    private const val NAME = "profile"
    private val context by lazy {
        MainApplication.getContextInstance()
    }

    private fun getSharedProfile(): SharedPreferences {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    fun saveProfile(name: String, onResult: () -> Unit) {
        launch(Dispatchers.IO) {
            val profileSharedPreferenes = getSharedProfile()
            with(profileSharedPreferenes.edit()) {
                clear()
                putString("value", name)
                apply()
            }
            withContext(Dispatchers.Main){
                onResult.invoke()
            }
        }
    }

    fun getProfile(onResult: (String) -> Unit) {
        launch(Dispatchers.IO) {
            val shared = getSharedProfile()
            val string = shared.getString("value", "NO NAME SAVED") ?: "NO NAME SAVED"
            withContext(Dispatchers.Main){
                onResult.invoke(string)
            }
        }
    }
}