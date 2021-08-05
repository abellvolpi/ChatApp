package com.example.chatapp.utils

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.net.toUri
import com.example.chatapp.R
import kotlinx.coroutines.*
import java.io.File
import java.io.InputStream
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

    fun getProfile(): String {
        val shared = getSharedProfile()
        val string = shared.getString("value", "NO NAME SAVED") ?: "NO NAME SAVED"
        return string
    }

    fun saveProfilePhoto(imageUri: Uri) {
        val profileSharedPreferences = getSharedProfile()
        profileSharedPreferences.edit().apply {
            putString("image", imageUri.toString())
            apply()
        }
    }

    fun getProfilePhoto(): Uri {
        val sharedPreferences = getSharedProfile()
        val uri = sharedPreferences.getString("image", "NO IMAGE SAVED")
        if (uri == "NO IMAGE SAVED") {
            return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.resources.getResourcePackageName(R.drawable.ic_profile) + "/drawable/ic_profile")
        }
        return uri?.toUri()
            ?: Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + context.resources.getResourcePackageName(R.drawable.ic_profile) + "/drawable/ic_profile")
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

}