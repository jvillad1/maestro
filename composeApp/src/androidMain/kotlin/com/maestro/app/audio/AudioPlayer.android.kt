package com.maestro.app.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

actual class AudioPlayer actual constructor() {
    actual fun playClick(isAccent: Boolean) {
        val sampleRate = 44100
        val samples = sampleRate * 80 / 1000
        val freq = if (isAccent) 1200.0 else 800.0
        val buffer = ShortArray(samples) { i ->
            val envelope = 1.0 - (i.toDouble() / samples)
            (sin(2 * PI * freq * i.toDouble() / sampleRate) * envelope * Short.MAX_VALUE * if (isAccent) 0.4 else 0.25).toInt().toShort()
        }
        val track = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(sampleRate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(buffer, 0, buffer.size)
        track.play()
    }
}
