package com.example.chatapp.room.profile.controller

import android.util.Base64
import androidx.room.Room
import com.example.chatapp.models.Profile
import com.example.chatapp.room.appDataBase.AppDataBase
import com.example.chatapp.utils.MainApplication
import java.io.File
import java.io.FileOutputStream

class ProfileController {
    private val profileController = Room
        .databaseBuilder(MainApplication.getContextInstance(), AppDataBase::class.java, "chatAppDB")
        .fallbackToDestructiveMigration()
        .build()
        .profileDAO()

    fun getAll(): ArrayList<Profile>{
        val arrayList = arrayListOf<Profile>()
        arrayList.addAll(profileController.getAll())
        return arrayList
    }
    fun insert(profile: Profile){
        if (profile.photoProfile != "" || profile.photoProfile != null) {
            saveAvatarToCacheDir(
                profile.id,
                profile.photoProfile ?: ""
            ) {
                profile.photoProfile = it
                profile.isMemberYet = true
                profileController.insert(profile)
            }
        } else {
            profile.photoProfile = ""
            profile.isMemberYet = true
            profileController.insert(profile)
        }
    }
    fun delete(id: Int){
        profileController.delete(id)
    }
    fun update(profile: Profile){
        profileController.update(profile)
    }
    fun getById(id: Int): Profile?{
        return profileController.getById(id)
    }
    fun deleteAll(){
        profileController.deleteAll()
    }
    fun getRanking(): ArrayList<Profile>{
        val arrayList = arrayListOf<Profile>()
        arrayList.addAll(profileController.getRanking())
        return arrayList
    }

    fun getProfileWhereIsMemberYet(): ArrayList<Profile> {
        val arrayList = arrayListOf<Profile>()
        arrayList.addAll(profileController.getProfileWhereIsMemberYet())
        return arrayList
    }

    private fun saveAvatarToCacheDir(id: Int, string: String, onResult: (String) -> Unit) {
        val context = MainApplication.getContextInstance()
        val output =
            File(context.cacheDir.absolutePath + "/photosProfile", "profilePhoto_${id}.jpg")
        val base64 = Base64.decode(string, Base64.NO_WRAP)
        output.parentFile?.mkdirs()
        val fos = FileOutputStream(output)
        fos.write(base64)
        fos.flush()
        fos.close()
        onResult.invoke(output.absolutePath)
    }


}