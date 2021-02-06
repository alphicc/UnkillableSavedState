package com.savestatesample.sampleScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.SavedStateHandle
import com.stateViewModel.stateViewModel.AndroidStateViewModel

class SampleViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidStateViewModel<SampleFragmentState>(application, savedStateHandle) {

    val text : MutableLiveData<String> = savedStateHandle.getLiveData("TEXT")

    init {
        Log.d("Alpha", "SampleViewModel init ${text.value} ${savedStateHandle.hashCode()}")
       // savedStateHandle.getLiveData<>()
    }

    override fun provideState() = createState<SampleFragmentState>()

    fun onSetTextClicked() {
        text.value = "SavedText"
        savedStateHandle.set("TEXT", text.value)
        val test = savedStateHandle.get<String>("TEXT")
    }
}