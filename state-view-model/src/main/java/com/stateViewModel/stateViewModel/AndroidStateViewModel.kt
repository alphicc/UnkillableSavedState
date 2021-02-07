package com.stateViewModel.stateViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import com.stateViewModel.EmptyState
import com.stateViewModel.STATE_ARE_NOT_INITIALIZED

abstract class AndroidStateViewModel<T : EmptyState>(
    application: Application,
    val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

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
        Log.d("Alpha", "createState ${savedStateHandle.hashCode()}")
        state = K::class.java.getDeclaredConstructor(SavedStateHandle::class.java).newInstance(savedStateHandle)
        //state = K::class.java.newInstance()
       // state.setStateHandle(savedStateHandle)
    }
}