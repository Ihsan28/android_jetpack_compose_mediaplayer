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