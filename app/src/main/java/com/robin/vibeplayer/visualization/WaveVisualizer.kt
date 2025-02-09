package com.robin.vibeplayer.visualization

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.absoluteValue

class WaveVisualizer : IVisualizer {
    private val paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    
    private val path = Path()
    private var baseAmplitude = 50f // 基础振幅
    private var waveLength = 0.02f // 波长系数
    private var phase = 0f // 相位
    private var lastAmplitude = 0f // 上一帧的振幅

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float, audioData: ByteArray) {
        // 计算平均振幅用于调整波浪高度
        val maxAmplitude = audioData.maxOrNull()?.toFloat()?.absoluteValue ?: 0f
        val avgAmplitude = audioData.map { it.toFloat().absoluteValue }.average().toFloat()
        
        // 使用平均值和最大值的组合来计算当前振幅
        val currentAmplitude = (maxAmplitude * 0.3f + avgAmplitude * 0.7f) / 128f
        
        // 平滑过渡到新的振幅
        lastAmplitude = lastAmplitude * 0.7f + currentAmplitude * 0.3f
        val normalizedAmplitude = (lastAmplitude * 2f).coerceIn(0.3f, 2f)
        
        // 更新相位，创建移动效果
        phase += 0.15f + normalizedAmplitude * 0.1f // 振幅大时移动更快
        if (phase > 2 * Math.PI) {
            phase = 0f
        }
        
        // 绘制三条波浪
        for (waveIndex in 0..2) {
            val wavePhase = phase + waveIndex * Math.PI.toFloat() / 3
            val amplitude = baseAmplitude * (1 + waveIndex * 0.3f) * normalizedAmplitude
            
            path.reset()
            var x = 0f
            var firstPoint = true
            
            while (x <= canvas.width) {
                val y = centerY + amplitude * Math.sin((x * waveLength + wavePhase).toDouble()).toFloat()
                
                if (firstPoint) {
                    path.moveTo(x, y)
                    firstPoint = false
                } else {
                    path.lineTo(x, y)
                }
                
                x += 2 // 更小的步进值使波浪更平滑
            }
            
            // 设置不同的透明度
            paint.alpha = (255 * (0.8f - waveIndex * 0.2f)).toInt()
            canvas.drawPath(path, paint)
        }
    }
}
