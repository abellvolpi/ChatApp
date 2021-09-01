package com.example.chatapp.room.withs

import androidx.room.Embedded
import androidx.room.Relation
import com.example.chatapp.models.Message
import com.example.chatapp.models.Profile

class MessagesWithProfile(
    @Embedded
    val profile: Profile,
    @Relation(parentColumn = "id", entityColumn = "fk_profile")
    val messages: Message){
}