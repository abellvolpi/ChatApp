package com.example.chatapp.room

import androidx.room.TypeConverter
import com.example.chatapp.models.Message
import com.google.gson.Gson

class TypeConverter {

    @TypeConverter
    fun messageToJson(value: Message.Join?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToMessage(string: String?): Message.Join? = Gson().fromJson(string, Message.Join::class.java)

    @TypeConverter
    fun ticTacToePlaysToJson(value: Message.TicTacToePlay?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToTicTacToePlays(string: String?): Message.TicTacToePlay? = Gson().fromJson(string, Message.TicTacToePlay::class.java)


}