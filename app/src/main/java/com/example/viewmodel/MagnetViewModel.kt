package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.FeedbackHelper
import com.example.model.FieldStatus
import com.example.model.MagnetReading
import com.example.model.TriggerAction
import com.example.sensor.MagnetometerManager
import com.example.service.MagnetBackgroundService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MagnetUiState(
    val currentReading: MagnetReading = MagnetReading(),
    val peakMagnitude: Float = 45f,
    val minMagnitude: Float = 45f,
    val avgMagnitude: Float = 45f,
    val threshold: Float = 120f,
    val selectedAction: TriggerAction = TriggerAction.FIRECRACKER_POP,
    val isAudioEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val isBackgroundServiceRunning: Boolean = false,
    val hasHardwareSensor: Boolean = true,
    val isSimulating: Boolean = false,
    val status: FieldStatus = FieldStatus.NORMAL,
    val isTriggerActive: Boolean = false,
    val historyPoints: List<Float> = emptyList(),
    val triggerCount: Int = 0
)

class MagnetViewModel(application: Application) : AndroidViewModel(application) {

    val sensorManager = MagnetometerManager(application)
    val feedbackHelper = FeedbackHelper(application)

    private val _uiState = MutableStateFlow(
        MagnetUiState(
            hasHardwareSensor = sensorManager.hasHardwareSensor,
            isSimulating = sensorManager.isSimulating.value
        )
    )
    val uiState: StateFlow<MagnetUiState> = _uiState.asStateFlow()

    private val _triggerEvent = MutableSharedFlow<TriggerAction>()
    val triggerEvent: SharedFlow<TriggerAction> = _triggerEvent.asSharedFlow()

    private val historyBuffer = ArrayDeque<Float>(60)
    private var totalSum = 0.0
    private var sampleCount = 0L
    private var lastTriggerTime = 0L
    private val triggerCooldownMs = 1800L

    init {
        viewModelScope.launch {
            sensorManager.isSimulating.collect { sim ->
                _uiState.value = _uiState.value.copy(isSimulating = sim)
            }
        }

        viewModelScope.launch {
            MagnetBackgroundService.isRunning.collect { running ->
                _uiState.value = _uiState.value.copy(isBackgroundServiceRunning = running)
            }
        }

        viewModelScope.launch {
            sensorManager.readingFlow.collect { reading ->
                processReading(reading)
            }
        }
    }

    private fun processReading(reading: MagnetReading) {
        val currentMag = reading.magnitude
        val state = _uiState.value

        // Maintain 60-point history for waveform chart
        if (historyBuffer.size >= 60) {
            historyBuffer.removeFirst()
        }
        historyBuffer.addLast(currentMag)

        totalSum += currentMag
        sampleCount++
        val avg = (totalSum / sampleCount).toFloat()

        val newPeak = maxOf(state.peakMagnitude, currentMag)
        val newMin = if (sampleCount == 1L) currentMag else minOf(state.minMagnitude, currentMag)

        val threshold = state.threshold
        val isOver = currentMag >= threshold
        val status = when {
            isOver -> FieldStatus.TRIGGERED
            currentMag > 70f -> FieldStatus.ELEVATED
            else -> FieldStatus.NORMAL
        }

        val now = System.currentTimeMillis()
        var newTriggerCount = state.triggerCount
        var isTriggerActive = false

        if (isOver) {
            isTriggerActive = true

            // Trigger crackle wave burst or specific action
            if (now - lastTriggerTime > triggerCooldownMs) {
                lastTriggerTime = now
                newTriggerCount++

                if (state.selectedAction == TriggerAction.FIRECRACKER_POP) {
                    feedbackHelper.playCrackleWaveBurst()
                } else {
                    if (state.isAudioEnabled) {
                        feedbackHelper.playCrackleWaveBurst()
                    } else if (state.isHapticEnabled) {
                        feedbackHelper.triggerAlarmHaptic()
                    }
                }

                viewModelScope.launch {
                    _triggerEvent.emit(state.selectedAction)
                }
            }
        } else if (state.isAudioEnabled && currentMag > 60f) {
            feedbackHelper.playGeigerClick(currentMag, threshold)
        }

        _uiState.value = state.copy(
            currentReading = reading,
            peakMagnitude = newPeak,
            minMagnitude = newMin,
            avgMagnitude = avg,
            status = status,
            isTriggerActive = isTriggerActive,
            historyPoints = historyBuffer.toList(),
            triggerCount = newTriggerCount
        )
    }

    fun start() {
        sensorManager.startListening()
    }

    fun stop() {
        // If background service is NOT running, stop sensor to save battery
        if (!_uiState.value.isBackgroundServiceRunning) {
            sensorManager.stopListening()
        }
    }

    fun toggleBackgroundService() {
        val context = getApplication<Application>()
        if (_uiState.value.isBackgroundServiceRunning) {
            MagnetBackgroundService.stopService(context)
        } else {
            MagnetBackgroundService.startService(context, _uiState.value.threshold)
        }
    }

    fun testFirecrackerPop() {
        feedbackHelper.playCrackleWaveBurst()
    }

    fun setThreshold(value: Float) {
        val updatedThreshold = value.coerceIn(50f, 350f)
        _uiState.value = _uiState.value.copy(threshold = updatedThreshold)
        // If background service is running, restart with new threshold
        if (_uiState.value.isBackgroundServiceRunning) {
            MagnetBackgroundService.startService(getApplication(), updatedThreshold)
        }
    }

    fun selectTriggerAction(action: TriggerAction) {
        feedbackHelper.triggerClickHaptic()
        _uiState.value = _uiState.value.copy(selectedAction = action)
    }

    fun toggleAudio() {
        val next = !_uiState.value.isAudioEnabled
        feedbackHelper.triggerClickHaptic()
        _uiState.value = _uiState.value.copy(isAudioEnabled = next)
    }

    fun toggleHaptics() {
        val next = !_uiState.value.isHapticEnabled
        feedbackHelper.triggerClickHaptic()
        _uiState.value = _uiState.value.copy(isHapticEnabled = next)
    }

    fun toggleSimulation() {
        feedbackHelper.triggerClickHaptic()
        sensorManager.toggleSimulation()
    }

    fun setSimulatedMagnetStrength(uT: Float) {
        sensorManager.setSimulationStrength(uT)
    }

    fun calibrateTare() {
        feedbackHelper.triggerClickHaptic()
        sensorManager.calibrateTare()
        resetStats()
    }

    fun resetStats() {
        totalSum = 0.0
        sampleCount = 0L
        historyBuffer.clear()
        _uiState.value = _uiState.value.copy(
            peakMagnitude = _uiState.value.currentReading.magnitude,
            minMagnitude = _uiState.value.currentReading.magnitude,
            avgMagnitude = _uiState.value.currentReading.magnitude,
            triggerCount = 0
        )
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopListening()
        feedbackHelper.release()
    }
}
