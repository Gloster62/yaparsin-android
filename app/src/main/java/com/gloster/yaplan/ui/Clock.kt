package com.gloster.yaplan.ui

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Ticks every minute so day-dependent screens roll over at midnight while open. */
fun minuteTicks(): Flow<Long> = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(60_000)
    }
}
