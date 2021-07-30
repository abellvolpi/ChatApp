package com.example.chatapp.ui.bottomsheets

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.activityViewModels
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentBottomSheetBinding
import com.example.chatapp.models.Board
import com.example.chatapp.models.Cell
import com.example.chatapp.models.Message
import com.example.chatapp.ui.ChatFragment
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragment(private var player: String, private var canIPlay: Boolean, val chatFragment: ChatFragment) {


//    private val connectionFactory : ConnectionFactory by chatFragment.activityViewModels()
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//    ): View {
//        binding
//        initViewTicTacToe()
//        connectionFactory.startListenerMessages()
//        connectionFactory.line.observe(chatFragment.viewLifecycleOwner) {
//            Log.e("reading", "sheet")
//            if (it != null) {
//                val messageClass = Utils.JSONtoMessageClass(it)
//                validReceivedMessage(messageClass)
//            }
//        }
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//    }
//
//    private fun initViewTicTacToe() {
//        with(binding){
//            boardCells[0][0] = btn1
//            boardCells[0][1] = btn2
//            boardCells[0][2] = btn3
//            boardCells[1][0] = btn4
//            boardCells[1][1] = btn5
//            boardCells[1][2] = btn6
//            boardCells[2][0] = btn7
//            boardCells[2][1] = btn8
//            boardCells[2][2] = btn9
//            if (canIPlay) {
//                whoPlay.text = "Sua vez de jogar. Você é ${player}!"
//            } else {
//                whoPlay.text = "Esperando o oponente jogar..."
//            }
//        }
//        callClickListener()
//    }
//
//    private fun callClickListener() {
//        for (i in boardCells.indices) {
//            for (j in boardCells.indices) {
//                boardCells[i][j]?.setOnClickListener(CellClickListener(i, j))
//            }
//        }
//    }
//
//    inner class CellClickListener(private val i: Int, private val j: Int) : View.OnClickListener {
//        override fun onClick(v: View?) {
//            if (!board.gameOver() && canIPlay) {
//                val cell = Cell(i, j)
//                board.placeMove(cell, player)
//                sendPlay(i, j)
//            }
//            mapBoardToUi()
//        }
//    }
//
//    private fun mapBoardToUi() {
//        for (i in board.boardplaces.indices) {
//            for (j in board.boardplaces.indices) {
//                when (board.boardplaces[i][j]) {
//                    Board.O -> {
//                        boardCells[i][j]?.setImageResource(R.drawable.ic_circle)
//                        boardCells[i][j]?.isEnabled = false
//                    }
//                    Board.X -> {
//                        boardCells[i][j]?.setImageResource(R.drawable.ic_x)
//                        boardCells[i][j]?.isEnabled = false
//                    }
//                    //servirá para limpar o board após o restart
//                    else -> {
//                        boardCells[i][j]?.setImageResource(0)
//                        boardCells[i][j]?.isEnabled = true
//                    }
//                }
//            }
//        }
//    }
//
//    private fun sendPlay(i: Int, j: Int) {
//        val messagePlay = Message("", "${i},${j},${player}", Message.RECEIVE_PLAY)
//        connectionFactory.sendMessage(messagePlay) {
//            binding.whoPlay.text = "Esperando o oponente jogar..."
//            canIPlay = false
//            Log.e("Sent", "play")
//        }
//    }
//
//    private fun validReceivedMessage(message: Message) {
//        if (message.typeMesage == Message.RECEIVE_PLAY) {
//            Log.e("Received", "play")
//            val i = message.message.split(",")[0].toInt()
//            val j = message.message.split(",")[1].toInt()
//            val playerReceived = message.message.split(",")[2]
//            val cell = Cell(i, j)
//            board.placeMove(cell, playerReceived)
//            mapBoardToUi()
//            binding.whoPlay.text = "Sua vez de jogar. Você é ${player}"
//            canIPlay = true
//        }
//    }
}