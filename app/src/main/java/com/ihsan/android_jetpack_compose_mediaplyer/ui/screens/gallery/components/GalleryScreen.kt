package com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.components

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ihsan.android_jetpack_compose_mediaplyer.R
import com.ihsan.android_jetpack_compose_mediaplyer.model.LocalMediaItem
import com.ihsan.android_jetpack_compose_mediaplyer.model.MediaType
import com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.GalleryViewModel
import kotlinx.coroutines.launch


private const val TAG = "GalleryScreen"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {
    val mediaItems by viewModel.localMediaItems.collectAsState()
    val imageItems by viewModel.imageItems.collectAsState()
    val videoItems by viewModel.videoItems.collectAsState()
    val audioItems by viewModel.audioItems.collectAsState()

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    // State variable to track the selected media item
    var selectedLocalMediaItem by remember { mutableStateOf<LocalMediaItem?>(null) }
    var selectedLocalMediaItemIndex by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // Variable to keep track of the selected tab index
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Gallery") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            TabRow(
                modifier = Modifier
                    .fillMaxWidth(),
                selectedTabIndex = selectedTabIndex,
                contentColor = Color.Cyan

            ) {
                // Video Tab
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 }) { Text("Video") }

                // Audio Tab
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 }) { Text("Audio") }

                // Image Tab
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 }) { Text("Images") }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter media items based on the selected tab
            val filteredMediaItems = when (selectedTabIndex) {
                0 -> videoItems
                1 -> audioItems
                2 -> imageItems
                else -> mediaItems
            }

            if (showBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = {
                        showBottomSheet = false
                    },
                    sheetState = sheetState
                ) {
                    // Sheet content
                    BottomSheetContent(
                        viewModel = viewModel,
                        selectedLocalMediaItem = selectedLocalMediaItem!!,
                        selectedLocalMediaItemIndex = selectedLocalMediaItemIndex
                    )
                    Button(onClick = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }) {
                        Text("Hide bottom sheet")
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.padding(8.dp)
            ) {
                items(filteredMediaItems.size) { mediaIndex ->
                    val mediaItem = filteredMediaItems[mediaIndex]
                    val mediaIcon = when (mediaItem.type) {
                        MediaType.VIDEO -> Icons.Default.Movie
                        MediaType.AUDIO -> Icons.Default.MusicNote
                        MediaType.IMAGE -> Icons.Default.Image
                        else -> Icons.Default.Image
                    }

                    val previewResource = when (mediaItem.type) {
                        MediaType.VIDEO -> R.drawable.ic_video_placeholder
                        MediaType.AUDIO -> R.drawable.ic_audio_placeholder
                        MediaType.IMAGE -> filteredMediaItems[mediaIndex].data
                        else -> R.drawable.ic_placeholder // Handle unexpected types
                    }

                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .clickable {
                                // Update selected media item
                                selectedLocalMediaItem = mediaItem
                                selectedLocalMediaItemIndex = mediaIndex
                                showBottomSheet = true
                                // Open the bottom sheet
                                scope.launch {
                                    sheetState.show()
                                }
                            },
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 10.dp
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Add media icon
                            Icon(
                                imageVector = mediaIcon,
                                contentDescription = mediaItem.displayName,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(4.dp)
                            )

                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(previewResource)
                                    .placeholder(R.drawable.ic_image_placeholder)
                                    .build(),
                                contentDescription = "",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .aspectRatio(1f)
                            )

                            Text(
                                text = mediaItem.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 2,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                softWrap = true,
                                style = TextStyle(
                                    color = Color.White,
                                    shadow = Shadow(
                                        color = Color.Black,
                                        offset = Offset(2f, 2f),
                                        blurRadius = 5f
                                    )
                                ),
                                modifier = Modifier
                                    .padding(6.dp)
                                    .align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomSheetContent(
    viewModel: GalleryViewModel,
    selectedLocalMediaItem: LocalMediaItem,
    selectedLocalMediaItemIndex: Int
) {
    // Your bottom sheet content, for example:
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
    ) {
        Text(text = selectedLocalMediaItem.displayName)

        when (selectedLocalMediaItem.type) {
            MediaType.VIDEO -> {
                VideoPlayerComponent(
                    viewModel = viewModel,
                    url = selectedLocalMediaItem.data,
                    index = selectedLocalMediaItemIndex
                )
            }

            MediaType.AUDIO -> {
                AudioPlayerComponent(
                    viewModel = viewModel,
                    url = selectedLocalMediaItem.data,
                    index = selectedLocalMediaItemIndex
                )
            }

            MediaType.IMAGE -> {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(selectedLocalMediaItem.data)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .build(),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                )
            }

            else -> {
                // Handle unexpected types
            }
        }
    }
}


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun VideoPlayerComponent(viewModel: GalleryViewModel, url: String, index: Int) {
    // Create an ExoPlayer instance
    val exoPlayer = viewModel.exoPlayer
    val songs = viewModel.videoItems.value
    val selectedSong = remember { mutableStateOf(songs[0]) }
    // Display the video player
    Column {
        MusicPlayer(song = selectedSong.value, exoPlayer, viewModel)

        DropdownMenu(
            expanded = false,
            onDismissRequest = {})
        {
            songs.forEach { song ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = song.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        selectedSong.value = song
                        // Add navigation or playback actions
                    }
                )
            }
        }

    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AudioPlayerComponent(viewModel: GalleryViewModel, url: String, index: Int) {
    // Create an ExoPlayer instance
    val exoPlayer = viewModel.exoPlayer
    val songs = viewModel.audioItems.value
    val selectedSong = remember { mutableStateOf(songs[0]) }
    // Display the video player
    Column {
        MusicPlayer(song = selectedSong.value, exoPlayer, viewModel)

        DropdownMenu(
            expanded = false,
            onDismissRequest = {})
        {
            songs.forEach { song ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = song.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        selectedSong.value = song
                        // Add navigation or playback actions
                        viewModel.seekTo(0)
                    }
                )
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayer(song: LocalMediaItem, exoPlayer: ExoPlayer, viewModel: GalleryViewModel) {

    val state = viewModel.playerState.value
    var listener: Player.Listener? = null
    viewModel.prepareMediaSource(song, LocalContext.current)
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
            exoPlayer.removeListener(listener!!)
        }
    }

    LaunchedEffect(song) {
        try {
            // Update states based on ExoPlayer events (onDispose to release resources)
            listener = object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    viewModel.handlePlayerError(error)
                }

                // Update other player states as needed
            }
            exoPlayer.addListener(listener!!)
        } catch (e: Exception) {
            viewModel.handlePlayerError(e)
        }
    }

    if (state.error != null) {
        // Display error message
        Text(text = "Error: ${state.error}", color = MaterialTheme.colorScheme.error)
    } else {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ... (previous Image, title, artist code)
            //... (next Image, title, artist code)


            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = state.currentPosition.toFloat() / state.duration.toFloat(),
                onValueChange = { viewModel.seekTo((it * state.duration).toLong()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {}) { // Replace with previous track function }
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous Track",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { viewModel.playPause() }) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = {}) { // Replace with next track function }
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next Track",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "${millisToFormattedTime(state.currentPosition)} / ${
                    millisToFormattedTime(
                        state.duration
                    )
                }",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

fun millisToFormattedTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}:${remainingSeconds}"
}



