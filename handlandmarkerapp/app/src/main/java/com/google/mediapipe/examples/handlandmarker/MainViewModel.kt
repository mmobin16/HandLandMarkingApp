package com.google.mediapipe.examples.handlandmarker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    // Existing hand landmarker settings
    private var _delegate: Int = HandLandmarkerHelper.DELEGATE_CPU
    private var _minHandDetectionConfidence: Float = HandLandmarkerHelper.DEFAULT_HAND_DETECTION_CONFIDENCE
    private var _minHandTrackingConfidence: Float = HandLandmarkerHelper.DEFAULT_HAND_TRACKING_CONFIDENCE
    private var _minHandPresenceConfidence: Float = HandLandmarkerHelper.DEFAULT_HAND_PRESENCE_CONFIDENCE
    private var _maxHands: Int = HandLandmarkerHelper.DEFAULT_NUM_HANDS

    // New UI state variables
    private val _uiState = MutableLiveData<UiState>()
    val uiState: LiveData<UiState> = _uiState

    // Existing getters
    val currentDelegate: Int get() = _delegate
    val currentMinHandDetectionConfidence: Float get() = _minHandDetectionConfidence
    val currentMinHandTrackingConfidence: Float get() = _minHandTrackingConfidence
    val currentMinHandPresenceConfidence: Float get() = _minHandPresenceConfidence
    val currentMaxHands: Int get() = _maxHands

    // Existing setters
    fun setDelegate(delegate: Int) {
        _delegate = delegate
        updateUiState()
    }

    fun setMinHandDetectionConfidence(confidence: Float) {
        _minHandDetectionConfidence = confidence
        updateUiState()
    }

    fun setMinHandTrackingConfidence(confidence: Float) {
        _minHandTrackingConfidence = confidence
        updateUiState()
    }

    fun setMinHandPresenceConfidence(confidence: Float) {
        _minHandPresenceConfidence = confidence
        updateUiState()
    }

    fun setMaxHands(maxResults: Int) {
        _maxHands = maxResults
        updateUiState()
    }

    // New UI state management
    private fun updateUiState() {
        _uiState.value = UiState(
            processingMode = if (_delegate == HandLandmarkerHelper.DELEGATE_GPU) "GPU" else "CPU",
            confidenceThresholds = mapOf(
                "Detection" to _minHandDetectionConfidence,
                "Tracking" to _minHandTrackingConfidence,
                "Presence" to _minHandPresenceConfidence
            ),
            maxHands = _maxHands
        )
    }

    data class UiState(
        val processingMode: String,
        val confidenceThresholds: Map<String, Float>,
        val maxHands: Int
    )
}