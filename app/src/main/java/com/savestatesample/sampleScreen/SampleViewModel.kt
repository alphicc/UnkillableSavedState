package com.savestatesample.sampleScreen

import android.app.Application
import com.savestatesample.core.BaseViewModel
import com.savestatesample.core.EmptyState

class SampleViewModel(application: Application) : BaseViewModel<EmptyState>(application) {

    override fun provideState() = createState<EmptyState>()
}