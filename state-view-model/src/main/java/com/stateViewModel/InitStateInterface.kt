package com.stateViewModel

import androidx.lifecycle.SavedStateHandle

interface InitStateInterface {
    fun provideValues(savedStateHandle: SavedStateHandle)
}