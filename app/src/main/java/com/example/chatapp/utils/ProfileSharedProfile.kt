package com.example.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

object ProfileSharedProfile {

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
            putString("value", name)
            apply()
        }
    }

    fun saveIdProfile(id: Int){
        val profileSharedPreferences = getSharedProfile()
        with(profileSharedPreferences.edit()){
            putInt("id", id)
            apply()
        }
    }

    fun getIdProfile(): Int{
        val shred = getSharedProfile()
        val int = shred.getInt("id", 0)
        return int
    }

    fun getProfile(): String {
        val shared = getSharedProfile()
        return shared.getString("value", "NO NAME SAVED") ?: "NO NAME SAVED"
    }

    fun saveProfilePhoto(imageBitmap: Bitmap) {
        val profileSharedPreferences = getSharedProfile()
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 10, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val result = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        profileSharedPreferences.edit().apply {
            clear()
            putString("image", result)
            apply()
        }
    }

    fun getProfilePhoto(): Bitmap? {
        val sharedPreferences = getSharedProfile()
        val bitmap = sharedPreferences.getString("image", "NO IMAGE SAVED")
        if (bitmap == "NO IMAGE SAVED") {
            return null
        }
        return BitmapFactory.decodeByteArray(Base64.decode(bitmap, 0), 0, Base64.decode(bitmap, 0).size)
    }


    fun bitmapToByteArrayToString(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG,10,baos)
        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 30, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }


    fun clearSharedPreferences() {
        getSharedProfile().edit().apply {
            clear()
            apply()
        }
    }

}