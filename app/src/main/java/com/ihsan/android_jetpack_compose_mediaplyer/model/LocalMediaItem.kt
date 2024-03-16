package com.ihsan.android_jetpack_compose_mediaplyer.model

data class LocalMediaItem(
    val id: Long,
    val type: MediaType, // enum for Video, Audio, Image
    val displayName: String,
    val dateAdded: Long,
    val size: Long,
    val data: String // File path
)

enum class MediaType {
    VIDEO,
    AUDIO,
    IMAGE,
    UNKNOWN
}
data class Song(val id: Int, val title: String, val artist: String, val filePath: String)
data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val bufferedPosition: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null
)

data class GalleryTabPage(
    val title: String,
    val items: List<LocalMediaItem>
)