package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.model.MagnetReading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.sqrt

class MagnetometerManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    val hardwareSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val hasHardwareSensor: Boolean = hardwareSensor != null

    private val _readingFlow = MutableStateFlow(MagnetReading())
    val readingFlow: StateFlow<MagnetReading> = _readingFlow.asStateFlow()

    private val _isSimulating = MutableStateFlow(!hasHardwareSensor)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private var smoothedX = 0f
    private var smoothedY = 0f
    private var smoothedZ = 0f
    private var baselineOffset = 0f

    // Smoothing factor (0 = infinite smooth, 1 = raw instant)
    private val alpha = 0.35f

    private var simulationJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun startListening() {
        if (hasHardwareSensor && !_isSimulating.value) {
            sensorManager?.registerListener(
                this,
                hardwareSensor,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            startSimulationLoop()
        }
    }

    fun stopListening() {
        if (hasHardwareSensor) {
            sensorManager?.unregisterListener(this)
        }
        stopSimulationLoop()
    }

    fun toggleSimulation(forceSim: Boolean? = null) {
        val next = forceSim ?: !_isSimulating.value
        _isSimulating.value = next
        if (next) {
            if (hasHardwareSensor) {
                sensorManager?.unregisterListener(this)
            }
            startSimulationLoop()
        } else {
            stopSimulationLoop()
            if (hasHardwareSensor) {
                sensorManager?.registerListener(
                    this,
                    hardwareSensor,
                    SensorManager.SENSOR_DELAY_GAME
                )
            }
        }
    }

    fun setSimulationStrength(simulatedTotalMicroTesla: Float) {
        if (_isSimulating.value) {
            val ratio = simulatedTotalMicroTesla / sqrt(3f)
            val reading = MagnetReading(
                x = ratio * 0.9f,
                y = ratio * 1.1f,
                z = ratio * 0.95f,
                magnitude = simulatedTotalMicroTesla,
                rawMagnitude = simulatedTotalMicroTesla
            )
            _readingFlow.value = reading
        }
    }

    fun calibrateTare() {
        // Zero-out current reading as baseline
        baselineOffset = _readingFlow.value.rawMagnitude
    }

    fun resetCalibration() {
        baselineOffset = 0f
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD || _isSimulating.value) return

        val rawX = event.values[0]
        val rawY = event.values[1]
        val rawZ = event.values[2]

        // Low-pass filter for smooth gauge movement
        smoothedX += alpha * (rawX - smoothedX)
        smoothedY += alpha * (rawY - smoothedY)
        smoothedZ += alpha * (rawZ - smoothedZ)

        val rawMag = sqrt(smoothedX * smoothedX + smoothedY * smoothedY + smoothedZ * smoothedZ)
        val calibratedMag = (rawMag - baselineOffset).coerceAtLeast(0f)

        _readingFlow.value = MagnetReading(
            x = smoothedX,
            y = smoothedY,
            z = smoothedZ,
            magnitude = calibratedMag,
            rawMagnitude = rawMag,
            timestamp = System.currentTimeMillis()
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun startSimulationLoop() {
        if (simulationJob?.isActive == true) return
        simulationJob = scope.launch {
            var step = 0f
            while (isActive) {
                step += 0.08f
                // Ambient fluctuation around 42-48 uT
                val ambient = 45f + (sin(step.toDouble()) * 4f).toFloat()
                val reading = MagnetReading(
                    x = ambient * 0.4f + (sin(step * 0.7) * 2f).toFloat(),
                    y = ambient * 0.7f + (sin(step * 1.2) * 3f).toFloat(),
                    z = ambient * 0.5f,
                    magnitude = ambient,
                    rawMagnitude = ambient,
                    timestamp = System.currentTimeMillis()
                )
                _readingFlow.value = reading
                delay(60)
            }
        }
    }

    private fun stopSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = null
    }
}
