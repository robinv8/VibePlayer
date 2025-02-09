package com.robin.vibeplayer.visualization

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.PI

class NetVisualizer : IVisualizer {
    private val paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }
    
    private val path = Path()
    private var mSpectrumCount = 120 // 频谱数量
    private var mItemMargin = 2f // 项目间距
    private var mSpectrumRatio = 20f // 频谱比例
    private var mStrokeWidth = 0f // 笔画宽度

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float, audioData: ByteArray) {
        val radius = 150f
        mStrokeWidth = ((PI * 2 * radius - (mSpectrumCount - 1) * mItemMargin) / mSpectrumCount).toFloat()
        
        // 绘制中心圆
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawCircle(centerX, centerY, radius, paint)

        // 绘制频谱线
        paint.strokeWidth = mStrokeWidth
        paint.style = Paint.Style.FILL
        
        path.reset()
        path.moveTo(0f, centerY)
        
        for (i in 0 until mSpectrumCount) {
            val angel = (360.0 / mSpectrumCount) * (i + 1)
            val radian = Math.toRadians(angel)
            
            val startX = centerX + (radius + mStrokeWidth/2) * Math.sin(radian)
            val startY = centerY + (radius + mStrokeWidth/2) * Math.cos(radian)
            
            val stopX = centerX + (radius + mStrokeWidth/2 + mSpectrumRatio * audioData[i]) * Math.sin(radian)
            val stopY = centerY + (radius + mStrokeWidth/2 + mSpectrumRatio * audioData[i]) * Math.cos(radian)
            
            canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), paint)
            
            if (i == 0) {
                path.moveTo(startX.toFloat(), startY.toFloat())
            }
            path.lineTo(stopX.toFloat(), stopY.toFloat())
        }
        
        // 绘制路径
        paint.style = Paint.Style.STROKE
        canvas.drawPath(path, paint)
    }
}
