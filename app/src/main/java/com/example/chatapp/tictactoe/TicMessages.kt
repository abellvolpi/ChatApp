package com.example.chatapp.tictactoe

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class TicMessages(var player1Id: Int? = null,
                  var player2Id: Int? = null) : Serializable