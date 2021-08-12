package com.example.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File

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
        }
    }

    fun getIdProfile(): Int{
        val shred = getSharedProfile()
        val int = shred.getInt("id", 0) ?: 0
        return int
    }

    fun getProfile(): String {
        val shared = getSharedProfile()
        val string = shared.getString("value", "NO NAME SAVED") ?: "NO NAME SAVED"
        return string
    }

    fun saveProfilePhoto(imageBitmap: Bitmap) {
        val profileSharedPreferences = getSharedProfile()
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 10, baos)
        val b = baos.toByteArray()
        val result = Base64.encodeToString(b, Base64.NO_WRAP)
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
        return BitmapFactory.decodeByteArray(Base64.decode(bitmap,0),0,Base64.decode(bitmap,0).size)
    }


    fun UriToByteArrayToString(uri: Uri): String {
        val file = File(context.cacheDir, "resize.png")
        val byteArray = file.inputStream().buffered().use {
            it.readBytes()
        }
//        val byteArray = context.contentResolver.openInputStream(uri)?.buffered()?.use {
//            it.readBytes()
//        }
//        val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray!!.size)
//        val bitmapEdited = Bitmap.createScaledBitmap(bitmap,100,100,false)
//        val stream = ByteArrayOutputStream()
//        bitmapEdited.compress(Bitmap.CompressFormat.PNG,50,stream)
//        val image = stream.toByteArray()
//        return Base64.encodeToString(image,Base64.NO_WRAP)

        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    fun BitmapToByteArrayToString(bitmap: Bitmap):String{
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap,50,50,false)
        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG,10,baos)
        scaledBitmap.compress(Bitmap.CompressFormat.PNG,10,baos)

        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }


    fun clearSharedPreferences(){
        getSharedProfile().edit().apply{
            clear()
            apply()
        }
    }

}