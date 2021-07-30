package com.example.chatapp.ui.bottomsheets

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentBottomSheetBinding
import com.example.chatapp.models.Board
import com.example.chatapp.models.Cell
import com.example.chatapp.models.Message
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.utils.Utils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding
    private val boardCells = Array(3) { arrayOfNulls<ImageButton>(3) } // Array de image button
    var board = Board()
    private var player = ""
    private var canIPlay = false
    private val connectionFactory : ConnectionFactory by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = arguments?.getString("boardPlayer") as String
        canIPlay = arguments?.getBoolean("canIPlay") as Boolean
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        initView()
        connectionFactory.startListenerMessages()
        connectionFactory.line.observe(viewLifecycleOwner) {
            Log.e("reading", "sheet")
            if (it != null) {
                val messageClass = Utils.JSONtoMessageClass(it)
                validReceivedMessage(messageClass)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadBoard()
    }

    private fun initView() {
        if (canIPlay) {
            binding.whoPlay.text = "Sua vez de jogar. Você é ${player}!"
        } else {
            binding.whoPlay.text = "Esperando o oponente jogar..."
        }
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
        for (i in boardCells.indices) {
            for (j in boardCells.indices) {
                boardCells[i][j]?.setOnClickListener(CellClickListener(i, j))
            }
        }
    }

    inner class CellClickListener(private val i: Int, private val j: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            if (!board.gameOver() && canIPlay) {
                val cell = Cell(i, j)
                board.placeMove(cell, player)
                sendPlay(i, j)
            }
            mapBoardToUi()
        }
    }

    private fun mapBoardToUi() {
        for (i in board.boardplaces.indices) {
            for (j in board.boardplaces.indices) {
                when (board.boardplaces[i][j]) {
                    Board.O -> {
                        boardCells[i][j]?.setImageResource(R.drawable.ic_circle)
                        boardCells[i][j]?.isEnabled = false
                    }
                    Board.X -> {
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

    private fun sendPlay(i: Int, j: Int) {
        val messagePlay = Message("", "${i},${j},${player}", Message.RECEIVE_PLAY)
        connectionFactory.sendMessage(messagePlay) {
            binding.whoPlay.text = "Esperando o oponente jogar..."
            canIPlay = false
            Log.e("Sent", "play")
        }
    }

    private fun validReceivedMessage(message: Message) {
        if (message.typeMesage == Message.RECEIVE_PLAY) {
            Log.e("Received", "play")
            val i = message.message.split(",")[0].toInt()
            val j = message.message.split(",")[1].toInt()
            val playerReceived = message.message.split(",")[2]
            val cell = Cell(i, j)
            board.placeMove(cell, playerReceived)
            mapBoardToUi()
            binding.whoPlay.text = "Sua vez de jogar. Você é ${player}"
            canIPlay = true
        }
    }
}