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
    var profiles: MutableLiveData<ArrayList<Profile>> = MutableLiveData()
    lateinit var ranking: MutableLiveData<ArrayList<Profile>>

    init {
//        allProfilesWhereIsMemberYet {
//            profiles = MutableLiveData(it)
//        }
        getRanking {
            ranking = MutableLiveData(it)
        }
    }

    private fun allProfilesWhereIsMemberYet(onResult: (ArrayList<Profile>) -> Unit) {
        launch(Dispatchers.IO) {
            val getAll = controller.getProfileWhereIsMemberYet()
            withContext(Dispatchers.Main) {
                onResult.invoke(getAll)
            }
        }
    }

    fun deleteProfile(id: Int) {
        launch(Dispatchers.IO) {
            controller.delete(id)
        }
        allProfilesWhereIsMemberYet {
            profiles.postValue(it)
        }
    }

    fun insert(profile: Profile) {
        launch(Dispatchers.IO) {
            controller.insert(profile)
            val list = profiles.value?: arrayListOf()
            list.add(profile)
            profiles.postValue(list)
        }
    }

    fun deleteAll(onResult: () -> Unit) {
        launch(Dispatchers.IO) {
            controller.deleteAll()
            withContext(Dispatchers.Main){
                onResult.invoke()
            }
        }
        allProfilesWhereIsMemberYet {
            profiles.postValue(it)
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
            withContext(Dispatchers.Main){
                allProfilesWhereIsMemberYet {
                    profiles.postValue(it)
                }
            }
        }
    }

    fun getRanking(onResult: (ArrayList<Profile>) -> Unit){
        ranking = MutableLiveData()
        launch(Dispatchers.IO) {
            val ranking = controller.getRanking()
            withContext(Dispatchers.Main) {
                onResult.invoke(ranking as ArrayList<Profile>)
            }
        }
    }


}