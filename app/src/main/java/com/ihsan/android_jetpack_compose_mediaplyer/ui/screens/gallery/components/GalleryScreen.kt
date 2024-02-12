package com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.components

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.ihsan.android_jetpack_compose_mediaplyer.R
import com.ihsan.android_jetpack_compose_mediaplyer.model.LocalMediaItem
import com.ihsan.android_jetpack_compose_mediaplyer.model.MediaType
import com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.MainActivity
import com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.GalleryViewModel
import kotlinx.coroutines.launch


private const val TAG = "GalleryScreen"

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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

    val context = LocalContext.current

    // Variable to keep track of the selected tab index
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Gallery") }
            )
        }
    ) { contentPadding ->
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
                        selectedLocalMediaItem = selectedLocalMediaItem!!
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
fun BottomSheetContent(viewModel: GalleryViewModel, selectedLocalMediaItem: LocalMediaItem) {
    // Your bottom sheet content, for example:
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
    ) {
        Text(text = selectedLocalMediaItem.displayName)

        when (selectedLocalMediaItem.type) {
            MediaType.VIDEO -> {
                VideoPlayerComponent(viewModel = viewModel, url = selectedLocalMediaItem.data)
            }

            MediaType.AUDIO -> {
                AudioPlayerComponent(selectedLocalMediaItem.data)
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


@Composable
fun VideoPlayerComponent(viewModel: GalleryViewModel, url: String) {
    // Create an ExoPlayer instance
    val exoPlayer = viewModel.exoPlayer
    // create a media item.
    val mediaItem = MediaItem.Builder()
        .setUri(url)
        .build()

    // Create a media source and pass the media item
    val mediaSource = ProgressiveMediaSource.Factory(
        DefaultDataSource.Factory(MainActivity().applicationContext) // <- context
    )
        .createMediaSource(mediaItem)
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }
    // Prepare the media item
    exoPlayer.setMediaSource(mediaSource)
    exoPlayer.prepare()
    // Display the video player
    Column {
        PlayerView(MainActivity().applicationContext).apply {
            player = exoPlayer
            player?.playWhenReady = true
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING) // Optional: show buffering indicator
            this.setShowNextButton(true) // Optional: show next video button
        }
    }
}

@Composable
fun AudioPlayerComponent(url: String) {
//    val mediaItem = MediaItem.fromUri(Uri.parse(url))
//    val mediaSource = ProgressiveMediaSource.Factory(
//        DefaultDataSource.Factory(MainActivity().applicationContext) // <- context
//    )
//        .createMediaSource(mediaItem)
//    DisposableEffect(exoPlayer) {
//        onDispose {
//            exoPlayer.stop()
//            exoPlayer.release()
//        }
//    }
//
//    exoPlayer.setMediaSource(mediaSource)
    // Display controls without specific view for audio
    Column {
        // Add audio player controls here (e.g., play/pause, seek bar)
    }
}
