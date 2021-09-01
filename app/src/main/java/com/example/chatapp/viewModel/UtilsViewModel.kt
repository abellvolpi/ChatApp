package com.example.chatapp.viewModel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UtilsViewModel : ViewModel() {
    private var isRunningAudio : Pair<Int, Boolean> = Pair(-1, false)
    private val audioRunning : MutableLiveData<Pair<Int, Boolean>> by lazy {
        MutableLiveData<Pair<Int, Boolean>>().also {
            isRunningAudio
        }
    }

    fun changeAudioRunning(boolean: Boolean, position: Int){
        isRunningAudio = Pair(position, boolean)
        audioRunning.postValue(isRunningAudio)
    }

    fun getHasAudioRunning(): LiveData<Pair<Int, Boolean>> {
        return audioRunning
    }
}