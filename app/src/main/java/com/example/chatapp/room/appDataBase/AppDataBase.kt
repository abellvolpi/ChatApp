package com.example.chatapp.room.appDataBase

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.chatapp.models.Message
import com.example.chatapp.models.Profile
import com.example.chatapp.room.message.dao.MessageDao
import com.example.chatapp.room.profile.dao.ProfileDAO

@Database(version = 1, entities = [Profile::class, Message::class], exportSchema = false)
abstract class AppDataBase: RoomDatabase() {
    abstract fun profileDAO():ProfileDAO
    abstract fun messageDao():MessageDao
}