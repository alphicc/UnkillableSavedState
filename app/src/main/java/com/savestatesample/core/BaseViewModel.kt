package com.savestatesample.core

import android.app.Application
import androidx.lifecycle.AndroidViewModel

open class BaseViewModel<T : EmptyState>(
    application: Application,
    var state: T = EmptyState.newInstance()
) : AndroidViewModel(application) {

}