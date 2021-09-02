package com.example.chatapp.tictactoe

data class TicTacToeGame(val matchId: Int, val player1Id: Int, val player2Id: Int){
    val board : ArrayList<String> = arrayListOf()
}