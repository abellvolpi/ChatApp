package com.example.chatapp.tictactoe

object ServerTicTacToeManager {

    private lateinit var ticTacToeArray: ArrayList<TicTacToeGame>
    private const val player1 = "O"
    private const val player2 = "X"


    fun newGame(player1Id: Int, player2Id: Int) {
        for (i in ticTacToeArray.indices) {
            if (ticTacToeArray.lastIndex == i) {
                val matchId = i + 1
                val ticTacToeGame = TicTacToeGame(matchId, player1Id, player2Id)
                ticTacToeArray.add(ticTacToeGame)
            }
        }
    }

    fun searchInMatches(playerId: Int): Boolean {
        for (i in ticTacToeArray.indices) {
            if (ticTacToeArray[i].player1Id == playerId || ticTacToeArray[i].player2Id == playerId) {
                return true
            }
        }
        return false
    }


    fun placeMove(playerId: Int, position: Int): Boolean {

        for (i in ticTacToeArray.indices) {
            if (ticTacToeArray[i].player1Id == playerId) {
                if (ticTacToeArray[i].board[position].isEmpty()) {
                    ticTacToeArray[i].board[position] = player1
                    return true
                }
            }
            if (ticTacToeArray[i].player2Id == playerId) {
                if (ticTacToeArray[i].board[position].isEmpty()) {
                    ticTacToeArray[i].board[position] = player2
                    return true
                }
            }
        }
        return false
    }

    fun removeGame(matchId: Int) {
        for (i in ticTacToeArray.indices) {
            if (i == matchId) {
                ticTacToeArray.removeAt(i)
            }
        }
    }

    fun gameIsOver(boardPlaces: ArrayList<String>): Boolean {
        if (player1Won(boardPlaces) || player2Won(boardPlaces) || hasDraw(boardPlaces)) {
            return true
        }
        return false
    }

    private fun player2Won(boardPlaces: ArrayList<String>): Boolean {
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

    private fun player1Won(boardPlaces: ArrayList<String>): Boolean {
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

    private fun hasDraw(boardPlaces: ArrayList<String>): Boolean {
        for (i in boardPlaces.indices) {
            if (boardPlaces[i].isEmpty()) {
                return false
            }
        }
        return true
    }

}