package com.example.chatapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Profile(
    @PrimaryKey
    var id : Int,
    var name : String,
    var photoProfile : String?,
    var scoreTicTacToe: Int,
    var isMemberYet: Boolean?
)