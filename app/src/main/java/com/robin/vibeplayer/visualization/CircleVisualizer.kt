package com.robin.vibeplayer.visualization

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path

class CircleVisualizer : IVisualizer {
    private val paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }
    
    private var mSpectrumCount = 120 // 频谱数量

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float, audioData: ByteArray) {
        val baseRadius = 150f
        val maxAmplitude = audioData.maxOrNull()?.toFloat() ?: 0f
        val normalizedAmplitudes = audioData.map { it.toFloat() / (maxAmplitude.takeIf { it > 0 } ?: 1f) }
        
        // 绘制多个圆圈
        for (i in 0 until 3) {
            val radius = baseRadius + i * 50f
            val points = mutableListOf<Float>()
            
            for (j in 0 until mSpectrumCount) {
                val angle = (360.0 / mSpectrumCount) * j
                val radian = Math.toRadians(angle)
                val amplitude = normalizedAmplitudes[j] * 30f // 振幅调整
                
                val x = centerX + (radius + amplitude) * Math.sin(radian)
                val y = centerY + (radius + amplitude) * Math.cos(radian)
                
                points.add(x.toFloat())
                points.add(y.toFloat())
            }
            
            // 添加第一个点以闭合路径
            if (points.size >= 4) {
                points.add(points[0])
                points.add(points[1])
            }
            
            // 绘制平滑曲线
            if (points.size >= 4) {
                val path = Path()
                path.moveTo(points[0], points[1])
                
                var i = 2
                while (i < points.size - 2) {
                    val xc = (points[i] + points[i + 2]) / 2
                    val yc = (points[i + 1] + points[i + 3]) / 2
                    path.quadTo(points[i], points[i + 1], xc, yc)
                    i += 2
                }
                
                // 处理最后两个点
                path.quadTo(
                    points[points.size - 2], points[points.size - 1],
                    points[0], points[1]
                )
                
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawPath(path, paint)
            }
        }
    }
}
