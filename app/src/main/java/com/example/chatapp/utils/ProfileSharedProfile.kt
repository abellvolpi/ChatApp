package com.example.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
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
        val profileSharedPreferences = getSharedProfile()
        with(profileSharedPreferences.edit()) {
            clear()
            putString("value", name)
            apply()
        }
    }

    fun getProfile(onResult: (String) -> Unit) {
        val shared = getSharedProfile()
        val string = shared.getString("value", "NO NAME SAVED") ?: "NO NAME SAVED"
        onResult.invoke(string)
    }

    fun saveProfilePhoto(imageUri: Uri) {
        val profileSharedPreferences = getSharedProfile()
        profileSharedPreferences.edit().apply {
            clear()
            putString("image", imageUri.toString())
            apply()
        }
    }

    fun getProfilePhoto(onResult:(Uri) -> Unit) {
        val sharedPreferences = getSharedProfile()
        val uri = sharedPreferences.getString("image","NO IMAGE SAVED")
        uri?.let { onResult.invoke(it.toUri()) }
    }

}