package com.example.chatapp.room

import androidx.room.TypeConverter
import com.example.chatapp.models.Message
import com.google.gson.Gson

class TypeConverter {

    @TypeConverter
    fun sourceToJson(value: Message.Join?): String? = Gson().toJson(value)

    @TypeConverter
    fun jsonToSource(string: String?): Message.Join? = Gson().fromJson(string, Message.Join::class.java)

}