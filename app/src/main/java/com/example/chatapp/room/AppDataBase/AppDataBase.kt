package com.example.chatapp.room.AppDataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatapp.models.Profile
import com.example.chatapp.room.Profile.DAO.ProfileDAO

@Database(version = 1, entities = arrayOf(Profile::class), exportSchema = false)
abstract class AppDataBase: RoomDatabase() {
    abstract fun profileDAO():ProfileDAO
}