package com.example.chatapp.room.profile.controller

import androidx.room.Room
import com.example.chatapp.models.Profile
import com.example.chatapp.room.appDataBase.AppDataBase
import com.example.chatapp.utils.MainApplication

class ProfileController {
    private val profileController = Room
        .databaseBuilder(MainApplication.getContextInstance(), AppDataBase::class.java, "chatAppDB")
        .fallbackToDestructiveMigration()
        .build()
        .profileDAO()

    fun getAll(): List<Profile>{
        return  profileController.getAll()
    }
    fun insert(profile: Profile){
        profileController.insert(profile)
    }
    fun delete(profile: Profile){
        profileController.delete(profile)
    }
    fun update(profile: Profile){
        profileController.update(profile)
    }
    fun getByIp(ip: String): Profile?{
        return profileController.getByIpAddress(ip)
    }
    fun deleteAll(){
        profileController.deleteAll()
    }
}