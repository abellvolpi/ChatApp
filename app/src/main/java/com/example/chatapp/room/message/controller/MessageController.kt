package com.example.chatapp.room.message.controller

import androidx.room.Room
import com.example.chatapp.models.Message
import com.example.chatapp.room.appDataBase.AppDataBase
import com.example.chatapp.utils.MainApplication


object MessageController {
    private val messageController = Room
        .databaseBuilder(MainApplication.getContextInstance(), AppDataBase::class.java, "chatAppDB")
        .fallbackToDestructiveMigration()
        .build()
        .messageDao()

    fun getAll(): List<Message> {
        return messageController.getAll()
    }

    fun insert(message: Message) {
        return messageController.insert(message)
    }

    fun delete(message: Message) {
        return messageController.delete(message)
    }

    fun update(message: Message) {
        return messageController.update(message)
    }

}
