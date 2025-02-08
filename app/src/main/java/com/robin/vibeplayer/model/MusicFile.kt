package com.robin.vibeplayer.model

data class MusicFile(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val path: String
)
