package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class FeedbackHelper(context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var lastToneTime = 0L
    private var lastPopTime = 0L
    private var lastVibrateTime = 0L
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {
            toneGenerator = null
        }

        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            vibrator = null
        }
    }

    /**
     * Synthesizes a realistic firecracker pop / snap sound ("صوت فرقعة / طرقعة")
     * using AudioTrack with an impulsive transient burst followed by decaying resonant frequency.
     */
    fun playPopSound(pitchHz: Double = 320.0, intensity: Float = 1.0f) {
        val now = System.currentTimeMillis()
        if (now - lastPopTime < 80) return
        lastPopTime = now

        scope.launch {
            try {
                val sampleRate = 44100
                val durationMs = 120
                val numSamples = (sampleRate * durationMs) / 1000
                val buffer = ShortArray(numSamples)
                val random = Random()

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    // Sharp exponential decay envelope: e^(-t * 40)
                    val envelope = exp(-t * 40.0)
                    // Low resonant pop thud mixed with transient white noise crackle
                    val thud = sin(2.0 * PI * pitchHz * exp(-t * 25.0) * t)
                    val noise = (random.nextDouble() * 2.0 - 1.0)
                    val sample = ((thud * 0.55 + noise * 0.45) * envelope * 32767.0 * intensity).coerceIn(-32767.0, 32767.0)
                    buffer[i] = sample.toInt().toShort()
                }

                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )

                val audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
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
                        .setBufferSizeInBytes(maxOf(minBufSize, buffer.size * 2))
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        maxOf(minBufSize, buffer.size * 2),
                        AudioTrack.MODE_STATIC
                    )
                }

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                delay(durationMs.toLong() + 20)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Fallback to ToneGenerator if AudioTrack encounters device audio constraint
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 60)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Plays a series of quick pop waves ("موجات فرقعات متتالية وتروح")
     * like firecracker sparks fading away as the magnet passes.
     */
    fun playCrackleWaveBurst() {
        scope.launch {
            playPopSound(pitchHz = 420.0, intensity = 1.0f)
            delay(55)
            playPopSound(pitchHz = 280.0, intensity = 0.85f)
            delay(65)
            playPopSound(pitchHz = 350.0, intensity = 0.65f)
            delay(75)
            playPopSound(pitchHz = 200.0, intensity = 0.40f)
        }
        triggerWaveVibration()
    }

    /**
     * Vibrates in expanding wave pulses ("موجات كده اهوت وتروح")
     */
    fun triggerWaveVibration() {
        val now = System.currentTimeMillis()
        if (now - lastVibrateTime < 400) return
        lastVibrateTime = now

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Wave pattern: strong peak, medium secondary wave, light trailing wave
                val timings = longArrayOf(0, 80, 50, 120, 60, 90, 50, 40)
                val amplitudes = intArrayOf(0, 255, 0, 200, 0, 140, 0, 60)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 80, 50, 120, 60, 90), -1)
            }
        } catch (_: Exception) {}
    }

    fun playGeigerClick(fieldStrength: Float, threshold: Float) {
        val now = System.currentTimeMillis()
        val ratio = (fieldStrength / threshold).coerceIn(0.1f, 2.0f)
        val intervalMs = (380 / ratio).toLong().coerceIn(60L, 750L)

        if (now - lastToneTime >= intervalMs) {
            lastToneTime = now
            playPopSound(pitchHz = 400.0, intensity = 0.5f)
        }
    }

    fun triggerAlarmHaptic() {
        triggerWaveVibration()
    }

    fun triggerClickHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
