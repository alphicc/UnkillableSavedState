package com.stateViewModel.stateViewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.stateViewModel.EmptyState
import com.stateViewModel.STATE_ARE_NOT_INITIALIZED

abstract class StateViewModel<T : EmptyState>(val savedStateHandle: SavedStateHandle) :
    ViewModel() {

    var state: T
        get() {
            if (_state == null) provideState()
            return _state ?: throw IllegalStateException(STATE_ARE_NOT_INITIALIZED)
        }
        set(value) {
            _state = value
        }

    private var _state: T? = null

    abstract fun provideState()

    protected inline fun <reified K : T> createState() {
        state = K::class.java.getDeclaredConstructor(SavedStateHandle::class.java)
            .newInstance(savedStateHandle)
    }
}