package com.example.chatapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Profile(
    val name : String,
    val ipAddress : String,
    val photoProfile : String?
)  {
    @PrimaryKey(autoGenerate = true)
    var id : Int? =null
}