package io.github.kurenairyu.compose.core

class EventLocker {

    private var value: Boolean = false

    fun lock() {
        value = false
    }

    fun unlock() {
        value = true
    }

    fun isLocked(): Boolean {
        return value
    }
}