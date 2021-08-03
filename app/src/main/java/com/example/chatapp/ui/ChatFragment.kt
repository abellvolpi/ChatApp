package com.example.chatapp.ui

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.models.Board
import com.example.chatapp.models.Cell
import com.example.chatapp.models.Message
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.hideSoftKeyboard
import com.example.chatapp.viewModel.ConnectionFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private val connectionFactory: ConnectionFactory by activityViewModels()
    private lateinit var adapter: ChatAdapter
    private var data = arrayListOf<Message>()
    private lateinit var bottomsheetForConfig: BottomSheetBehavior<View>
    private lateinit var profileName: String
    private val navController by lazy {
        findNavController()
    }
    private lateinit var snackbar: Snackbar

    //bottomsheet
    private val boardCells = Array(3) { arrayOfNulls<ImageButton>(3) } // Array de image button
    private var board = Board()
    private var canIPlay: Boolean = false
    private var player = ""
    private var isTicTacToePlayRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        ProfileSharedProfile.getProfile {
            profileName = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        
        setHasOptionsMenu(true)
        binding = FragmentChatBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.toolbar.inflateMenu(R.menu.chat_menu)


        super.onViewCreated(view, savedInstanceState)
        binding.constraintLayout.setOnClickListener {
            activity?.hideSoftKeyboard()
        }
        binding.tictactoe.setOnClickListener {
            sendInviteTicTacToe()
            if (!isTicTacToePlayRunning) {
                sendInviteTicTacToe()
            } else {
                Snackbar.make(
                    requireView(),
                    "The game has been started. To return, swipe up",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
        bottomsheetForConfig =
            BottomSheetBehavior.from(requireView().findViewById(R.id.bottom_sheet))
        bottomsheetForConfig.peekHeight = 150
    }

    private fun initView() {
        with(binding) {
            connectionFactory.startListenerMessages()
            connectionFactory.line.observe(viewLifecycleOwner) {
                if (it != null) {
                    val messageClass = Utils.JSONtoMessageClass(it)
                    validReceivedMessage(messageClass)
                    Log.e("Listener: ", it)
                } else {
                    val action =
                        com.example.chatapp.ui.ChatFragmentDirections.actionChatFragmentToHomeFragment(
                            "Server disconnected"
                        )
                    navController.navigate(action)
                }
            }

            buttonSend.setOnClickListener {
                if (messageField.text.isNotBlank()) {
                    val message =
                        Message(profileName, messageField.text.toString(), Message.SENT_MESSAGE)
                    connectionFactory.sendMessage(message) {
                        messageField.text.clear()
                        refreshUIChat(message)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Message cannot be blank",
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
            adapter = ChatAdapter(data)
            adapter.setHasStableIds(true)
            messagesRecyclerview.adapter = adapter
            messagesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun refreshUIChat(message: Message) {
        adapter.addData(message)
        if (message.typeMesage == Message.SENT_MESSAGE) {
            binding.messagesRecyclerview.scrollToPosition(data.size - 1)
            return
        }
        binding.messagesRecyclerview.apply {
            if (!canScrollVertically(1)) {
                scrollToPosition(data.size - 1)
            }
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
            binding.bottomSheet.whoPlay.text = "Sua vez de jogar. Você é ${player}"
            canIPlay = true
            verifyIfHasWinner()
            return
        }
        if (message.typeMesage == Message.INVITE_TICTACTOE) {
            if (message.message == "accepted") {
                refreshBoard()
                snackbar.dismiss()
                player = Board.O
                canIPlay = true
                initViewTicTacToe()
                bottomsheetForConfig.state = BottomSheetBehavior.STATE_EXPANDED
                return
            }
            if (message.message == "declined") {
                snackbar.dismiss()
                snackbar = Snackbar.make(
                    requireView(),
                    "Jogador recusou seu convite",
                    Snackbar.LENGTH_LONG
                )
                snackbar.show()
                return
            }
            receiveConviteTicTacToe(message.name)
            return
        }
        if (message.typeMesage != Message.NOTIFY_CHAT) {
            message.typeMesage = Message.RECEIVED_MESSAGE
            refreshUIChat(message)
        } else {
            refreshUIChat(message)
        }
    }

    private fun receiveConviteTicTacToe(name: String) {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setMessage("Aceitar uma partida de TicTacToe com ${name}?")
            setPositiveButton("ok") { dialog, which ->
                acceptInviteTicTacToe()
            }
            setNegativeButton("cancelar") { dialog: DialogInterface?, which: Int ->
                dialog?.dismiss()
                declineInviteTicTacToe()
            }
        }
        builder.show()
    }

    private fun sendInviteTicTacToe() {
        ProfileSharedProfile.getProfile {
            val message = Message(it, "", Message.INVITE_TICTACTOE)
            connectionFactory.sendMessage(message) {
                snackbar = Snackbar.make(
                    requireView(),
                    "Aguardando oponente aceitar a partida",
                    Snackbar.LENGTH_LONG
                )
                snackbar.show()
            }
        }
    }

    private fun acceptInviteTicTacToe() {
        refreshBoard()
        player = Board.X
        canIPlay = false
        initViewTicTacToe()
        bottomsheetForConfig.state = BottomSheetBehavior.STATE_EXPANDED
        val message = Message("", "accepted", Message.INVITE_TICTACTOE)
        connectionFactory.sendMessage(message) {}
    }

    private fun declineInviteTicTacToe() {
        val message = Message("", "declined", Message.INVITE_TICTACTOE)
        connectionFactory.sendMessage(message) {}
    }

    override fun onStart() {
        super.onStart()
        connectionFactory.getBackgroundMessages().forEach {
            validReceivedMessage(it)
        }
        connectionFactory.empyBackgroundMessages()
    }

    //bottom sheet functions
    private fun initViewTicTacToe() {
        isTicTacToePlayRunning = true
        with(binding.bottomSheet) {
            boardCells[0][0] = btn1
            boardCells[0][1] = btn2
            boardCells[0][2] = btn3
            boardCells[1][0] = btn4
            boardCells[1][1] = btn5
            boardCells[1][2] = btn6
            boardCells[2][0] = btn7
            boardCells[2][1] = btn8
            boardCells[2][2] = btn9
            if (canIPlay) {
                whoPlay.text = "Sua vez de jogar. Você é ${player}!"
            } else {
                whoPlay.text = "Esperando o oponente jogar..."
            }
            rematchButton.setOnClickListener {
                sendInviteTicTacToe()
            }
        }
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
                verifyIfHasWinner()
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
            binding.bottomSheet.whoPlay.text = "Esperando o oponente jogar..."
            canIPlay = false
            Log.e("Sent", "play")
            verifyIfHasWinner()
        }
    }

    private fun refreshBoard() {
        board = Board()
        mapBoardToUi()
    }

    private fun verifyIfHasWinner() {
        with(binding.bottomSheet) {
            if (board.gameOver()) {
                if (board.playerWon()) { //player O
                    whoPlay.text = "Player O wins!"
                    rematchButton.visibility = View.VISIBLE
                    if (player == Board.O) {
                        ProfileSharedProfile.getProfile {
                            val message =
                                Message("", "${it} winner TicTacToe!", Message.NOTIFY_CHAT)
                            connectionFactory.sendMessage(message) {}
                        }
                    }
                    return
                }
                if (board.opponentWon()) {
                    whoPlay.text = "Player X wins!"
                    rematchButton.visibility = View.VISIBLE
                    if (player == Board.X) {
                        ProfileSharedProfile.getProfile {
                            val message = Message("", "${it} winner TicTacToe!", Message.NOTIFY_CHAT)
                            connectionFactory.sendMessage(message) {}
                        }
                    }
                    return
                }
                whoPlay.text = "The game is DRAW!"
                rematchButton.visibility = View.VISIBLE
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.chat_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.perfil -> {
                findNavController().navigate(ChatFragmentDirections.actionChatFragmentToProfileFragment())
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}