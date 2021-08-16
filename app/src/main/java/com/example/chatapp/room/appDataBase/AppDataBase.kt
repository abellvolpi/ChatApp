package com.example.chatapp.room.AppDataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatapp.models.Profile
import com.example.chatapp.room.profile.dao.ProfileDAO

@Database(version = 1, entities = [Profile::class], exportSchema = false)
abstract class AppDataBase: RoomDatabase() {
    abstract fun profileDAO(): ProfileDAO
}