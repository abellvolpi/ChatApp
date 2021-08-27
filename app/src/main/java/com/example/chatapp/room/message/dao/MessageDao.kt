package com.example.chatapp.room.message.dao

import androidx.room.*
import com.example.chatapp.models.Message
import com.example.chatapp.room.withs.MessagesWithProfile

@Dao
interface MessageDao {

    @Query("SELECT * FROM message")
    fun getAll(): List<Message>

    @Insert
    fun insert(vararg message: Message)

    @Delete
    fun delete(vararg message: Message)

    @Update
    fun update(vararg message: Message)

    @Query("DELETE FROM Message")
    fun deleteAll()

    @Transaction
    @Query("Select * From message")
    fun getMessagesWithProfile(): List<MessagesWithProfile>

}