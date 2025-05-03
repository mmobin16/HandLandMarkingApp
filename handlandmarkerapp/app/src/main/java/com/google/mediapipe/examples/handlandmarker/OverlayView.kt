package com.google.mediapipe.examples.handlandmarker

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
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
    private var coordinatePaint = Paint()
    private var backgroundPaint = Paint()
    private var buttonPaint = Paint()
    private var landmarkLabels = arrayOf(
        "Wrist", "Thumb CMC", "Thumb MCP", "Thumb IP", "Thumb Tip",
        "Index MCP", "Index PIP", "Index DIP", "Index Tip",
        "Middle MCP", "Middle PIP", "Middle DIP", "Middle Tip",
        "Ring MCP", "Ring PIP", "Ring DIP", "Ring Tip",
        "Pinky MCP", "Pinky PIP", "Pinky DIP", "Pinky Tip"
    )

    private var scaleFactor: Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1
    private var imageRotation: Int = 0
    private var translateX: Float = 0f
    private var translateY: Float = 0f
    
    // UI state
    private var showCoordinates = false
    private var buttonRect = RectF()
    private var windowRect = RectF()
    private var isWindowOpen = false
    private var windowAnimationProgress = 0f
    private var windowAnimationSpeed = 0.2f
    private var isAnimating = false
    
    // Scrolling state
    private var scrollOffset = 0f
    private var maxScrollOffset = 0f
    private var isScrolling = false
    private var lastTouchY = 0f
    private var contentHeight = 0f

    // Colors for different hand parts
    private val palmColor = Color.parseColor("#4CAF50")
    private val thumbColor = Color.parseColor("#2196F3")
    private val indexColor = Color.parseColor("#FF9800")
    private val middleColor = Color.parseColor("#F44336")
    private val ringColor = Color.parseColor("#9C27B0")
    private val pinkyColor = Color.parseColor("#607D8B")
    private val buttonColor = Color.parseColor("#4CAF50")
    private val buttonPressedColor = Color.parseColor("#388E3C")

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

        // Coordinate text paint
        coordinatePaint.apply {
            color = Color.WHITE
            textSize = COORDINATE_TEXT_SIZE
            typeface = Typeface.MONOSPACE
            isAntiAlias = true
        }
        
        // Background paint for coordinate window
        backgroundPaint.apply {
            color = Color.argb(200, 0, 0, 0)
            style = Paint.Style.FILL
        }
        
        // Button paint
        buttonPaint.apply {
            color = buttonColor
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results?.let { handLandmarkerResult ->
            // Calculate the scale factor based on the view and image dimensions
            val viewAspectRatio = width.toFloat() / height.toFloat()
            val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()

            // Calculate the scale factor to fit the image in the view while maintaining aspect ratio
            scaleFactor = if (viewAspectRatio > imageAspectRatio) {
                height.toFloat() / imageHeight.toFloat()
            } else {
                width.toFloat() / imageWidth.toFloat()
            }

            // Calculate the translation to center the image in the view
            translateX = (width - imageWidth * scaleFactor) / 2
            translateY = (height - imageHeight * scaleFactor) / 2

            // Apply the transformation
            canvas.translate(translateX, translateY)

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

                // Draw landmarks with numbers
                landmark.forEachIndexed { index, normalizedLandmark ->
                    pointPaint.color = when {
                        index in 1..4 -> thumbColor
                        index in 5..8 -> indexColor
                        index in 9..12 -> middleColor
                        index in 13..16 -> ringColor
                        index in 17..20 -> pinkyColor
                        else -> palmColor
                    }

                    val x = normalizedLandmark.x() * imageWidth * scaleFactor
                    val y = normalizedLandmark.y() * imageHeight * scaleFactor

                    // Draw the landmark point
                    canvas.drawCircle(x, y, LANDMARK_POINT_WIDTH, pointPaint)
                    
                    // Draw the landmark number
                    val numberText = (index + 1).toString()
                    val numberBounds = Rect()
                    coordinatePaint.getTextBounds(numberText, 0, numberText.length, numberBounds)
                    
                    // Draw background for the number
                    canvas.drawCircle(
                        x, 
                        y, 
                        LANDMARK_POINT_WIDTH + 5, 
                        Paint().apply {
                            color = Color.argb(180, 0, 0, 0)
                            style = Paint.Style.FILL
                        }
                    )
                    
                    // Draw the number
                    canvas.drawText(
                        numberText,
                        x - numberBounds.width() / 2,
                        y + numberBounds.height() / 2,
                        coordinatePaint
                    )
                }
            }
        }
        
        // Reset canvas translation to draw UI elements
        canvas.translate(-translateX, -translateY)
        
        // Draw the coordinates button
        drawCoordinatesButton(canvas)
        
        // Draw the sliding window if it's open or animating
        if (isWindowOpen || isAnimating) {
            drawSlidingWindow(canvas)
            
            // Continue animation if needed
            if (isAnimating) {
                if (isWindowOpen) {
                    windowAnimationProgress += windowAnimationSpeed
                    if (windowAnimationProgress >= 1f) {
                        windowAnimationProgress = 1f
                        isAnimating = false
                    }
                } else {
                    windowAnimationProgress -= windowAnimationSpeed
                    if (windowAnimationProgress <= 0f) {
                        windowAnimationProgress = 0f
                        isAnimating = false
                        // Reset scroll when window closes
                        scrollOffset = 0f
                    }
                }
                invalidate()
            }
        }
    }
    
    private fun drawCoordinatesButton(canvas: Canvas) {
        // Define button dimensions
        val buttonWidth = 60f
        val buttonHeight = 120f
        val buttonX = width - buttonWidth - 20f
        val buttonY = height / 2f - buttonHeight / 2f
        
        // Update button rect for touch detection
        buttonRect.set(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight)
        
        // Draw button background
        buttonPaint.color = if (showCoordinates) buttonPressedColor else buttonColor
        canvas.drawRoundRect(buttonRect, 10f, 10f, buttonPaint)
        
        // Draw button text
        val buttonText = "COORDS"
        val textBounds = Rect()
        textPaint.apply {
            textSize = 16f
            getTextBounds(buttonText, 0, buttonText.length, textBounds)
        }
        
        // Draw text vertically
        val textX = buttonX + buttonWidth / 2f
        val textY = buttonY + buttonHeight / 2f
        
        canvas.save()
        canvas.rotate(90f, textX, textY)
        canvas.drawText(buttonText, textX - textBounds.width() / 2f, textY, textPaint)
        canvas.restore()
    }
    
    private fun drawSlidingWindow(canvas: Canvas) {
        // Calculate window dimensions
        val windowWidth = width * 0.8f
        val windowHeight = height * 0.7f
        val windowX = width - windowWidth * windowAnimationProgress
        val windowY = (height - windowHeight) / 2f
        
        // Update window rect
        windowRect.set(windowX, windowY, windowX + windowWidth, windowY + windowHeight)
        
        // Draw window background
        canvas.drawRect(windowRect, backgroundPaint)
        
        // Draw window border
        canvas.drawRect(windowRect, Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
        })
        
        // Draw close button
        val closeButtonSize = 40f
        val closeButtonX = windowX + windowWidth - closeButtonSize - 10f
        val closeButtonY = windowY + 10f
        
        canvas.drawCircle(
            closeButtonX + closeButtonSize / 2,
            closeButtonY + closeButtonSize / 2,
            closeButtonSize / 2,
            Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            }
        )
        
        canvas.drawText(
            "X",
            closeButtonX + closeButtonSize / 2 - 5f,
            closeButtonY + closeButtonSize / 2 + 5f,
            textPaint.apply {
                color = Color.WHITE
                textSize = 20f
            }
        )
        
        // Draw window title
        canvas.drawText(
            "Hand Landmark Coordinates",
            windowX + 20f,
            windowY + 40f,
            textPaint.apply {
                textSize = 24f
            }
        )
        
        // Only draw coordinates if we have results
        results?.let { handLandmarkerResult ->
            // Calculate content height to determine if scrolling is needed
            val columns = 1 // Single column for vertical layout
            val rows = 21 // All landmarks in one column
            val cellWidth = windowWidth - 40f
            val cellHeight = 80f // Increased cell height for larger text
            
            // Calculate total content height
            var totalContentHeight = 0f
            handLandmarkerResult.landmarks().forEachIndexed { handIndex, _ ->
                totalContentHeight += 60f // Increased space for hand title
                totalContentHeight += rows * cellHeight // Space for landmarks
            }
            
            // Update max scroll offset
            val visibleContentHeight = windowHeight - 100f // Account for header
            maxScrollOffset = max(0f, totalContentHeight - visibleContentHeight)
            
            // Clamp scroll offset
            scrollOffset = max(0f, min(scrollOffset, maxScrollOffset))
            
            // Draw scroll indicator if needed
            if (maxScrollOffset > 0) {
                val scrollBarWidth = 10f
                val scrollBarHeight = (visibleContentHeight / totalContentHeight) * visibleContentHeight
                val scrollBarX = windowX + windowWidth - scrollBarWidth - 5f
                val scrollBarY = windowY + 100f
                val scrollThumbY = scrollBarY + (scrollOffset / maxScrollOffset) * (visibleContentHeight - scrollBarHeight)
                
                // Draw scroll track
                canvas.drawRect(
                    scrollBarX,
                    scrollBarY,
                    scrollBarX + scrollBarWidth,
                    scrollBarY + visibleContentHeight,
                    Paint().apply {
                        color = Color.argb(100, 255, 255, 255)
                        style = Paint.Style.FILL
                    }
                )
                
                // Draw scroll thumb
                canvas.drawRect(
                    scrollBarX,
                    scrollThumbY,
                    scrollBarX + scrollBarWidth,
                    scrollThumbY + scrollBarHeight,
                    Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.FILL
                    }
                )
            }
            
            // Apply scroll offset
            canvas.save()
            canvas.clipRect(windowX, windowY + 80f, windowX + windowWidth - 20f, windowY + windowHeight - 20f)
            
            // Draw coordinates for each hand
            var currentY = windowY + 100f - scrollOffset
            
            handLandmarkerResult.landmarks().forEachIndexed { handIndex, landmark ->
                // Draw hand title with background
                val handTitle = "Hand ${handIndex + 1}"
                val titleBounds = Rect()
                textPaint.apply {
                    textSize = 28f // Increased title size
                    getTextBounds(handTitle, 0, handTitle.length, titleBounds)
                }
                
                // Draw title background
                canvas.drawRect(
                    windowX + 10f,
                    currentY - titleBounds.height() - 10f,
                    windowX + titleBounds.width() + 30f,
                    currentY + 10f,
                    Paint().apply {
                        color = Color.argb(150, 0, 0, 0)
                        style = Paint.Style.FILL
                    }
                )
                
                // Draw hand title
                canvas.drawText(
                    handTitle,
                    windowX + 20f,
                    currentY,
                    textPaint
                )
                
                currentY += 60f // Increased spacing after title
                
                // Draw coordinates for this hand
                landmark.forEachIndexed { index, normalizedLandmark ->
                    val cellX = windowX + 20f
                    val cellY = currentY + index * cellHeight
                    
                    // Draw landmark label and coordinates
                    val labelText = "${index + 1}. ${landmarkLabels[index]}:"
                    val coordText = String.format("(%.2f, %.2f, %.2f)", 
                        normalizedLandmark.javaClass.getMethod("x").invoke(normalizedLandmark) as Float, 
                        normalizedLandmark.javaClass.getMethod("y").invoke(normalizedLandmark) as Float, 
                        normalizedLandmark.javaClass.getMethod("z").invoke(normalizedLandmark) as Float)
                    
                    // Draw background for better readability
                    canvas.drawRect(
                        cellX - 5f,
                        cellY - 5f,
                        cellX + cellWidth - 10f,
                        cellY + cellHeight - 5f,
                        Paint().apply {
                            color = Color.argb(50, 255, 255, 255)
                            style = Paint.Style.FILL
                        }
                    )
                    
                    canvas.drawText(
                        labelText,
                        cellX,
                        cellY + 35f, // Adjusted for larger text
                        coordinatePaint.apply {
                            textSize = 24f // Increased label size
                        }
                    )
                    
                    canvas.drawText(
                        coordText,
                        cellX,
                        cellY + 70f, // Adjusted for larger text
                        coordinatePaint.apply {
                            textSize = 22f // Increased coordinate size
                        }
                    )
                }
                
                currentY += rows * cellHeight + 40f // Add spacing between hands
            }
            
            canvas.restore()
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check if button was pressed
                if (buttonRect.contains(event.x, event.y)) {
                    showCoordinates = !showCoordinates
                    isWindowOpen = showCoordinates
                    isAnimating = true
                    invalidate()
                    return true
                }
                
                // Check if close button was pressed
                if (isWindowOpen && windowRect.contains(event.x, event.y)) {
                    val closeButtonX = windowRect.right - 50f
                    val closeButtonY = windowRect.top + 10f
                    val closeButtonSize = 40f
                    
                    if (event.x >= closeButtonX && event.x <= closeButtonX + closeButtonSize &&
                        event.y >= closeButtonY && event.y <= closeButtonY + closeButtonSize) {
                        showCoordinates = false
                        isWindowOpen = false
                        isAnimating = true
                        invalidate()
                        return true
                    }
                }
                
                // Check if we're touching the scrollable area
                if (isWindowOpen && windowRect.contains(event.x, event.y) && 
                    event.y > windowRect.top + 100f && event.y < windowRect.bottom - 20f &&
                    event.x < windowRect.right - 20f) {
                    isScrolling = true
                    lastTouchY = event.y
                    return true
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isScrolling) {
                    val deltaY = lastTouchY - event.y
                    scrollOffset += deltaY
                    lastTouchY = event.y
                    invalidate()
                    return true
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isScrolling = false
            }
        }
        return super.onTouchEvent(event)
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
        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 8F
        private const val LANDMARK_POINT_WIDTH = 14F
        private const val TEXT_SIZE = 40F
        private const val COORDINATE_TEXT_SIZE = 20F
    }
}