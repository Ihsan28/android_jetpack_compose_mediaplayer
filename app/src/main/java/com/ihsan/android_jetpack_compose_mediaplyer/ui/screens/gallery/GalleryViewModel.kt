package com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery

import android.content.Context
import androidx.lifecycle.ViewModel
import com.ihsan.android_jetpack_compose_mediaplyer.data.local.MediaRepository
import com.ihsan.android_jetpack_compose_mediaplyer.model.LocalMediaItem
import com.ihsan.android_jetpack_compose_mediaplyer.model.MediaType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalleryViewModel(context:Context) : ViewModel() {
    private val mediaRepository = MediaRepository(context.contentResolver)

    private val _Local_mediaItems = MutableStateFlow<List<LocalMediaItem>>(emptyList())
    val localMediaItems: StateFlow<List<LocalMediaItem>> = _Local_mediaItems.asStateFlow()

    private val _imageItems = MutableStateFlow<List<LocalMediaItem>>(emptyList())
    val imageItems: StateFlow<List<LocalMediaItem>> = _imageItems.asStateFlow()

    private val _videoItems = MutableStateFlow<List<LocalMediaItem>>(emptyList())
    val videoItems: StateFlow<List<LocalMediaItem>> = _videoItems.asStateFlow()

    private val _audioItems = MutableStateFlow<List<LocalMediaItem>>(emptyList())
    val audioItems: StateFlow<List<LocalMediaItem>> = _audioItems.asStateFlow()

    init {
        loadMediaItems()
    }

    private fun loadMediaItems() {
        _Local_mediaItems.value = mediaRepository.getAllMedia()

        _Local_mediaItems.value.forEach { mediaItem ->
            when (mediaItem.type) {
                MediaType.IMAGE -> _imageItems.value += mediaItem
                MediaType.VIDEO -> _videoItems.value += mediaItem
                MediaType.AUDIO -> _audioItems.value += mediaItem
                else -> {
                }
            }
        }
    }
}
