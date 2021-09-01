package com.example.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
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
            putString("value", name)
            apply()
        }
    }

    fun saveIdProfile(id: Int) {
        val profileSharedPreferences = getSharedProfile()
        with(profileSharedPreferences.edit()) {
            putInt("id", id)
            apply()
        }
    }

    fun getIdProfile(): Int {
        val shred = getSharedProfile()
        return shred.getInt("id", 0)
    }

    fun getProfile(): String {
        val shared = getSharedProfile()
        return shared.getString("value", "NO NAME SAVED") ?: "NO NAME SAVED"
    }


    private fun saveProfilePhoto(imageBitmap: Bitmap) {
        launch(Dispatchers.Default) {
            val profileSharedPreferences = getSharedProfile()
            val byteArrayOutputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 10, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            val result = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            profileSharedPreferences.edit().apply {
//                clear()
                putString("image", result)
                apply()
            }
        }
    }

    fun saveUriProfilePhoto(imageUri: Uri) {
        val profileSharedPreferences = getSharedProfile()
        profileSharedPreferences.edit().apply {
            clear()
            putString("imageUri", imageUri.toString())
            apply()
        }
        launch(Dispatchers.Default) {
            Utils.uriToBitmap(imageUri, context.contentResolver) {
                saveProfilePhoto(it)
            }
        }
    }


//    fun getProfilePhoto(): Bitmap? {
//        val sharedPreferences = getSharedProfile()
//        val bitmap = sharedPreferences.getString("image", "NO IMAGE SAVED")
//        if (bitmap == "NO IMAGE SAVED") {
//            return null
//        }
//        return BitmapFactory.decodeByteArray(Base64.decode(bitmap, 0), 0, Base64.decode(bitmap, 0).size)
//    }

    fun getProfilePhoto(onResult: (Bitmap?) -> Unit) {
        val sharedPreferences = getSharedProfile()
        val bitmap = sharedPreferences.getString("image", "NO IMAGE SAVED")
        if (bitmap == "NO IMAGE SAVED") {
            onResult.invoke(null)
        } else {
            val result = BitmapFactory.decodeByteArray(Base64.decode(bitmap, 0), 0, Base64.decode(bitmap, 0).size)
            onResult.invoke(result)
        }
    }


    fun getUriProfilePhoto(): Uri? {
        val sharedPreferences = getSharedProfile()
        val uri = sharedPreferences.getString("imageUri", "NO IMAGE SAVED")
        if (uri == "NO IMAGE SAVED") {
            return null
        }
        return uri?.toUri()
    }


    fun getProfilePhotoBase64(): String? {
        val sharedPreferences = getSharedProfile()
        return sharedPreferences.getString("image", null)
    }


    fun bitmapToByteArrayToString(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG,10,byteArrayOutputStream)
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