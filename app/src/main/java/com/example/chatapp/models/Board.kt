package com.example.chatapp.models

class Board {

    companion object {
        const val O = "O"
        const val X = "X"
    }

    val boardPlaces = Array(3) { arrayOfNulls<String>(3) }

    private val availableCells: List<Cell>
        get() {
            val cells = arrayListOf<Cell>()
            for (i in boardPlaces.indices) {
                for (j in boardPlaces.indices) {
                    if (boardPlaces[i][j].isNullOrEmpty()) {
                        cells.add(Cell(i, j))
                    }
                }
            }
            return cells
        }


    fun placeMove(cell: Cell, player: String) {
        boardPlaces[cell.i][cell.j] = player
    }

    fun opponentWon(): Boolean {
        if (
            (boardPlaces[0][0] == boardPlaces[1][1] && boardPlaces[1][1] == boardPlaces[2][2] && boardPlaces[0][0] == X) ||
            (boardPlaces[0][2] == boardPlaces[1][1] && boardPlaces[1][1] == boardPlaces[2][0] && boardPlaces[2][0] == X)
        ) {
            return true
        }
        for (i in boardPlaces.indices) {
            if (
                (boardPlaces[i][0] == boardPlaces[i][1] && boardPlaces[i][1] == boardPlaces[i][2] && boardPlaces[i][0] == X) ||
                (boardPlaces[0][i] == boardPlaces[1][i] && boardPlaces[0][i] == boardPlaces[2][i] && boardPlaces[0][i] == X)
            ) {
                return true
            }
        }
        return false
    }

    fun playerWon(): Boolean {
        if (
            (boardPlaces[0][0] == boardPlaces[1][1] && boardPlaces[1][1] == boardPlaces[2][2] && boardPlaces[0][0] == O) ||
            (boardPlaces[0][2] == boardPlaces[1][1] && boardPlaces[1][1] == boardPlaces[2][0] && boardPlaces[2][0] == O)
        ) {
            return true
        }
        for (i in boardPlaces.indices) {
            if (
                (boardPlaces[i][0] == boardPlaces[i][1] && boardPlaces[i][1] == boardPlaces[i][2] && boardPlaces[i][0] == O) ||
                (boardPlaces[0][i] == boardPlaces[1][i] && boardPlaces[0][i] == boardPlaces[2][i] && boardPlaces[0][i] == O)
            ) {
                return true
            }
        }
        return false
    }

    fun gameOver(): Boolean {
        if (opponentWon() || playerWon() || availableCells.isEmpty()) {
            return true
        }
        return false
    }

}