package com.prem.skudo.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TimerManager {
    private var timerJob: Job? = null
    private val _seconds = MutableStateFlow(0L)
    val seconds = _seconds.asStateFlow()

    fun start(initialSeconds: Long = 0, onTick: (Long) -> Unit) {
        _seconds.value = initialSeconds
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                delay(1000)
                _seconds.value += 1
                onTick(_seconds.value)
            }
        }
    }

    fun stop() {
        timerJob?.cancel()
    }

    fun reset() {
        stop()
        _seconds.value = 0
    }
}
