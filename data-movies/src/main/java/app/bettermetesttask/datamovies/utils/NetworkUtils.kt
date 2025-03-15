package app.bettermetesttask.datamovies.utils

import kotlinx.coroutines.delay

suspend fun <T> withRetry(
    maxAttempts: Int = 3,
    initialDelayMillis: Long = 1000,
    maxDelayMillis: Long = 5000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMillis
    repeat(maxAttempts - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (attempt == maxAttempts - 2) throw e

            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
        }
    }
    return block()
}