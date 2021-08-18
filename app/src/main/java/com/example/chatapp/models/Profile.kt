package com.example.chatapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

@Entity
class Profile(
    @PrimaryKey
    var id : Int,
    var name : String,
    var photoProfile : String?,
    var scoreTicTacToe: Int,
    var isMemberYet: Boolean?
)  {
}