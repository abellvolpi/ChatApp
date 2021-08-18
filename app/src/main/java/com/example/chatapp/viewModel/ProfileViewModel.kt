package com.example.chatapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chatapp.models.Profile
import com.example.chatapp.room.profile.controller.ProfileController
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ProfileViewModel : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Job() + Dispatchers.Main
    private val controller = ProfileController()
    lateinit var profiles: MutableLiveData<ArrayList<Profile>>

    init {
        allProfiles {
            profiles = MutableLiveData(it)
        }
    }

    private fun allProfiles(onResult: (ArrayList<Profile>) -> Unit) {
        launch(Dispatchers.IO) {
            val getAll = controller.getAll()
            withContext(Dispatchers.Main) {
                onResult.invoke(getAll)
            }
        }
    }

    fun deleteProfile(id: Int) {
        launch(Dispatchers.IO) {
            controller.delete(id)
        }
        allProfiles {
            profiles.postValue(it)
        }
    }

    fun insert(profile: Profile) {
        launch(Dispatchers.IO) {
            controller.insert(profile)
            profiles.postValue(arrayListOf(profile))
        }
    }

    fun deleteAll() {
        launch(Dispatchers.IO) {
            controller.deleteAll()
        }
    }

    fun getProfile(id: String, onResult: (Profile?) -> Unit) {
        launch(Dispatchers.IO) {
            onResult.invoke(controller.getById(id))
        }
    }

    fun updateProfile(profile: Profile) {
        launch(Dispatchers.IO) {
            controller.update(profile)
        }
    }

}