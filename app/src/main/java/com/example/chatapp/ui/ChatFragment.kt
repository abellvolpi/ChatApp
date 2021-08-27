package com.example.chatapp.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
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
import com.example.chatapp.models.Profile
import com.example.chatapp.utils.Extensions.hideSoftKeyboard
import com.example.chatapp.utils.MainApplication
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.viewModel.ConnectionFactory
import com.example.chatapp.viewModel.MessageViewModel
import com.example.chatapp.viewModel.ProfileViewModel
import com.example.chatapp.viewModel.UtilsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ChatFragment : Fragment() {
    private lateinit var output: String
    private lateinit var mediaRecorder: MediaRecorder
    private var state: Boolean = false
    private lateinit var binding: FragmentChatBinding
    private val connectionFactory: ConnectionFactory by activityViewModels()
    private lateinit var adapter: ChatAdapter
    private val data = arrayListOf<Message>()
    private var isHistoryCall = false
    private lateinit var bottomSheetForConfig: BottomSheetBehavior<View>
    private lateinit var startActivityLaunch: ActivityResultLauncher<String>
    private lateinit var profileName: String
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val messageViewModel: MessageViewModel by activityViewModels()
    private val profileId: Int
        get() {
            return ProfileSharedProfile.getIdProfile()
        }
    private val utilsViewModel: UtilsViewModel by activityViewModels()
    private val navController by lazy {
        findNavController()
    }
    private lateinit var snackbar: Snackbar
    private var joinMessage: Message? = null


    private val boardCells = Array(3) { arrayOfNulls<ImageButton>(3) } // Array de image button
    private var board = Board()
    private var canIPlay: Boolean = false
    private var player = ""
    private var isTicTacToePlayRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        profileName = ProfileSharedProfile.getProfile()
        startActivityLaunch = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            with(binding) {
                if (uri != null) {
                    sentImageFrameLayout.visibility = View.VISIBLE
                    buttonClip.visibility = View.GONE
                    sentImage.setImageURI(uri)
                    buttonSend.visibility = View.VISIBLE
                    buttonVoiceMessageRecord.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        joinMessage = arguments?.getSerializable("joinMessage") as Message?
        isHistoryCall = arguments?.getBoolean("isHistoryCall") ?: false
        initView()
        if (connectionFactory.isFirstAccessInThisFragment() && joinMessage != null) {
            sendMessageSocket(joinMessage!!)
            connectionFactory.setFirstAccessChatFragment(false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetForConfig =
            BottomSheetBehavior.from(requireView().findViewById(R.id.bottom_sheet))
        bottomSheetForConfig.peekHeight = 150
    }

    private fun readMessageMissed() {
        val messagesClass = arrayListOf<Pair<Message, String>>()
        val messages = connectionFactory.isRead.iterator()

        while (messages.hasNext()) {
            val message = messages.next()
            if (connectionFactory.lastLine != message) {
                messagesClass.add(Pair(Utils.jsonToMessageClass(message), message))
                messages.remove()
            }
        }
        messagesClass.sortedBy { it.first.time }.forEach {
            validReceivedMessage(it.first)
            connectionFactory.lastLine = it.second
        }
    }

    private fun initView() {
        readMessageMissed()
        with(binding) {
            adapter = ChatAdapter(data, utilsViewModel, viewLifecycleOwner, false)
            messagesRecyclerview.adapter = adapter
            messagesRecyclerview.layoutManager = LinearLayoutManager(requireContext())
            if (!isHistoryCall) {
                readMessageMissed()
                connectionFactory.startListenerMessages()
                connectionFactory.line.observe(viewLifecycleOwner) {
                    if (it != null) {
                        if (connectionFactory.lastLine != it.second) {
                            validReceivedMessage(it.first)
                            connectionFactory.lastLine = it.second
                            connectionFactory.isRead.remove(it.second)
                        }
                    }
                }
                connectionFactory.serverOnline.observe(viewLifecycleOwner) {
                    if (it == false) {
                        Log.e("Chat disconnected", "server down")
//                    val action =
//                        ChatFragmentDirections.actionChatFragmentToHomeFragment("Server Stopped")
//                    navController.navigate(action)
                    }
                }
                constraintLayout.setOnClickListener {
                    activity?.hideSoftKeyboard()
                }
                tictactoe.setOnClickListener {
                    sendInviteTicTacToe()
                    bottomSheet.whoPlay.visibility = View.VISIBLE
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
                            buttonClip.visibility = View.VISIBLE
                        } else {
                            sentImageFrameLayout.visibility = View.GONE
                            buttonSend.visibility = View.VISIBLE
                            buttonVoiceMessageRecord.visibility = View.GONE
                            buttonClip.visibility = View.GONE

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

                buttonClip.setOnClickListener {
                    startActivityLaunch.launch("image/*")
                    buttonSend.visibility = View.VISIBLE
                    buttonVoiceMessageRecord.visibility = View.GONE
                }
                closeImageButton.setOnClickListener {
                    restartUI()
                }

                buttonSend.setOnClickListener {
                    when {
                        sentImageFrameLayout.visibility == View.VISIBLE -> {
                //       val bitmap = sentImage.drawable.toBitmap()
                            val base64 = Utils.bitmapToByteArray3(sentImage.drawable)
                            val message = Message(
                                Message.MessageType.IMAGE.code,
                                id = profileId,
                                base64Data = base64,
                                text = null,
                                username = profileName
                            )
                            sendMessageSocket(message)
                            restartUI()

                        }
                        messageField.text.isNotBlank() -> {
                            val message =
                                Message(
                                    Message.MessageType.MESSAGE.code,
                                    username = profileName,
                                    text = messageField.text.toString(),
                                    id = profileId,
                                    base64Data = null
                                )
                            sendMessageSocket(message)
                            messageField.text.clear()
                        }
                        else -> {
                            Toast.makeText(
                                requireContext(),
                                R.string.message_blank,
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                    }
                }
                inflateToolBarOptions()
            } else {
                startHistoryMode()
            }
        }
    }

    private fun sendMessageSocket(message: Message) {
        connectionFactory.sendMessageToSocket(message) {}
        if(message.type != Message.MessageType.JOIN.code) {
            refreshUIChatAndSaveMessageInToRoom(message)
        }
    }

    private fun refreshUiChat(message: Message) {
        adapter.addData(message)
        if (message.status == Message.MessageStatus.SENT.code) {
            binding.messagesRecyclerview.scrollToPosition(data.size - 1)
        } else {
            binding.messagesRecyclerview.apply {
                if (!canScrollVertically(1)) {
                    scrollToPosition(data.size - 1)
                }
            }
        }

    }

    private fun refreshUIChatAndSaveMessageInToRoom(message: Message) {
        when (message.type) {
            Message.MessageType.AUDIO.code -> {
                Utils.saveMessageAudioByteToCacheDir(message) {
                    message.base64Data = it
                    refreshUiChat(message)
                    messageViewModel.insertMessage(message)
                }
            }
            Message.MessageType.IMAGE.code -> {
                Utils.saveMessageImageByteToCacheDirPNG(message) {
                    message.base64Data = it
                    refreshUiChat(message)
                    messageViewModel.insertMessage(message)
                }
            }
            Message.MessageType.JOIN.code ->{
                refreshUiChat(message)
                messageViewModel.insertMessage(message)
            }
            else -> {
                refreshUiChat(message)
                messageViewModel.insertMessage(message)
            }
        }
    }

    private fun returnToHomeFragmentWithMessage(message: String) {
        val action =
            ChatFragmentDirections.actionChatFragmentToHomeFragment(message)
        navController.navigate(action)
    }

    private fun validReceivedMessage(messageReceived: Message) {
        with(messageReceived) {
            if (type == Message.MessageType.REVOKED.code) {
                when (id) {
                    1 -> {
                        val action =
                            ChatFragmentDirections.actionChatFragmentToWritePasswordDialog()
                        navController.navigate(action)
                    }
                    2 -> {
                        returnToHomeFragmentWithMessage("Server Security Kick")
                    }
                    3 -> returnToHomeFragmentWithMessage("Admin kicked you")
                }
                return
            }
            if (type == Message.MessageType.ACKNOWLEDGE.code) {
                if (id != null) {
                    ProfileSharedProfile.saveIdProfile(id)
                    empyHistoryCache()
                    if (text != null) {
                        profileViewModel.deleteAll {
                            Utils.listJsonToProfiles(text)?.forEach { profile ->
                                if (connectionFactory.getIpHost() == Utils.getIpAddress()) {
                                    profile.isAdmin = true
                                }
                                if (profile.photoProfile != "" || profile.photoProfile != null) {
                                    saveAvatarToCacheDir(
                                        profile.id,
                                        profile.photoProfile ?: ""
                                    ) {
                                        profile.photoProfile = it
                                        profile.isMemberYet = true
                                        profileViewModel.insert(profile)
                                    }
                                } else {
                                    profile.photoProfile = ""
                                    profile.isMemberYet = true
                                    profileViewModel.insert(profile)
                                }
                            }
                        }
                    }
                }
                return
            }

            if (type == Message.MessageType.JOIN.code) {
                if (id != null) {
                    if (id == profileId) {
                        saveAvatarToCacheDir(id, join?.avatar ?: "") {
                            val profile =
                                Profile(id, username ?: "", it, 0, true, join?.isAdmin)
//                            profileViewModel.insert(profile)
                            refreshUIChatAndSaveMessageInToRoom(this)
                        }
                    } else {
                        if (join?.avatar != "" || join?.avatar != null) {
                            saveAvatarToCacheDir(id, join?.avatar ?: "") {
                                val profile =
                                    Profile(id, username ?: "", it, 0, true, join?.isAdmin)
                                profileViewModel.insert(profile)
//                                refreshUIChatAndSaveMessageInToRoom(this)
                                refreshUIChatAndSaveMessageInToRoom(this)
                            }
                        } else {
                            val profile = Profile(id, username ?: "", "", 0, true, join.isAdmin)
                            profileViewModel.insert(profile)
//                            refreshUIChatAndSaveMessageInToRoom(this)
                            refreshUIChatAndSaveMessageInToRoom(this)
                        }
                    }
                } else {
                    Log.e("chatNotRefresh", "an error occurred because id is null")
                    Log.e("database", "error when insert profile, id is null")
                }
                return
            }

            if (type == Message.MessageType.LEAVE.code) {
                refreshUIChatAndSaveMessageInToRoom(this)
                if (id != null) {
                    profileViewModel.getProfile(id.toString()) {
                        if (it != null) {
                            it.isMemberYet = false
                            profileViewModel.updateProfile(it)
                        } else {
                            Log.e(
                                "database",
                                "error when update profile because ID doesn't exists"
                            )
                        }
                    }
                } else {
                    Log.e(
                        "database",
                        "error when delete profile because id from server is null"
                    )
                }
                return
            }
            if (id == profileId) {
                if (status == Message.MessageStatus.RECEIVED.code) {
                    status = Message.MessageStatus.SENT.code
                }
            } else {
                status = Message.MessageStatus.RECEIVED.code
            }

            when (status) {
                Message.MessageStatus.SENT.code -> {
//                    when (type) {
////                        Message.MessageType.MESSAGE.code -> {
////                            refreshUIChatAndSaveMessageInToRoom(this)
////                        }
////                        Message.MessageType.AUDIO.code -> {
////                            refreshUIChatAndSaveMessageInToRoom(this)
////                        }
////                        Message.MessageType.IMAGE.code -> {
////                            refreshUIChatAndSaveMessageInToRoom(this)
////                        }
//                    }
                }

                Message.MessageStatus.RECEIVED.code -> {
                    when (type) {
                        Message.MessageType.MESSAGE.code -> {
                            refreshUIChatAndSaveMessageInToRoom(this)
                            Log.i("received", "Received Message")
                        }
                        Message.MessageType.AUDIO.code -> {
                            refreshUIChatAndSaveMessageInToRoom(this)
                        }
                        Message.MessageType.IMAGE.code -> {
                            refreshUIChatAndSaveMessageInToRoom(this)
                        }
                        Message.MessageType.TICPLAY.code -> {
                            Log.e("Received", "play")
                            val i = text?.split(",")?.get(0)?.toInt() ?: -1
                            val j = text?.split(",")?.get(1)?.toInt() ?: -1
                            val playerReceived = text?.split(",")?.get(2) ?: ""
                            val cell = Cell(i, j)
                            board.placeMove(cell, playerReceived)
                            mapBoardToUi()
                            binding.bottomSheet.whoPlay.text =
                                getString(R.string.your_move, player)
                            canIPlay = true
                            verifyIfHasWinner()
                        }
                        Message.MessageType.TICINVITE.code -> {
                            receiveInviteTicTacToe(username ?: "Error username")
                        }
                        else -> refreshUIChatAndSaveMessageInToRoom(this)
                    }
                }
                else -> false
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

    private fun empyHistoryCache() {
        File(
            MainApplication.getContextInstance().cacheDir.absolutePath,
            "/photosProfile"
        ).apply {
            if (exists()) {
                listFiles()?.forEach {
                    it.delete()
                }
            }
        }

        File(MainApplication.getContextInstance().cacheDir.absolutePath, "audios").apply {
            if (exists()) {
                listFiles()?.forEach {
                    it.delete()
                }
            }
        }
        File(MainApplication.getContextInstance().cacheDir.absolutePath, "images").apply {
            if (exists()) {
                listFiles()?.forEach {
                    it.delete()
                }
            }
        }
    }

    private fun saveAvatarToCacheDir(id: Int, string: String, onResult: (String) -> Unit) {
        val context = MainApplication.getContextInstance()
        val output =
            File(context.cacheDir.absolutePath + "/photosProfile", "profilePhoto_${id}.jpg")
        val base64 = Base64.decode(string, Base64.NO_WRAP)
        output.parentFile?.mkdirs()
        val fos = FileOutputStream(output)
        fos.write(base64)
        fos.flush()
        fos.close()
        onResult.invoke(output.absolutePath)
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
        val message = Message(
            Message.MessageType.TICINVITE.code,
            text = null,
            id = profileId,
            base64Data = null,
            username = profileName
        )
        sendMessageSocket(message)
        snackbar = Snackbar.make(
            requireView(),
            getString(R.string.waiting_accept),
            Snackbar.LENGTH_LONG
        )
        snackbar.show()
    }

    private fun acceptInviteTicTacToe() {
        binding.bottomSheet.bottomSheetLayout.visibility = View.VISIBLE
        refreshBoard()
        player = Board.X
        canIPlay = false
        initViewTicTacToe()
        bottomSheetForConfig.state = BottomSheetBehavior.STATE_EXPANDED
        val message = Message(
            Message.MessageType.TICINVITE.code,
            text = "accepted",
            id = profileId,
            base64Data = null,
            username = profileName
        )
        sendMessageSocket(message)
    }

    private fun declineInviteTicTacToe() {
        val message = Message(
            Message.MessageType.TICINVITE.code,
            text = "declined",
            id = profileId,
            base64Data = null,
            username = profileName
        )
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
        val messagePlay = Message(
            Message.MessageType.TICPLAY.code,
            text = "${i},${j},${player}",
            id = profileId,
            base64Data = null,
            username = profileName
        )
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
            val message = Message(
                Message.MessageType.AUDIO.code,
                id = profileId,
                base64Data = it,
                text = null,
                username = profileName
            )
            sendMessageSocket(message)
            binding.progressBarSendMessage.visibility = View.GONE
        }
    }

    private fun restartUI() {
        with(binding) {
            buttonClip.visibility = View.VISIBLE
            sentImageFrameLayout.visibility = View.GONE
            buttonSend.visibility = View.GONE
            buttonVoiceMessageRecord.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val RECORD_PERMISSION = 102
    }

    private fun startHistoryMode() {
        with(binding) {
            chatToolbar.title = getString(R.string.history_title)
            layout.visibility = View.GONE
            centerProgressBar.visibility = View.VISIBLE
            messagesRecyclerview.alpha = 0.5f
            messageViewModel.getAllMessages {
                val arrayList = arrayListOf<Message>()
                arrayList.addAll(it)
                adapter = ChatAdapter(arrayList, utilsViewModel, viewLifecycleOwner, true)
                messagesRecyclerview.adapter = adapter
                messagesRecyclerview.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    scrollToPosition(it.size - 1)
                }
                centerProgressBar.visibility = View.GONE
                messagesRecyclerview.alpha = 1f
                chatToolbar.apply {
                    navigationIcon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.back_icon)
                    chatToolbar.setNavigationOnClickListener {
                        val action = ChatFragmentDirections.actionChatFragmentToHomeFragment("")
                        navController.navigate(action)
                    }
                }
            }
        }
    }

    private fun inflateToolBarOptions() {
        with(binding) {
            chatToolbar.apply {
                setOnClickListener {
                    navController.navigate(ChatFragmentDirections.actionChatFragmentToChatDetailsFragment())
                }
                overflowIcon =
                    AppCompatResources.getDrawable(requireContext(), R.drawable.ic_more_vert)
                inflateMenu(R.menu.chat_menu)
                setOnMenuItemClickListener { item ->
                    when (item?.itemId) {
                        R.id.perfil -> {
                            navController.navigate(ChatFragmentDirections.actionChatFragmentToProfileFragment())
                        }
                        R.id.share_link -> {
                            navController.navigate(
                                ChatFragmentDirections.actionChatFragmentToShareLinkBottomSheetDialogFragment(
                                    connectionFactory.getIpHost(),
                                    connectionFactory.getIpPort().toInt()
                                )
                            )
                        }
                    }
                    true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionFactory.setFirstAccessChatFragment(false)
    }
}