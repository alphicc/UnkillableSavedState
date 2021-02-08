package com.savestatesample.sampleScreen

import androidx.lifecycle.MutableLiveData
import com.stateViewModel.EmptyState
import com.state_view_model_annotations.Unkillable

@Unkillable
data class SampleFragmentState(
    val test: Map<List<Map<Int, List<Map<Float, Double>>>>, Int>,
    val test2: MutableLiveData<List<Map<List<Map<Int, List<Map<Float, Double>>>>, Int>>>,
    val test3: Double,
    val test4: MyCustomClassSaved,
    val test5: List<MyCustomClassSaved>,
    val test6: List<Int>
) : EmptyState()