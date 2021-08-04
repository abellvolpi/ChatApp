package com.example.chatapp.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.hideSoftKeyboard
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.UtilsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException

class ChatFragment : Fragment() {
    private lateinit var output: String
    private lateinit var mediaRecorder: MediaRecorder
    private var state: Boolean = false
    private lateinit var binding: FragmentChatBinding
    private val connectionFactory: ConnectionFactory by activityViewModels()
    private lateinit var adapter: ChatAdapter
    private val data = arrayListOf<Message>()
    private lateinit var bottomsheetForConfig: BottomSheetBehavior<View>
    private lateinit var profileName: String
    private val utilsViewModel: UtilsViewModel by activityViewModels()
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
        profileName = ProfileSharedProfile.getProfile()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        initView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomsheetForConfig =
            BottomSheetBehavior.from(requireView().findViewById(R.id.bottom_sheet))
        bottomsheetForConfig.peekHeight = 150

    }


    private fun initView() {
        with(binding) {
            connectionFactory.startListenerMessages()
            connectionFactory.line.observe(viewLifecycleOwner) {
                if(it !=null){
                    if(it != ""){
                        val messageClass = Utils.JSONtoMessageClass(it)
                        validReceivedMessage(messageClass)
                        Log.e("Listener: ", it)
                    }
                } else {
                    val action =
                        com.example.chatapp.ui.ChatFragmentDirections.actionChatFragmentToHomeFragment(
                            "Server disconnected"
                        )
                    navController.navigate(action)
                }
            }
            constraintLayout.setOnClickListener {
                activity?.hideSoftKeyboard()
            }
            tictactoe.setOnClickListener {
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
            messageField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    if (s != null && s.isEmpty()) {
                        buttonSend.visibility = View.GONE
                        buttonVoiceMessageRecord.visibility = View.VISIBLE
                    } else {
                        buttonSend.visibility = View.VISIBLE
                        buttonVoiceMessageRecord.visibility = View.GONE
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            buttonVoiceMessageRecord.setOnClickListener {
                checkPermission(android.Manifest.permission.RECORD_AUDIO, RECORD_PERMISSION)
                if (!state) {
                    startRecording()
                } else {
                    stopRecording()
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
            adapter = ChatAdapter(data, utilsViewModel, viewLifecycleOwner)
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
        if (message.typeMesage == Message.SENT_MESSAGE_VOICE) {
            message.typeMesage = Message.RECEIVED_MESSAGE_VOICE
            refreshUIChat(message)
            return
        }
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
                val parent =
                    requireActivity().window.decorView.findViewById<View>(android.R.id.content)
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
            return
        }
        refreshUIChat(message)
    }

    private fun receiveConviteTicTacToe(name: String) {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setMessage("Aceitar uma partida de TicTacToe com $name?")
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
        val message = Message(profileName, "", Message.INVITE_TICTACTOE)
        connectionFactory.sendMessage(message) {
            snackbar = Snackbar.make(
                requireView(),
                "Aguardando oponente aceitar a partida",
                Snackbar.LENGTH_LONG
            )
            snackbar.show()
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
                whoPlay.text = "Sua vez de jogar. Você é $player!"
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

    inner class CellClickListener(private val i: Int, private val j: Int) :
        View.OnClickListener {
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
                        val message =
                            Message("", "$profileName winner TicTacToe!", Message.NOTIFY_CHAT)
                        connectionFactory.sendMessage(message) {}
                    }
                    return
                }
                if (board.opponentWon()) {
                    whoPlay.text = "Player X wins!"
                    rematchButton.visibility = View.VISIBLE
                    if (player == Board.X) {
                        val message =
                            Message("", "$profileName winner TicTacToe!", Message.NOTIFY_CHAT)
                        connectionFactory.sendMessage(message) {}
                    }
                    return
                }
                whoPlay.text = "The game is DRAW!"
                rematchButton.visibility = View.VISIBLE
                return
            }
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(permission),
                requestCode
            )
        }
    }

    private fun startRecording() {
        try {
            mediaRecorder = MediaRecorder()
            output =
                MainApplication.getContextInstance().cacheDir.absolutePath + "/recordVoice.mp3"
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            mediaRecorder.setOutputFile(output)
            mediaRecorder.prepare()
            mediaRecorder.start()
            state = true
            binding.buttonVoiceMessageRecord.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.layout_button_red)
            Toast.makeText(requireContext(), "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            checkPermission(android.Manifest.permission.RECORD_AUDIO, RECORD_PERMISSION)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.release()
        binding.progressBarSendMessage.visibility = View.VISIBLE
        state = false
        binding.buttonVoiceMessageRecord.background =
            ContextCompat.getDrawable(requireContext(), R.drawable.layout_button)
        Utils.parseAnythingToByteString(File(output)) {
            val message = Message("", it, typeMesage = Message.SENT_MESSAGE_VOICE)
            connectionFactory.sendMessage(message) {
                binding.progressBarSendMessage.visibility = View.GONE
                refreshUIChat(message)
            }
        }
    }

    companion object {
        private const val RECORD_PERMISSION = 102
    }

}