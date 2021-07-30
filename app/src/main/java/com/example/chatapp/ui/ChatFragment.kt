package com.example.chatapp.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.adapters.ChatAdapter
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.models.Board
import com.example.chatapp.models.Message
import com.example.chatapp.objects.ConnectionFactory
import com.example.chatapp.utils.ProfileSharedProfile
import com.example.chatapp.utils.Utils
import com.example.chatapp.utils.Utils.hideSoftKeyboard
import com.google.android.material.snackbar.Snackbar


class ChatFragment : Fragment() {
    private lateinit var binding : FragmentChatBinding
    private val connectionFactory : ConnectionFactory by activityViewModels()
    private lateinit var adapter: ChatAdapter
    private var data = arrayListOf<Message>()
    private lateinit var profileName: String
    private val navController by lazy {
        findNavController()
    }
    private lateinit var snackbar: Snackbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProfileSharedProfile.getProfile {
            profileName = it
        }
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
        binding.constraintLayout.setOnClickListener {
            activity?.hideSoftKeyboard()
        }
        binding.tictactoe.setOnClickListener {
            sendInviteTicTacToe()
        }
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
        if(message.typeMesage == Message.SENT_MESSAGE){
            binding.messagesRecyclerview.scrollToPosition(data.size-1)
            return
        }
        binding.messagesRecyclerview.apply{
            if(!canScrollVertically(1)){
                scrollToPosition(data.size-1)
            }
        }
    }
    fun createDialog(view: View,context: Context) {



        var builder = AlertDialog.Builder(context).apply {
            setMessage("Deseja desafiar Fulano para uma partida de TicTacToe?")
            setPositiveButton("ok", DialogInterface.OnClickListener { dialog, which ->

                val bottomsheet = requireView().findViewById<View>(R.id.bottomSheetLayout)
                var bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior.peekHeight = 150
//                bottomSheetBehavior.isHideable = false


//                view.findNavController().navigate(R.id.action_chatFragment_to_bottomSheetFragment)
            })
            setNegativeButton("cancelar", DialogInterface.OnClickListener() { dialog: DialogInterface?, which: Int ->
                dialog?.dismiss()
            })

        }
        builder.create().show()


    }

    private fun validReceivedMessage(message: Message) {
        if (message.typeMesage == Message.INVITE_TICTACTOE) {
            if(message.message == "accepted"){
                snackbar.dismiss()
                val action = ChatFragmentDirections.actionChatFragmentToBottomSheetFragment(Board.O, true)
                navController.navigate(action)
                return
            }
            if(message.message == "declined"){
                snackbar.dismiss()
                snackbar = Snackbar.make(requireView(), "Jogador recusou seu convite", Snackbar.LENGTH_LONG)
                snackbar.show()
                return
            }
            receiveConviteTicTacToe(message.name)
            return
        }
        if (message.typeMesage != Message.NOTIFY_CHAT && message.typeMesage != Message.RECEIVE_PLAY) {
            message.typeMesage = Message.RECEIVED_MESSAGE
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

    private fun sendInviteTicTacToe(){
        ProfileSharedProfile.getProfile {
            val message = Message(it, "", Message.INVITE_TICTACTOE)
            connectionFactory.sendMessage(message){
                snackbar = Snackbar.make(requireView(), "Aguardando oponente aceitar a partida", Snackbar.LENGTH_LONG)
                snackbar.show()
            }
        }

    }

    private fun acceptInviteTicTacToe(){
        val action = ChatFragmentDirections.actionChatFragmentToBottomSheetFragment(Board.X, false)
        navController.navigate(action)
        val message = Message("", "accepted", Message.INVITE_TICTACTOE)
        connectionFactory.sendMessage(message){}
    }

    private fun declineInviteTicTacToe(){
        val message = Message("", "declined", Message.INVITE_TICTACTOE)
        connectionFactory.sendMessage(message){}
    }


}