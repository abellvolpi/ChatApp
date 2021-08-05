package com.example.chatapp.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UtilsViewModel : ViewModel() {
    private var isRunning : Pair<Int, Boolean> = Pair(-1, false)
    private val audioRunning : MutableLiveData<Pair<Int, Boolean>> by lazy {
        MutableLiveData<Pair<Int, Boolean>>().also {
            isRunning
        }
    }

    fun changeAudioRunning(boolean: Boolean, position: Int){
        isRunning = Pair(position, boolean)
        audioRunning.postValue(isRunning)
    }

    fun getHasAudioRunning(): LiveData<Pair<Int, Boolean>> {
        return audioRunning
    }

}