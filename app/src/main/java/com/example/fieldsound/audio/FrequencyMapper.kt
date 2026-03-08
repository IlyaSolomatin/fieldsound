package com.example.fieldsound.audio

object FrequencyMapper {
    private const val MIN_UT = 0f
    private const val MAX_UT = 200f
    private const val MIN_HZ = 100f
    private const val MAX_HZ = 4000f

    fun map(microTesla: Float): Float {
        val clamped = microTesla.coerceIn(MIN_UT, MAX_UT)
        return MIN_HZ + (clamped - MIN_UT) / (MAX_UT - MIN_UT) * (MAX_HZ - MIN_HZ)
    }
}
