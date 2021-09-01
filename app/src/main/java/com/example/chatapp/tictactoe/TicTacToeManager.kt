package com.example.chatapp.tictactoe

import java.net.Socket

class TicTacToeManager(val socket1Id: Int,
                       val socket2Id: Int) {

    private val player1 = "O"
    private val player2 = "X"


    var boardPlaces = arrayOf<String?>()



    fun placeMove(player: String, position: Int) {
        boardPlaces[position] = player
    }

    fun restartGame() {
        boardPlaces = emptyArray()
    }

    fun gameIsOver(): Boolean {
        if (player1Won() || player2Won() || hasDraw()) {
            return true
        }
        return false
    }

    fun player2Won(): Boolean {
        for (i in boardPlaces.indices step 3) {
            if (boardPlaces[i] == boardPlaces[i + 1] &&
                boardPlaces[i + 1] == boardPlaces[i + 2] &&
                boardPlaces[i + 2] == player2
            ) {
                return true
            }
        }
        for (i in 0..2) {
            if (boardPlaces[i] == boardPlaces[i + 3] && boardPlaces[i + 3] == boardPlaces[i + 6] && boardPlaces[i] == player2) {
                return true
            }
        }
        if (boardPlaces[0] == boardPlaces[4] && boardPlaces[4] == boardPlaces[8] && boardPlaces[8] == player2 ||
            boardPlaces[2] == boardPlaces[4] && boardPlaces[4] == boardPlaces[6] && boardPlaces[6] == player2
        ) {
            return true
        }
        return false
    }

    fun player1Won(): Boolean {
        for (i in boardPlaces.indices step 3) {
            if (boardPlaces[i] == boardPlaces[i + 1] &&
                boardPlaces[i + 1] == boardPlaces[i + 2] &&
                boardPlaces[i + 2] == player1
            ) {
                return true
            }
        }
        for (i in 0..2) {
            if (boardPlaces[i] == boardPlaces[i + 3] && boardPlaces[i + 3] == boardPlaces[i + 6] && boardPlaces[i] == player1) {
                return true
            }
        }
        if (boardPlaces[0] == boardPlaces[4] && boardPlaces[4] == boardPlaces[8] && boardPlaces[8] == player1 ||
            boardPlaces[2] == boardPlaces[4] && boardPlaces[4] == boardPlaces[6] && boardPlaces[6] == player1
        ) {
            return true
        }
        return false
    }

    fun hasDraw(): Boolean {
        for (i in boardPlaces.indices) {
            if (boardPlaces[i].isNullOrEmpty()) {
                return false
            }
        }
        return true
    }

}