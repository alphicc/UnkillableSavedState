package com.savestatesample.sampleScreen

import androidx.lifecycle.MutableLiveData
import com.stateViewModel.EmptyState
import com.state_view_model_annotations.Unkillable

@Unkillable
data class SampleFragmentState(
    val test: Int,
    val test2: MutableLiveData<String>,
    val test3: List<Int>,
    val test4: MyCustomClassSaved,
    var test5: Boolean
) : EmptyState()