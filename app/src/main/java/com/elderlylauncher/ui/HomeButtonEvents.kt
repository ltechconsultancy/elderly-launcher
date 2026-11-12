package com.elderlylauncher.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Counts presses of the Android Home button while Klaro is the launcher. */
object HomeButtonEvents {
    private val _presses = MutableStateFlow(0)
    val presses: StateFlow<Int> = _presses.asStateFlow()

    fun pressed() {
        _presses.value = _presses.value + 1
    }
}
