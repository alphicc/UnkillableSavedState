package com.savestatesample.sampleScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.stateViewModel.stateViewModel.AndroidStateViewModel

class SampleViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidStateViewModel<UnkillableSampleFragmentState>(application, savedStateHandle) {

    //val text: MutableLiveData<Int?> = MutableLiveData(state.test)

    override fun provideState() = createState<UnkillableSampleFragmentState>()

    init {
        //  val t: Int? = savedStateHandle.get("11")
        Log.d("Alpha", "0 tesss ${state.test3}")
        Log.d("Alpha", "1 tesss ${savedStateHandle.get<Int>("Key")}")
    }

    fun onSetTextClicked() {
        state.tabPosition = 2

        // savedStateHandle.set("11", 321)
        state.test3 = 1.1
        savedStateHandle.set("Key", 2)

        //state.test = 1
        //text.value = state.test
    }
}