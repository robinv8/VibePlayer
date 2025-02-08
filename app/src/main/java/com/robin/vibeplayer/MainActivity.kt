package com.robin.vibeplayer

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.Visualizer
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.robin.vibeplayer.adapter.MusicAdapter
import com.robin.vibeplayer.model.MusicFile
import com.robin.vibeplayer.visualization.AudioVisualizerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.core.widget.NestedScrollView
import com.robin.vibeplayer.visualization.VisualizerEffect

class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var visualizer: Visualizer? = null
    private lateinit var visualizerView: AudioVisualizerView
    private lateinit var playButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var playlistButton: ImageButton
    private lateinit var effectButton: ImageButton
    private lateinit var musicAdapter: MusicAdapter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<NestedScrollView>

    private var musicFiles = mutableListOf<MusicFile>()
    private var currentMusicIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupBottomSheet()
        setupAudioPermissions()
        setupMediaPlayer()
        setupMusicList()
    }

    private fun setupViews() {
        visualizerView = findViewById(R.id.visualizer)
        playButton = findViewById(R.id.playButton)
        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
        playlistButton = findViewById(R.id.playlistButton)
        effectButton = findViewById(R.id.effectButton)
        
        // 设置效果切换按钮
        effectButton.setOnClickListener {
            visualizerView.currentEffect = when (visualizerView.currentEffect) {
                VisualizerEffect.NET -> VisualizerEffect.CIRCLE
                VisualizerEffect.CIRCLE -> VisualizerEffect.NET
            }
        }
        
        setupPlayButton()
        setupNavigationButtons()
    }

    private fun setupBottomSheet() {
        val bottomSheet = findViewById<NestedScrollView>(R.id.bottomSheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        playlistButton.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
        }
    }

    private fun setupMusicList() {
        val recyclerView = findViewById<RecyclerView>(R.id.musicRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        musicAdapter = MusicAdapter(musicFiles) { musicFile ->
            playMusic(musicFiles.indexOf(musicFile))
        }
        recyclerView.adapter = musicAdapter

        loadMusicFiles()
    }

    private fun loadMusicFiles() {
        // 添加内置的 APT 音频文件
        val aptMusicFile = MusicFile(
            id = -1,
            title = "Default APT Audio",
            artist = "System",
            duration = 0,
            path = "raw://apt"
        )
        musicFiles.add(aptMusicFile)

        // 加载设备中的音乐文件
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        
        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))

                val musicFile = MusicFile(id, title, artist, duration, path)
                musicFiles.add(musicFile)
            }
        }

        musicAdapter.updateList(musicFiles)
    }

    private fun setupAudioPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (!hasPermissions(permissions)) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        } else {
            setupVisualizer()
        }
    }

    private fun hasPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
    }

    private fun playMusic(index: Int) {
        if (index < 0 || index >= musicFiles.size) return
        
        currentMusicIndex = index
        val musicFile = musicFiles[index]

        try {
            mediaPlayer?.apply {
                reset()
                if (musicFile.path.startsWith("raw://")) {
                    // 播放内置音频文件
                    val resourceName = musicFile.path.substringAfter("raw://")
                    android.util.Log.d("VibePlayer", "Playing raw resource: $resourceName")
                    
                    val resourceId = resources.getIdentifier(
                        resourceName,
                        "raw",
                        packageName
                    )
                    
                    android.util.Log.d("VibePlayer", "Resource ID: $resourceId")
                    
                    if (resourceId == 0) {
                        throw IllegalStateException("Could not find resource: $resourceName")
                    }
                    
                    val afd = resources.openRawResourceFd(resourceId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                } else {
                    // 播放外部音频文件
                    android.util.Log.d("VibePlayer", "Playing external file: ${musicFile.path}")
                    setDataSource(musicFile.path)
                }
                
                setOnPreparedListener { 
                    android.util.Log.d("VibePlayer", "MediaPlayer prepared successfully")
                    start()
                    playButton.setImageResource(android.R.drawable.ic_media_pause)
                    setupVisualizer() // 在播放器准备好后设置Visualizer
                }
                
                setOnErrorListener { mp, what, extra ->
                    android.util.Log.e("VibePlayer", "MediaPlayer error: what=$what, extra=$extra")
                    false
                }
                
                prepareAsync()
            }
        } catch (e: Exception) {
            android.util.Log.e("VibePlayer", "Error playing music", e)
            // 显示错误提示
            android.widget.Toast.makeText(
                this,
                "播放错误: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupNavigationButtons() {
        previousButton.setOnClickListener {
            playMusic(currentMusicIndex - 1)
        }

        nextButton.setOnClickListener {
            playMusic(currentMusicIndex + 1)
        }
    }

    private fun setupPlayButton() {
        playButton.setOnClickListener {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    playButton.setImageResource(android.R.drawable.ic_media_play)
                } else {
                    if (currentMusicIndex == -1 && musicFiles.isNotEmpty()) {
                        playMusic(0)
                    } else {
                        it.start()
                        playButton.setImageResource(android.R.drawable.ic_media_pause)
                    }
                }
            }
        }
    }

    private fun setupVisualizer() {
        // 释放旧的Visualizer
        visualizer?.release()
        
        mediaPlayer?.let { player ->
            // 创建新的Visualizer
            visualizer = Visualizer(player.audioSessionId).apply {
                // 设置最大的采样大小
                captureSize = Visualizer.getCaptureSizeRange()[1]
                
                // 设置数据捕获监听器
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(visualizer: Visualizer, waveform: ByteArray, samplingRate: Int) {
                        val amplitudes = FloatArray(waveform.size) {
                            // 增强波形数据
                            val raw = waveform[it].toFloat() / 128.0f
                            raw * 1.5f // 降低放大倍数
                        }

                        visualizerView.updateWaveform(amplitudes)
                    }

                    override fun onFftDataCapture(visualizer: Visualizer, fft: ByteArray, samplingRate: Int) {
                        // 我们使用波形数据而不是FFT数据
                    }
                }, Visualizer.getMaxCaptureRate(), true, false) // 使用最大采样率
                
                // 启用Visualizer
                enabled = true
            }
            
            android.util.Log.d("VibePlayer", "Visualizer setup completed. AudioSessionId: ${player.audioSessionId}")
        } ?: run {
            android.util.Log.e("VibePlayer", "MediaPlayer is null when setting up visualizer")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                setupVisualizer()
                loadMusicFiles()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        visualizer?.release()
        mediaPlayer?.release()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}
