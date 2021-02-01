package com.savestatesample.core

open class EmptyState {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T> newInstance(): T {
           return EmptyState() as T
        }
    }
}