package com.google.mediapipe.examples.handlandmarker.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.mediapipe.examples.handlandmarker.HandLandmarkerHelper
import com.google.mediapipe.examples.handlandmarker.MainViewModel
import com.google.mediapipe.examples.handlandmarker.databinding.FragmentSettingsBinding
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _fragmentSettingsBinding: FragmentSettingsBinding? = null
    private val fragmentSettingsBinding
        get() = _fragmentSettingsBinding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        return fragmentSettingsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up back button
        fragmentSettingsBinding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Initialize settings with current values
        fragmentSettingsBinding.maxHandsValue.text = viewModel.currentMaxHands.toString()
        fragmentSettingsBinding.detectionConfidenceSlider.value = viewModel.currentMinHandDetectionConfidence
        fragmentSettingsBinding.trackingConfidenceSlider.value = viewModel.currentMinHandTrackingConfidence
        fragmentSettingsBinding.presenceConfidenceSlider.value = viewModel.currentMinHandPresenceConfidence

        // Update value displays
        updateConfidenceValueDisplay(
            fragmentSettingsBinding.detectionConfidenceValue,
            viewModel.currentMinHandDetectionConfidence
        )
        updateConfidenceValueDisplay(
            fragmentSettingsBinding.trackingConfidenceValue,
            viewModel.currentMinHandTrackingConfidence
        )
        updateConfidenceValueDisplay(
            fragmentSettingsBinding.presenceConfidenceValue,
            viewModel.currentMinHandPresenceConfidence
        )

        // Set up slider listeners
        fragmentSettingsBinding.detectionConfidenceSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setMinHandDetectionConfidence(value)
                updateConfidenceValueDisplay(
                    fragmentSettingsBinding.detectionConfidenceValue,
                    value
                )
            }
        }

        fragmentSettingsBinding.trackingConfidenceSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setMinHandTrackingConfidence(value)
                updateConfidenceValueDisplay(
                    fragmentSettingsBinding.trackingConfidenceValue,
                    value
                )
            }
        }

        fragmentSettingsBinding.presenceConfidenceSlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                viewModel.setMinHandPresenceConfidence(value)
                updateConfidenceValueDisplay(
                    fragmentSettingsBinding.presenceConfidenceValue,
                    value
                )
            }
        }

        // Set up max hands controls
        fragmentSettingsBinding.maxHandsMinus.setOnClickListener {
            if (viewModel.currentMaxHands > 1) {
                viewModel.setMaxHands(viewModel.currentMaxHands - 1)
                fragmentSettingsBinding.maxHandsValue.text = viewModel.currentMaxHands.toString()
            }
        }

        fragmentSettingsBinding.maxHandsPlus.setOnClickListener {
            if (viewModel.currentMaxHands < 2) {
                viewModel.setMaxHands(viewModel.currentMaxHands + 1)
                fragmentSettingsBinding.maxHandsValue.text = viewModel.currentMaxHands.toString()
            }
        }

        // Set up delegate selection
        fragmentSettingsBinding.spinnerDelegate.setSelection(viewModel.currentDelegate, false)
        fragmentSettingsBinding.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    try {
                        viewModel.setDelegate(p2)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting delegate: ${e.message}")
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // No-op
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _fragmentSettingsBinding = null
    }

    private fun updateConfidenceValueDisplay(textView: TextView, value: Float) {
        textView.text = String.format(Locale.US, "%.2f", value)
    }

    companion object {
        private const val TAG = "SettingsFragment"
    }
} 