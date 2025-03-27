package com.google.mediapipe.examples.handlandmarker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var results: HandLandmarkerResult? = null
    private var linePaint = Paint()
    private var pointPaint = Paint()
    private var textPaint = Paint()

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    // Colors for different hand parts
    private val palmColor = Color.parseColor("#4CAF50")
    private val thumbColor = Color.parseColor("#2196F3")
    private val indexColor = Color.parseColor("#FF9800")
    private val middleColor = Color.parseColor("#F44336")
    private val ringColor = Color.parseColor("#9C27B0")
    private val pinkyColor = Color.parseColor("#607D8B")

    init {
        initPaints()
    }

    fun clear() {
        results = null
        invalidate()
    }

    private fun initPaints() {
        // Line paint for connections
        linePaint.apply {
            strokeWidth = LANDMARK_STROKE_WIDTH
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        // Point paint for landmarks
        pointPaint.apply {
            strokeWidth = LANDMARK_POINT_WIDTH
            style = Paint.Style.FILL
        }

        // Text paint for debug info
        textPaint.apply {
            color = Color.WHITE
            textSize = TEXT_SIZE
            typeface = Typeface.DEFAULT_BOLD
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Draw debug info
        canvas.drawText("SignSpeak", 20f, 50f, textPaint)

        results?.let { handLandmarkerResult ->
            // Draw each hand's landmarks and connections
            handLandmarkerResult.landmarks().forEachIndexed { handIndex, landmark ->
                // Draw connections first (so points appear on top)
                HandLandmarker.HAND_CONNECTIONS.forEach { connection ->
                    connection?.let {
                        // Set different colors for different finger connections
                        linePaint.color = when {
                            it.start() in 1..4 || it.end() in 1..4 -> thumbColor
                            it.start() in 5..8 || it.end() in 5..8 -> indexColor
                            it.start() in 9..12 || it.end() in 9..12 -> middleColor
                            it.start() in 13..16 || it.end() in 13..16 -> ringColor
                            it.start() in 17..20 || it.end() in 17..20 -> pinkyColor
                            else -> palmColor
                        }

                        canvas.drawLine(
                            landmark.get(it.start()).x() * imageWidth * scaleFactor,
                            landmark.get(it.start()).y() * imageHeight * scaleFactor,
                            landmark.get(it.end()).x() * imageWidth * scaleFactor,
                            landmark.get(it.end()).y() * imageHeight * scaleFactor,
                            linePaint
                        )
                    }
                }

                // Draw landmarks
                landmark.forEachIndexed { index, normalizedLandmark ->
                    pointPaint.color = when {
                        index in 1..4 -> thumbColor
                        index in 5..8 -> indexColor
                        index in 9..12 -> middleColor
                        index in 13..16 -> ringColor
                        index in 17..20 -> pinkyColor
                        else -> palmColor
                    }

                    canvas.drawCircle(
                        normalizedLandmark.x() * imageWidth * scaleFactor,
                        normalizedLandmark.y() * imageHeight * scaleFactor,
                        LANDMARK_POINT_WIDTH,
                        pointPaint
                    )
                }
            }
        }
    }

    fun setResults(
        handLandmarkerResults: HandLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = handLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        scaleFactor = when (runningMode) {
            RunningMode.IMAGE, RunningMode.VIDEO -> {
                min(width * 1f / imageWidth, height * 1f / imageHeight)
            }
            RunningMode.LIVE_STREAM -> {
                max(width * 1f / imageWidth, height * 1f / imageHeight)
            }
        }
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 5F
        private const val LANDMARK_POINT_WIDTH = 10F
        private const val TEXT_SIZE = 40F
    }
}