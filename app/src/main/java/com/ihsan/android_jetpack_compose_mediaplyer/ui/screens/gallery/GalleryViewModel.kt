package com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.ihsan.android_jetpack_compose_mediaplyer.data.local.MediaRepository
import com.ihsan.android_jetpack_compose_mediaplyer.model.LocalMediaItem
import com.ihsan.android_jetpack_compose_mediaplyer.model.MediaType
import com.ihsan.android_jetpack_compose_mediaplyer.model.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalleryViewModel(context: Context) : ViewModel() {
    private val mediaRepository = MediaRepository(context.contentResolver)

    // ExoPlayer instance
    val exoPlayer = ExoPlayer.Builder(context).build()
    val playerState = mutableStateOf(PlayerState())
    var playerListener: Player.Listener? = null

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

    @OptIn(UnstableApi::class)
    fun prepareMediaSource(song: LocalMediaItem, context: Context) {
        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse(song.data))
            .build()
        val mediaSource = ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(context)
        ).createMediaSource(mediaItem)

//        playerListener= object :Player.Listener{
//            override fun onIsPlayingChanged(isPlaying: Boolean) {
//                super.onIsPlayingChanged(isPlaying)
//                playerState.value = playerState.value.copy(
//                    isPlaying = isPlaying
//                )
//            }
//
//            override fun onPlaybackStateChanged(state: Int) {
//                super.onPlaybackStateChanged(state)
//                playerState.value = playerState.value.copy(
//                    isPlaying = state == Player.STATE_READY && exoPlayer.playWhenReady,
//                    duration = exoPlayer.duration,
//                    bufferedPosition = exoPlayer.bufferedPosition
//                )
//            }
//
//            override fun onPlayerError(error: PlaybackException) {
//                super.onPlayerError(error)
//                handlePlayerError(error)
//            }
//
//            override fun onPositionDiscontinuity(reason: Int) {
//                super.onPositionDiscontinuity(reason)
//                playerState.value = playerState.value.copy(
//                    currentPosition = exoPlayer.currentPosition
//                )
//            }
//        }

        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        //exoPlayer.addListener(playerListener as Player.Listener)

        playerState.value = playerState.value.copy(
            isPlaying = true,
            currentPosition = 0L,
            duration = exoPlayer.duration
        )
    }

    fun playPause(){
        if (playerState.value.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.playWhenReady = true
        }
        playerState.value = playerState.value.copy(isPlaying = !playerState.value.isPlaying)
    }

    fun seekToPlayer(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun handlePlayerError(error: Exception) {
        playerState.value = playerState.value.copy(
            error = "Error: ${error.message}"
        )
    }

    fun disposePlayer() {
        exoPlayer.stop()
//        exoPlayer.release()
        if (playerListener != null) {
            exoPlayer.removeListener(playerListener!!)
        }
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
