package com.example.chatapp.room

import androidx.room.TypeConverter
import com.example.chatapp.models.Message
import com.example.chatapp.tictactoe.TicMessages
import com.google.gson.Gson

class TypeConverter {

    @TypeConverter
    fun messageToJson(value: Message.Join?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToMessage(string: String?): Message.Join? = Gson().fromJson(string, Message.Join::class.java)

    @TypeConverter
    fun ticMessagesToJson(value: TicMessages?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToTicMessages(string: String?): TicMessages? = Gson().fromJson(string, TicMessages::class.java)


}