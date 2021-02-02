package com.savestatesample.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel

abstract class BaseViewModel<T : EmptyState>(application: Application) :
    AndroidViewModel(application) {

    companion object {
        private const val STATE_ARE_NOT_INITIALIZED =
            "State are not initialized. Call the createState() method inside the provideState() method to initialize"
    }

    init {
        @Suppress("LeakingThis") provideState()
    }

    @PublishedApi
    internal var state: T
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