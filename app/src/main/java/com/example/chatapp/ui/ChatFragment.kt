package com.example.chatapp.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.example.chatapp.room.message.controller.MessageController
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.hideSoftKeyboard
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.UtilsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private lateinit var bottomSheetForConfig: BottomSheetBehavior<View>
    private lateinit var profileName: String
    private val profileId: Int by lazy {
        ProfileSharedProfile.getIdProfile()
    }
    private val utilsViewModel: UtilsViewModel by activityViewModels()
    private val navController by lazy {
        findNavController()
    }
    private lateinit var snackbar: Snackbar
    private lateinit var joinMessage: Message

    //bottomsheet
    private val boardCells = Array(3) { arrayOfNulls<ImageButton>(3) } // Array de image button
    private var board = Board()
    private var canIPlay: Boolean = false
    private var player = ""
    private var isTicTacToePlayRunning = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        setHasOptionsMenu(true)
        profileName = ProfileSharedProfile.getProfile()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        joinMessage = arguments?.getSerializable("joinMessage") as Message
        sendMessageSocket(joinMessage)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetForConfig =
            BottomSheetBehavior.from(requireView().findViewById(R.id.bottom_sheet))
        bottomSheetForConfig.peekHeight = 150
    }

    private fun initView() {
        with(binding) {
            connectionFactory.startListenerMessages()
            connectionFactory.line.observe(viewLifecycleOwner) {
                if (it != null) {
                    if (it != "error") {
                        val jsonMessage = Utils.jsonToMessageClass(it)
                        validReceivedMessage(jsonMessage)
                        Log.e("Listener: ", it)
                    }
                } else {
                    val action =
                        com.example.chatapp.ui.ChatFragmentDirections.actionChatFragmentToHomeFragment(
                            Message.ACTION_DISCONNECTED
                        )
                    navController.navigate(action)
                }
            }
            connectionFactory.serverOnline.observe(viewLifecycleOwner) {
                if (it == false) {
                    val action = ChatFragmentDirections.actionChatFragmentToHomeFragment("Server Stopped")
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
                        R.string.game_started,
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
                        Message(Message.MessageType.MESSAGE.code, username = profileName, text = messageField.text.toString(), id = profileId, base64Data = null)
                    sendMessageSocket(message)
                    messageField.text.clear()
                } else {
                    Toast.makeText(
                        requireContext(),
                        R.string.message_blank,
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

    private fun sendMessageSocket(message: Message) {
        connectionFactory.sendMessageToSocket(message) {}

        CoroutineScope(Dispatchers.IO).launch {
            MessageController.insert(message)
            MessageController.getAll().forEach {
                Log.d("test", it.toString())
            }
        }

    }

    private fun refreshUIChat(message: Message) {
        adapter.addData(message)
        if (message.status == Message.MessageStatus.SENT.code) {
            binding.messagesRecyclerview.scrollToPosition(data.size - 1)
            return
        }
        binding.messagesRecyclerview.apply {
            if (!canScrollVertically(1)) {
                scrollToPosition(data.size - 1)
            }
        }
    }

    private fun validReceivedMessage(messageReceived: Message) {
        with(messageReceived) {
            if (type == Message.MessageType.REVOKED.code) {
                var message = ""
                when (id) {
                    1 -> message = "Wrong Password"
                    2 -> message = "Server Security Kick"
                    3 -> message = "Admin kicked you"
                }
                val action =
                    ChatFragmentDirections.actionChatFragmentToHomeFragment(message)
                navController.navigate(action)
                return@with
            }
            if (type == Message.MessageType.ACKNOWLEDGE.code) {
                ProfileSharedProfile.saveIdProfile(id ?: 0)
                return
            }

            if (id == profileId) {
                if (status == Message.MessageStatus.RECEIVED.code) {
                    status = Message.MessageStatus.SENT.code
                }
            }
            if (type == Message.MessageType.JOIN.code) {
                refreshUIChat(this)
                return@with
            }

            when (status) {
                Message.MessageStatus.SENT.code -> {
                    when (type) {
                        Message.MessageType.MESSAGE.code -> {
                            refreshUIChat(this)
                        }
                        Message.MessageType.AUDIO.code -> {
                            refreshUIChat(this)
                        }
                    }
                }

                Message.MessageStatus.RECEIVED.code -> {
                    when (type) {
                        Message.MessageType.MESSAGE.code -> {
                            refreshUIChat(this)
                        }
                        Message.MessageType.AUDIO.code -> {
                            refreshUIChat(this)
                        }
                        Message.MessageType.TICPLAY.code -> {
                            Log.e("Received", "play")
                            val i = text?.split(",")?.get(0)?.toInt() ?: -1
                            val j = text?.split(",")?.get(1)?.toInt() ?: -1
                            val playerReceived = text?.split(",")?.get(2) ?: ""
                            val cell = Cell(i, j)
                            board.placeMove(cell, playerReceived)
                            mapBoardToUi()
                            binding.bottomSheet.whoPlay.text = getString(R.string.your_move, player)
                            canIPlay = true
                            verifyIfHasWinner()
                        }
                        Message.MessageType.TICINVITE.code -> {
                            receiveInviteTicTacToe(username ?: "Error username")
                        }
                    }
                }
            }

//            when(type){
//                Message.INVITE_TICTACTOE -> {
//                    if (message == "accepted") {
//                        refreshBoard()
//                        snackbar.dismiss()
//                        player = Board.O
//                        canIPlay = true
//                        initViewTicTacToe()
//                        bottomSheetForConfig.state = BottomSheetBehavior.STATE_EXPANDED
//                        return
//                    }
//                    if (message == "declined") {
//                        snackbar.dismiss()
//                        val parent =
//                            requireActivity().window.decorView.findViewById<View>(android.R.id.content)
//                        snackbar = Snackbar.make(
//                            requireView(),
//                            R.string.invite_rejected.toString(),
//                            Snackbar.LENGTH_LONG
//                        )
//                        snackbar.show()
//                    }
//                }
//                Message.NOTIFY_CHAT -> {
//                    refreshUIChat(this)
//                }
//            }
        }
    }

    private fun receiveInviteTicTacToe(name: String) {
        val builder = AlertDialog.Builder(requireContext()).apply {
            setMessage(getString(R.string.invite_received, name))
            setPositiveButton("ok") { _, _ ->
                acceptInviteTicTacToe()
            }
            setNegativeButton(R.string.cancel) { dialog: DialogInterface?, _: Int ->
                dialog?.dismiss()
                declineInviteTicTacToe()
            }
        }
        builder.show()
    }

    private fun sendInviteTicTacToe() {
        val message = Message(Message.MessageType.TICINVITE.code, text = null, id = profileId, base64Data = null, username = profileName)
        sendMessageSocket(message)
        snackbar = Snackbar.make(
            requireView(),
            getString(R.string.waiting_accept),
            Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }

    private fun acceptInviteTicTacToe() {
        refreshBoard()
        player = Board.X
        canIPlay = false
        initViewTicTacToe()
        bottomSheetForConfig.state = BottomSheetBehavior.STATE_EXPANDED
        val message = Message(Message.MessageType.TICINVITE.code, text = "accepted", id = profileId, base64Data = null, username = profileName)
        sendMessageSocket(message)
    }

    private fun declineInviteTicTacToe() {
        val message = Message(Message.MessageType.TICINVITE.code, text = "declined", id = profileId, base64Data = null, username = profileName)
        sendMessageSocket(message)
    }

    override fun onStart() {
        super.onStart()
        connectionFactory.getBackgroundMessages().forEach {
            validReceivedMessage(it)
        }
        connectionFactory.emptyBackgroundMessages()
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
                whoPlay.text = getString(R.string.your_move, player)
            } else {
                whoPlay.text = getString(R.string.waiting_move)
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
        for (i in board.boardPlaces.indices) {
            for (j in board.boardPlaces.indices) {
                when (board.boardPlaces[i][j]) {
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
        val messagePlay = Message(Message.MessageType.TICPLAY.code, text = "${i},${j},${player}", id = profileId, base64Data = null, username = profileName)
        sendMessageSocket(messagePlay)
        binding.bottomSheet.whoPlay.text = getString(R.string.waiting_move)
        canIPlay = false
        Log.e("Sent", "play")
        verifyIfHasWinner()
    }

    private fun refreshBoard() {
        board = Board()
        mapBoardToUi()
    }

    private fun verifyIfHasWinner() {
        with(binding.bottomSheet) {
            if (board.gameOver()) {
                if (board.playerWon()) { //player O
                    whoPlay.text = getString(R.string.player_o_wins)
                    rematchButton.visibility = View.VISIBLE
                    if (player == Board.O) { // verifica se eu sou o jogador O
                        // adicionar pontos ao placar do jogador
                    }
                    return
                }
                if (board.opponentWon()) {
                    whoPlay.text = getString(R.string.player_x_wins)
                    rematchButton.visibility = View.VISIBLE
                    if (player == Board.X) { // verifica se eu sou o jogador X
                        //adicionar ponto ao placar do jogador
                    }
                    return
                }
                whoPlay.text = getString(R.string.draw)
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
            Toast.makeText(
                requireContext(),
                getString(R.string.recording_audio),
                Toast.LENGTH_SHORT
            ).show()
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
            val message = Message(Message.MessageType.AUDIO.code, id = profileId, base64Data = it, text = null, username = profileName)
            sendMessageSocket(message)
            binding.progressBarSendMessage.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.chat_menu, menu)
        super.onCreateOptionsMenu(menu, menuInflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.perfil -> {
                findNavController().navigate(ChatFragmentDirections.actionChatFragmentToProfileFragment())
                return true
            }
            R.id.share_link -> {
                val ip = connectionFactory.getIpHost()
                val port = connectionFactory.getIpPort()
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "http://www.mychatapp.com/home/$ip:$port")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareIntent, ""))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val RECORD_PERMISSION = 102
    }
}