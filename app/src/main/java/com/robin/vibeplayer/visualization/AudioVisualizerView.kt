package com.robin.vibeplayer.visualization

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI

class AudioVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
    }

    private val path = Path()
    private var centerX = 0f
    private var centerY = 0f
    
    private var mSpectrumCount = 120 // 减少频谱数量
    private var mItemMargin = 2f // 增加间距
    private var mSpectrumRatio = 20f // 减小频谱比例
    private var mStrokeWidth = 0f // 笔画宽度
    private var mRawAudioBytes = ByteArray(mSpectrumCount) // 音频数据
    
    // 当前效果类型
    var currentEffect = VisualizerEffect.NET
        set(value) {
            field = value
            invalidate()
        }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawVisualizer(canvas)
    }

    private fun drawVisualizer(canvas: Canvas) {
        when (currentEffect) {
            VisualizerEffect.NET -> drawNetEffect(canvas)
            VisualizerEffect.CIRCLE -> drawCircleEffect(canvas)
        }
    }

    private fun drawNetEffect(canvas: Canvas) {
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
            
            val stopX = centerX + (radius + mStrokeWidth/2 + mSpectrumRatio * mRawAudioBytes[i]) * Math.sin(radian)
            val stopY = centerY + (radius + mStrokeWidth/2 + mSpectrumRatio * mRawAudioBytes[i]) * Math.cos(radian)
            
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

    private fun drawCircleEffect(canvas: Canvas) {
        val baseRadius = 150f
        val maxAmplitude = mRawAudioBytes.maxOrNull()?.toFloat() ?: 0f
        val normalizedAmplitudes = mRawAudioBytes.map { it.toFloat() / (maxAmplitude.takeIf { it > 0 } ?: 1f) }
        
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

    @JvmName("updateWaveform")
    fun updateWaveform(newAmplitudes: FloatArray) {
        // 将浮点数组转换为字节数组
        mRawAudioBytes = newAmplitudes.map { (it).toInt().toByte() }.toByteArray()
        invalidate()
    }

    fun updateFFT(fft: FloatArray) {
        // 我们使用波形数据而不是FFT数据
    }
}
