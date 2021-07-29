package com.example.chatapp.ui.bottomsheets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentBottomSheetBinding
import com.example.chatapp.models.Board
import com.example.chatapp.models.Cell
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding
    private val boardCells = Array(3) { arrayOfNulls<ImageButton>(3) } // Array de image button
    var board = Board()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBottomSheetBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBoard()

    }

    private fun linkButtons() {
        with(binding) {
            boardCells[0][0] = btn1
            boardCells[0][1] = btn2
            boardCells[0][2] = btn3
            boardCells[1][0] = btn4
            boardCells[1][1] = btn5
            boardCells[1][2] = btn6
            boardCells[2][0] = btn7
            boardCells[2][1] = btn8
            boardCells[2][2] = btn9
        }
    }

    private fun loadBoard() {
        linkButtons()
        callClickListener()

    }

    private fun callClickListener() {
        for (i in boardCells.indices){
            for (j in boardCells.indices){
                boardCells[i][j]?.setOnClickListener(CellClickListener(i,j))
            }
        }
    }
    inner class CellClickListener(private val i: Int, private val j: Int): View.OnClickListener{
        override fun onClick(v: View?) {
            if(!board.gameOver()){
                val cell = Cell(i,j)
                board.placeMove(cell,Board.PLAYER)
            }
            mapBoardToUi()
        }

    }

    private fun mapBoardToUi(){
        for (i in board.boardplaces.indices){
            for (j in board.boardplaces.indices){
                when(board.boardplaces[i][j]){
                    Board.PLAYER -> {
                        boardCells[i][j]?.setImageResource(R.drawable.ic_circle)
                        boardCells[i][j]?.isEnabled = false
                    }
                    Board.OPPONENT -> {
                        boardCells[i][j]?.setImageResource(R.drawable.ic_x)
                        boardCells[i][j]?.isEnabled = false
                    }
                    //servirá para limpar o board após o restart
                    else -> {
                        boardCells[i][j]?.setImageResource(0)
                        boardCells[i][j]?.isEnabled = true
                    }
                }
            }
        }
  }




}