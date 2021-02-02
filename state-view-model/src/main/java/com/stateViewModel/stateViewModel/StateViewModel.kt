package com.stateViewModel.stateViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.stateViewModel.EmptyState
import com.stateViewModel.STATE_ARE_NOT_INITIALIZED

abstract class StateViewModel<T : EmptyState>(application: Application) :
    AndroidViewModel(application) {

    init {
        @Suppress("LeakingThis") provideState()
    }

    var state: T
        get() = _state ?: throw IllegalStateException(STATE_ARE_NOT_INITIALIZED)
        set(value) {
            _state = value
        }

    private var _state: T? = null

    abstract fun provideState()

    protected inline fun <reified K : T> createState() {
        state = K::class.java.newInstance()
    }
}