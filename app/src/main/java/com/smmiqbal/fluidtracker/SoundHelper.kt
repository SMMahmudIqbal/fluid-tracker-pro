package com.smmiqbal.fluidtracker

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.concurrent.thread
import kotlin.math.sin
import kotlin.random.Random

object SoundHelper {

    /**
     * Plays customized synthesized sound tailored specifically to the fluid type:
     * - Water / Sparkling: Crystalline water droplet / effervescent fizz
     * - Tea / Herbal: Soothing warm harmonic tone
     * - Coffee / Protein: Resonant energizing chime
     * - Electrolytes / Citrus: Crisp vibrant splash
     */
    fun playDrinkSound(drinkId: String) {
        when (drinkId) {
            "sparkling" -> playSodaFizzSound()
            "tea", "herbal" -> playTeaPourSound()
            "coffee", "protein" -> playResonantChimeSound()
            "electrolytes", "citrus" -> playCrispSplashSound()
            else -> playWaterDropSound()
        }
    }

    // Synthesize gentle, bubbly water drop sound
    fun playWaterDropSound() {
        thread(isDaemon = true) {
            try {
                val sampleRate = 44100
                val durationMs = 130
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val freq = 450.0 + (700.0 * progress * progress)
                    val envelope = (1.0 - progress) * (1.0 - progress)
                    val sample = sin(2.0 * Math.PI * freq * t) * envelope
                    buffer[i] = (sample * Short.MAX_VALUE * 0.75).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    // Synthesize sparkling carbonated fizz
    fun playSodaFizzSound() {
        thread(isDaemon = true) {
            try {
                val sampleRate = 44100
                val durationMs = 280
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val envelope = if (progress < 0.15) {
                        progress / 0.15
                    } else {
                        (1.0 - progress) * (1.0 - progress)
                    }

                    val whiteNoise = (Random.nextDouble() * 2.0 - 1.0)
                    val popFreq = 300.0 * (1.0 - progress * 0.8)
                    val t = i.toDouble() / sampleRate
                    val popComponent = if (progress < 0.12) sin(2.0 * Math.PI * popFreq * t) * 0.7 else 0.0

                    val combined = (whiteNoise * 0.35 + popComponent) * envelope
                    buffer[i] = (combined.coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.75).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    // Synthesize warm fluid pour stream
    fun playTeaPourSound() {
        thread(isDaemon = true) {
            try {
                val sampleRate = 44100
                val durationMs = 260
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val envelope = sin(progress * Math.PI)

                    val tone1 = sin(2.0 * Math.PI * 523.25 * t) // C5
                    val tone2 = sin(2.0 * Math.PI * 659.25 * t) // E5
                    val tone3 = sin(2.0 * Math.PI * 783.99 * t) // G5
                    val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.15

                    val sample = (tone1 * 0.4 + tone2 * 0.3 + tone3 * 0.2 + noise) * envelope
                    buffer[i] = (sample * Short.MAX_VALUE * 0.65).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    // Synthesize crisp vibrant splash
    fun playCrispSplashSound() {
        thread(isDaemon = true) {
            try {
                val sampleRate = 44100
                val durationMs = 220
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val envelope = (1.0 - progress)

                    val freq = 880.0 + (300.0 * sin(progress * 15.0))
                    val tone = sin(2.0 * Math.PI * freq * t)
                    val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.2

                    val sample = (tone * 0.6 + noise) * envelope
                    buffer[i] = (sample * Short.MAX_VALUE * 0.7).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    // Synthesize resonant harmonic chime
    fun playResonantChimeSound() {
        thread(isDaemon = true) {
            try {
                val sampleRate = 44100
                val durationMs = 240
                val numSamples = (durationMs * sampleRate) / 1000
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples
                    val envelope = (1.0 - progress) * (1.0 - progress)

                    val f1 = sin(2.0 * Math.PI * 587.33 * t) // D5
                    val f2 = sin(2.0 * Math.PI * 880.00 * t) // A5
                    val sample = (f1 * 0.6 + f2 * 0.4) * envelope

                    buffer[i] = (sample * Short.MAX_VALUE * 0.7).toInt().toShort()
                }

                playBuffer(buffer, sampleRate)
            } catch (_: Exception) {}
        }
    }

    private fun playBuffer(buffer: ShortArray, sampleRate: Int) {
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackBufferSize = buffer.size.coerceAtLeast(minBufferSize)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackBufferSize * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        try {
            val sleepTimeMs = (buffer.size * 1000L / sampleRate) + 50L
            Thread.sleep(sleepTimeMs)
        } catch (_: InterruptedException) {
        } finally {
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    fun triggerHapticFeedback(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (vibrator.hasAmplitudeControl()) {
                    // Pre-drop tension (light), then heavy thud (water hitting surface)
                    val timings = longArrayOf(0, 15, 60, 40)
                    val amplitudes = intArrayOf(0, 40, 0, 255) // max amplitude at the end
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(50)
            }
        } catch (_: Exception) {}
    }
}
