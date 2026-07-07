package com.example.imageloader.utils

/**
 * Interface to provide the current system time.
 * Useful for mocking time in unit tests.
 */
interface TimeProvider {
    fun currentTimeMillis(): Long
}

/**
 * Default implementation of [TimeProvider] using [System.currentTimeMillis].
 */
object DefaultTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
