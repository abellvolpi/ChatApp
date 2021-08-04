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

    fun saveProfile(name: String) {
        val profileSharedPreferenes = getSharedProfile()
        with(profileSharedPreferenes.edit()) {
            clear()
            putString("value", name)
            apply()
        }
    }

    fun getProfile(): String {
        val shared = getSharedProfile()
        val string = shared.getString("value", "NO NAME SAVED") ?: "NO NAME SAVED"
        return string
    }
}