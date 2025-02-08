package com.robin.vibeplayer.visualization

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AudioVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val path = Path()
    private var amplitudes = FloatArray(128) { 0f }
    private var radius = 0f
    private var centerX = 0f
    private var centerY = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 3f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawVisualizerCircle(canvas)
    }

    private fun drawVisualizerCircle(canvas: Canvas) {
        path.reset()
        val angleStep = 360f / amplitudes.size
        
        for (i in amplitudes.indices) {
            val angle = Math.toRadians((i * angleStep).toDouble())
            val amplitude = radius + amplitudes[i] * 100
            val x = centerX + amplitude * cos(angle).toFloat()
            val y = centerY + amplitude * sin(angle).toFloat()
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        
        path.close()
        canvas.drawPath(path, paint)
    }

    fun updateAmplitudes(newAmplitudes: FloatArray) {
        amplitudes = newAmplitudes
        invalidate()
    }
}
