package com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.components

//noinspection UsingMaterialAndMaterial3Libraries
import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import android.widget.StackView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.ihsan.android_jetpack_compose_mediaplyer.R
import com.ihsan.android_jetpack_compose_mediaplyer.model.LocalMediaItem
import com.ihsan.android_jetpack_compose_mediaplyer.model.MediaType
import com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.MainActivity
import com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.GalleryViewModel


private const val TAG = "GalleryScreen"
//private val exoPlayer = ExoPlayer.Builder(MainActivity().applicationContext).build()

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(viewModel: GalleryViewModel) {
    val mediaItems by viewModel.localMediaItems.collectAsState()
    val imageItems by viewModel.imageItems.collectAsState()
    val videoItems by viewModel.videoItems.collectAsState()
    val audioItems by viewModel.audioItems.collectAsState()

    // State variable to track whether the bottom sheet is open
    var bottomSheetState by remember { mutableStateOf(false) }
    // State variable to track the selected media item
    var selectedLocalMediaItem by remember { mutableStateOf<LocalMediaItem?>(null) }
    Log.d(TAG, "GalleryScreen: $mediaItems")

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

            // Show bottom sheet when a media item is clicked
//            if (selectedLocalMediaItem != null) {
//                ModalBottomSheetLayout(
//                    sheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden),
//                    sheetContent = {
//                        // Content of the bottom sheet
//                        //BottomSheetContent(selectedLocalMediaItem = selectedLocalMediaItem)
//                    }
//                ) {
//                    // Existing code...
//
//                }
//            }

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
                        MediaType.IMAGE -> R.drawable.ic_image_placeholder
                        else -> R.drawable.ic_placeholder // Handle unexpected types
                    }

                    Card(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxWidth()
                            .clickable {
                                // Update selected media item
                                selectedLocalMediaItem = mediaItem
                                // Open the bottom sheet
                                bottomSheetState = true
                            },
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 10.dp
                        )
                    ) {
                        StackView(context).apply {
                            // Add media icon
                            Icon(
                                imageVector = mediaIcon,
                                contentDescription = mediaItem.displayName,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(4.dp)
                            )

                            Image(
                                painter = painterResource(id = previewResource),
                                contentDescription = null, // Set descriptive contentDescription
                                modifier = Modifier.fillMaxSize() // Adjust size as needed
                            )

                            Text(
                                text = mediaItem.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                maxLines = 2,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun BottomSheetContent(selectedLocalMediaItem: LocalMediaItem?) {
//    val context = LocalContext.current
//
//    if (selectedLocalMediaItem != null) {
//        val mediaType = selectedLocalMediaItem.type
//        Column(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(
//                text = "Selected Media: ${selectedLocalMediaItem.displayName}",
//                fontSize = 18.sp,
//                modifier = Modifier.padding(16.dp),
//                color = Color.Black
//            )
//            when (mediaType) {
//                MediaType.VIDEO -> {
//                    VideoPlayerComponent(url = selectedLocalMediaItem.data) // Assuming you have a VideoPlayerComponent
//                }
//
//                MediaType.AUDIO -> {
//                    AudioPlayerComponent(url = selectedLocalMediaItem.data) // Assuming you have an AudioPlayerComponent
//                }
//
//                MediaType.IMAGE -> {
//                    Image(
//                        painter = painterResource(id = R.drawable.ic_image_placeholder), // Use a placeholder while loading
//                        contentDescription = selectedLocalMediaItem.displayName,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .aspectRatio(4 / 3f) // Adjust based on image aspect ratio
//                        // Load and display the actual image here (not shown)
//                    )
//                }
//
//                else -> {
//                    Text(text = "Unsupported media type")
//                }
//            }
//            // Add more options/buttons as needed
//        }
//    } else {
//        // Show loading indicator or "No media selected" message
//    }
//}
//
//@Composable
//fun VideoPlayerComponent(url: String) {
//    // create a media item.
//    val mediaItem = MediaItem.Builder()
//        .setUri(url)
//        .build()
//
//    // Create a media source and pass the media item
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
//    // Prepare the media item
//    exoPlayer.setMediaSource(mediaSource)
//
//    // Configure PlayerView (using a custom layout with controls)
//    val playerView = PlayerView(MainActivity().applicationContext)
//    playerView.player = exoPlayer
//    playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING) // Optional: show buffering indicator
//    //playerView.setShowNext() // Optional: show next video button
//    // Add more configuration and controls as needed
//
//    Column {
//        // Display PlayerView or custom layout with controls
//        //playerView.height= 300.dp
//
//        // Additional controls can be added here (e.g., seek bar, volume)
//    }
//}
//
//@Composable
//fun AudioPlayerComponent(url: String) {
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
//    // Display controls without specific view for audio
//    Column {
//        // Add audio player controls here (e.g., play/pause, seek bar)
//    }
//}
