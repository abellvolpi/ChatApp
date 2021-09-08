package com.example.chatapp.tictactoe

import com.example.chatapp.models.Cell

object UsersTicTacToeManager {
    const val PLAYER = "O"
    const val OPPONENT = "X"
    var matchId : Int? = null
    var boardPlaces = arrayOf<String?>()
    var opponentId : Int = -1


    fun placeMove(position: Cell, player: String) {
        boardPlaces[position.i] = player
    }

    fun restartGame() {
        boardPlaces = emptyArray()
        matchId = null
    }

    fun newGame(matchId : Int){
        this.matchId = matchId
    }

//    fun gameIsOver(): Boolean {
//        if (player1Won() || player2Won() || hasDraw()) {
//            return true
//        }
//        return false
//    }
//
//    fun player2Won(): Boolean {
//        for (i in boardPlaces.indices step 3) {
//            if (boardPlaces[i] == boardPlaces[i + 1] &&
//                boardPlaces[i + 1] == boardPlaces[i + 2] &&
//                boardPlaces[i + 2] == player2
//            ) {
//                return true
//            }
//        }
//        for (i in 0..2) {
//            if (boardPlaces[i] == boardPlaces[i + 3] && boardPlaces[i + 3] == boardPlaces[i + 6] && boardPlaces[i] == player2) {
//                return true
//            }
//        }
//        if (boardPlaces[0] == boardPlaces[4] && boardPlaces[4] == boardPlaces[8] && boardPlaces[8] == player2 ||
//            boardPlaces[2] == boardPlaces[4] && boardPlaces[4] == boardPlaces[6] && boardPlaces[6] == player2
//        ) {
//            return true
//        }
//        return false
//    }
//
//    fun player1Won(): Boolean {
//        for (i in boardPlaces.indices step 3) {
//            if (boardPlaces[i] == boardPlaces[i + 1] &&
//                boardPlaces[i + 1] == boardPlaces[i + 2] &&
//                boardPlaces[i + 2] == player1
//            ) {
//                return true
//            }
//        }
//        for (i in 0..2) {
//            if (boardPlaces[i] == boardPlaces[i + 3] && boardPlaces[i + 3] == boardPlaces[i + 6] && boardPlaces[i] == player1) {
//                return true
//            }
//        }
//        if (boardPlaces[0] == boardPlaces[4] && boardPlaces[4] == boardPlaces[8] && boardPlaces[8] == player1 ||
//            boardPlaces[2] == boardPlaces[4] && boardPlaces[4] == boardPlaces[6] && boardPlaces[6] == player1
//        ) {
//            return true
//        }
//        return false
//    }
//
//    fun hasDraw(): Boolean {
//        for (i in boardPlaces.indices) {
//            if (boardPlaces[i].isNullOrEmpty()) {
//                return false
//            }
//        }
//        return true
//    }
}