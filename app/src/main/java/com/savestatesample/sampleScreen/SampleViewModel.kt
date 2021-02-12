package com.savestatesample.sampleScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import com.stateViewModel.stateViewModel.AndroidStateViewModel

class SampleViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidStateViewModel<UnkillableSampleFragmentState>(application, savedStateHandle) {

    override fun provideState() = createState<UnkillableSampleFragmentState>()

    init {
        Log.d("StateLog", "0 value ${state.testValue}")
        Log.d("StateLog", "1 value ${state.testLiveData?.value}")
    }

    fun onSetDataClicked() {
        state.testValue = 2.2
        state.updateTestLiveDataValue(3.3)
    }
}