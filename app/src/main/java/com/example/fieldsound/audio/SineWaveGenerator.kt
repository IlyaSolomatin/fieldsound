package com.example.fieldsound.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Process
import kotlin.math.sin

class SineWaveGenerator {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val AMPLITUDE = 0.8f
        private const val BUFFER_FRAMES = 512
        private const val SMOOTHING = 0.002
    }

    @Volatile
    private var targetFrequency = 100f

    private var currentFrequency = 100.0
    private var phase = 0.0

    @Volatile
    private var isPlaying = false
    private var audioTrack: AudioTrack? = null
    private var playbackThread: Thread? = null

    fun setFrequency(hz: Float) {
        targetFrequency = hz
    }

    fun start(initialFrequency: Float) {
        if (isPlaying) return

        targetFrequency = initialFrequency
        currentFrequency = initialFrequency.toDouble()
        phase = 0.0
        isPlaying = true

        val minBufferBytes = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        val bufferBytes = maxOf(BUFFER_FRAMES * Float.SIZE_BYTES, minBufferBytes)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .build()
            )
            .setBufferSizeInBytes(bufferBytes)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            .build()

        audioTrack = track
        track.play()

        playbackThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val buffer = FloatArray(BUFFER_FRAMES)
            while (isPlaying) {
                fillBuffer(buffer)
                track.write(buffer, 0, buffer.size, AudioTrack.WRITE_BLOCKING)
            }
            track.stop()
            track.release()
        }, "SineWaveThread").also { it.start() }
    }

    fun stop() {
        isPlaying = false
        playbackThread?.join(1000)
        playbackThread = null
        audioTrack = null
    }

    private fun fillBuffer(buffer: FloatArray) {
        val target = targetFrequency.toDouble()
        for (i in buffer.indices) {
            currentFrequency += (target - currentFrequency) * SMOOTHING
            phase += 2.0 * Math.PI * currentFrequency / SAMPLE_RATE
            if (phase > 2.0 * Math.PI) {
                phase -= 2.0 * Math.PI
            }
            buffer[i] = (AMPLITUDE * sin(phase)).toFloat()
        }
    }
}
