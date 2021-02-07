package com.savestatesample.sampleScreen

import androidx.lifecycle.MutableLiveData
import com.stateViewModel.EmptyState
import com.state_view_model_annotations.Unkillable

@Unkillable
data class SampleFragmentState(
    val test: List<List<Int>>
) : EmptyState()