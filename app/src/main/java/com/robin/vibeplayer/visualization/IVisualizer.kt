package com.robin.vibeplayer.visualization

import android.graphics.Canvas

interface IVisualizer {
    fun draw(canvas: Canvas, centerX: Float, centerY: Float, audioData: ByteArray)
}
