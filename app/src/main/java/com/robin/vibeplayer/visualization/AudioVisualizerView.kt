package com.robin.vibeplayer.visualization

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class AudioVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var centerX = 0f
    private var centerY = 0f
    private var mRawAudioBytes = ByteArray(120) // 音频数据
    
    // 初始化所有效果
    private val visualizers = mapOf(
        VisualizerEffect.NET to NetVisualizer(),
        VisualizerEffect.CIRCLE to CircleVisualizer(),
        VisualizerEffect.WAVE to WaveVisualizer()
    )
    
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
        visualizers[currentEffect]?.draw(canvas, centerX, centerY, mRawAudioBytes)
    }



    @JvmName("updateWaveform")
    fun updateWaveform(newAmplitudes: FloatArray) {
        // 将浮点数组转换为字节数组
        mRawAudioBytes = newAmplitudes.map { (it * 128).toInt().toByte() }.toByteArray()
        invalidate()
    }

    fun updateFFT(fft: FloatArray) {
        // 我们使用波形数据而不是FFT数据
    }
}
