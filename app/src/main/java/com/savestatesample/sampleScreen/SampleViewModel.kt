package com.savestatesample.sampleScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.stateViewModel.stateViewModel.AndroidStateViewModel

class SampleViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidStateViewModel<UnkillableSampleFragmentState>(application, savedStateHandle) {

    //val text: MutableLiveData<Int?> = MutableLiveData(state.test)

    override fun provideState() = createState<UnkillableSampleFragmentState>()

    init {
        val t: Int? = savedStateHandle.get("11")
        Log.d("Alpha", "tesss ${t}")
    }

    fun onSetTextClicked() {
        savedStateHandle.set("11", 321)
        //state.test = 1
        //text.value = state.test
    }
}