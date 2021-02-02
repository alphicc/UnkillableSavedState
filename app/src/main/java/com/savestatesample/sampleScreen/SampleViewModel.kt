package com.savestatesample.sampleScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.stateViewModel.EmptyState
import com.stateViewModel.stateViewModel.AndroidStateViewModel

class SampleViewModel(
    application: Application,
    private val savedState: SavedStateHandle
) : AndroidStateViewModel<EmptyState>(application, savedState) {

    val text : MutableLiveData<String> = savedState.getLiveData("TEXT")

    override fun provideState() = createState<EmptyState>()

    init {
        val res = savedState.getLiveData<String>("TEXT")
        Log.d("Alpha", "init ${text.value} ${res.value}")
    }

    fun onSetTextClicked() {
        text.value = "SavedText"
        savedState.set("TEXT", text.value)
    }
}