package com.example.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

object SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private const val SAMPLE_RATE = 11025 // Smaller sample rate for fast generation / low overhead

    fun playTone(frequency: Double, durationMs: Int, volume: Float = 0.5f) {
        scope.launch {
            try {
                val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
                if (numSamples <= 0) return@launch
                val sample = DoubleArray(numSamples)
                val generatedSND = ByteArray(2 * numSamples)

                for (i in 0 until numSamples) {
                    // Generates sine wave
                    sample[i] = sin(2 * Math.PI * i / (SAMPLE_RATE / frequency))
                }

                var idx = 0
                for (dVal in sample) {
                    val valInt = (dVal * 32767).toInt()
                    generatedSND[idx++] = (valInt and 0x00ff).toByte()
                    generatedSND[idx++] = ((valInt and 0xff00) ushr 8).toByte()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(generatedSND.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(generatedSND, 0, generatedSND.size)
                audioTrack.setVolume(volume)
                audioTrack.play()
                delay(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                // Fail-silent on devices with unsupported audio specs
            }
        }
    }

    fun playCoinSound() {
        scope.launch {
            playTone(880.0, 60, 0.3f)
            delay(65)
            playTone(1320.0, 100, 0.3f)
        }
    }

    fun playCrashSound() {
        scope.launch {
            var currentFreq = 300.0
            for (i in 0..5) {
                playTone(currentFreq, 40, 0.5f)
                currentFreq -= 50.0
                delay(30)
            }
        }
    }

    fun playNitroSound() {
        scope.launch {
            playTone(150.0, 100, 0.4f)
            delay(50)
            playTone(350.0, 150, 0.4f)
        }
    }

    fun playSirenSound() {
        scope.launch {
            playTone(600.0, 150, 0.2f)
            delay(160)
            playTone(900.0, 150, 0.2f)
        }
    }
}
