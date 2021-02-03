package com.stateViewModel

import androidx.lifecycle.SavedStateHandle

abstract class EmptyState {

    private var savedStateHandle: SavedStateHandle? = null

    protected fun getStateHandle(): SavedStateHandle? =
        savedStateHandle// ?: throw IllegalArgumentException(STATE_HANDLE_NOT_INITIALIZED)

    @PublishedApi
    internal fun setStateHandle(savedStateHandle: SavedStateHandle) {
        this.savedStateHandle = savedStateHandle
    }
}