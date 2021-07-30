package com.example.chatapp.models

import android.media.AsyncPlayer

class Board {

    companion object {
        const val O = "O"
        const val X = "X"
    }

    val boardplaces = Array(3) { arrayOfNulls<String>(3) }

    val availableCells: List<Cell>
        // função dentro da variável a qual irá definir o valor available cells (por isso o return)
        get() {
            val cells = arrayListOf<Cell>()
            for (i in boardplaces.indices) {
                for (j in boardplaces.indices) {
                    if (boardplaces[i][j].isNullOrEmpty()) {
                        cells.add(Cell(i, j))
                    }
                }
            }
            return cells
        }


    fun placeMove(cell: Cell, player: String) {  //função para alocar um movimento em uma célula específica
        boardplaces[cell.i][cell.j] = player
    }

    fun opponentWon(): Boolean {
        if (
            (boardplaces[0][0] == boardplaces[1][1] && boardplaces[1][1] == boardplaces[2][2] && boardplaces[0][0] == X) ||
            (boardplaces[0][2] == boardplaces[1][1] && boardplaces[1][1] == boardplaces[2][0] && boardplaces[2][0] == X)
        ) {
            return true
        }
        for (i in boardplaces.indices) {
            if (
                (boardplaces[i][0] == boardplaces[i][1] && boardplaces[i][1] == boardplaces[i][2] && boardplaces[i][0] == X) ||
                (boardplaces[0][i] == boardplaces[1][i] && boardplaces[0][i] == boardplaces[2][i] && boardplaces[0][i] == X)
            ) {
                return true
            }
        }
        return false
    }

    fun playerWon(): Boolean {
        if (
            (boardplaces[0][0] == boardplaces[1][1] && boardplaces[1][1] == boardplaces[2][2] && boardplaces[0][0] == O) ||
            (boardplaces[0][2] == boardplaces[1][1] && boardplaces[1][1] == boardplaces[2][0] && boardplaces[2][0] == O)
        ) {
            return true
        }
        for (i in boardplaces.indices) {
            if (
                (boardplaces[i][0] == boardplaces[i][1] && boardplaces[i][1] == boardplaces[i][2] && boardplaces[i][0] == O) ||
                (boardplaces[0][i] == boardplaces[1][i] && boardplaces[0][i] == boardplaces[2][i] && boardplaces[0][i] == O)
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