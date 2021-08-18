package com.example.chatapp.viewModel

import androidx.lifecycle.ViewModel
import com.example.chatapp.models.Profile
import com.example.chatapp.room.profile.controller.ProfileController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ProfileViewModel : ViewModel(), CoroutineScope{
    override val coroutineContext: CoroutineContext = Job()+Dispatchers.Main
    private val controller = ProfileController()

    private fun allProfiles(onResult : (List<Profile>) -> Unit) {
        launch(Dispatchers.IO){
            val getAll = controller.getAll()
            withContext(Dispatchers.Main){
                onResult.invoke(getAll)
            }
        }
    }

    fun deleteProfile(id: Int) {
        launch(Dispatchers.IO) {
            controller.delete(id)
        }
    }

    fun insert(profile: Profile) {
        launch(Dispatchers.IO) {
            controller.insert(profile)
        }
    }
    fun deleteAll(){
        launch(Dispatchers.IO) {
            controller.deleteAll()
        }
    }
}