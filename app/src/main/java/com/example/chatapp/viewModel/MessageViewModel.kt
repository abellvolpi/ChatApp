package com.example.chatapp.viewModel

import androidx.lifecycle.ViewModel
import com.example.chatapp.models.Message
import com.example.chatapp.room.message.controller.MessageController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MessageViewModel : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Job()+Dispatchers.Main
    private val messageController by lazy {
        MessageController
    }

    fun getAllMessages(onResult: (List<Message>) -> Unit){
        var listAll = listOf<Message>()
        launch(Dispatchers.IO){
           listAll = messageController.getAll()
            withContext(Dispatchers.Main){
                onResult.invoke(listAll)
            }
        }
    }
    fun insertMessage(message: Message){
        launch(Dispatchers.IO){
            messageController.insert(message)
        }
    }
    fun deleteAll(onResult : () -> Unit){
        launch(Dispatchers.IO){
            messageController.deleteAll()
            withContext(Dispatchers.Main){
                onResult.invoke()
            }
        }
    }
}