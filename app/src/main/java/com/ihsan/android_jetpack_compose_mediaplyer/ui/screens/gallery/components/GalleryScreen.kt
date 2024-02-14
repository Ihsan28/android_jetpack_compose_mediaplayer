package com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.components

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.PopupProperties
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.ihsan.android_jetpack_compose_mediaplyer.R
import com.ihsan.android_jetpack_compose_mediaplyer.model.LocalMediaItem
import com.ihsan.android_jetpack_compose_mediaplyer.model.MediaType
import com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.GalleryViewModel
import com.skydoves.landscapist.rememberDrawablePainter
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
    var selectedLocalMediaItemIndex by remember { mutableIntStateOf(0) }

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
            Spacer(modifier = Modifier.height(60.dp))
            TabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
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
                        filteredMediaItems = filteredMediaItems,
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
                modifier = Modifier.padding(8.dp).fillMaxSize()
            ) {
                items(filteredMediaItems.size) { mediaIndex ->
                    val currentMediaItem = filteredMediaItems[mediaIndex]
                    val mediaIcon = when (currentMediaItem.type) {
                        MediaType.VIDEO -> Icons.Default.Movie
                        MediaType.AUDIO -> Icons.Default.MusicNote
                        MediaType.IMAGE -> Icons.Default.Image
                        else -> Icons.Default.Image
                    }

                    Card(
                        modifier = Modifier
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clickable {
                                // Update selected media item
                                selectedLocalMediaItem = currentMediaItem
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
                                contentDescription = currentMediaItem.displayName,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(4.dp)
                            )

                            GlideImage(mediaItem =currentMediaItem)

                            Text(
                                text = currentMediaItem.displayName,
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
fun GlideImage(mediaItem: LocalMediaItem, modifier: Modifier = Modifier,keepOriginalShape:Boolean=false) {
    // Holding image data with MutableState
    var image by remember { mutableStateOf<Drawable?>(null) }
    // Fetching the Context inside Compose using LocalContext.current
    val context = LocalContext.current
    val placeholder =when (mediaItem.type) {
        MediaType.VIDEO -> R.drawable.ic_video_placeholder
        MediaType.AUDIO -> R.drawable.ic_audio_placeholder
        MediaType.IMAGE -> R.drawable.ic_image_placeholder
        else -> R.drawable.ic_placeholder
    }

    val imageShape=if(keepOriginalShape) FitCenter() else CenterCrop()

    val customTarget=if(keepOriginalShape) {
        object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                image = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // Handle when the image load is cleared
            }
        }
    }else{
        object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                image = resource
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                // Handle when the image load is cleared
            }
        }
    }

    // Loading the image using the Glide library
    val glideRequest=Glide.with(context)
        .load(mediaItem.data)
        .transform(imageShape, RoundedCorners(8))
        .placeholder(placeholder)
        .error(R.drawable.ic_placeholder)
        .into(customTarget)


    // If there is an image, display it
    image?.let {
        Image(
            painter = rememberDrawablePainter(it),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium)
        )
    }
}

@Composable
fun BottomSheetContent(
    viewModel: GalleryViewModel,
    selectedLocalMediaItem: LocalMediaItem,
    filteredMediaItems: List<LocalMediaItem>,
    selectedLocalMediaItemIndex: Int
) {

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Text(text = selectedLocalMediaItem.displayName)

        when (selectedLocalMediaItem.type) {
            MediaType.VIDEO -> {
                VideoPlayerComponent(
                    viewModel = viewModel,
                    videoMediaItems = filteredMediaItems,
                    index = selectedLocalMediaItemIndex
                )
            }

            MediaType.AUDIO -> {
                AudioPlayerComponent(
                    viewModel = viewModel,
                    audioMediaItems = filteredMediaItems,
                    index = selectedLocalMediaItemIndex
                )
            }

            MediaType.IMAGE -> {
                GlideImage(mediaItem =selectedLocalMediaItem,modifier = Modifier.fillMaxSize(),keepOriginalShape = true)
            }

            else -> {
                // Handle unexpected types
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun VideoPlayerComponent(
    viewModel: GalleryViewModel,
    videoMediaItems: List<LocalMediaItem>,
    index: Int
) {
    val selectedSong = remember { mutableStateOf(videoMediaItems[index]) }
    var expanded by remember { mutableStateOf(false) }
    // Display the video player
    Box {
        MusicPlayer(song = selectedSong.value, viewModel)
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More"
            )
        }
        DropdownMenu(
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                clippingEnabled = true
            ),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            })
        {
            videoMediaItems.forEach { song ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = song.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        selectedSong.value = song

                    }
                )
            }
        }

    }
}

@Composable
fun AudioPlayerComponent(
    viewModel: GalleryViewModel,
    audioMediaItems: List<LocalMediaItem>,
    index: Int
) {
    val selectedSong = remember { mutableStateOf(audioMediaItems[index]) }
    var expanded by remember { mutableStateOf(false) }
    // Display the video player
    Box {
        MusicPlayer(song = selectedSong.value, viewModel)
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More"
            )
        }
        DropdownMenu(
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                clippingEnabled = true
            ),
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            })
        {
            audioMediaItems.forEach { song ->
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
                        viewModel.seekToPlayer(0)
                    }
                )
            }
        }
    }
}

@Composable
fun MusicPlayer(song: LocalMediaItem, viewModel: GalleryViewModel) {

    val exoPlayer = viewModel.exoPlayer
    val playerView = PlayerView(LocalContext.current)
    val playerState = viewModel.playerState.value
    val context = LocalContext.current
    LaunchedEffect(exoPlayer) {
        viewModel.prepareMediaSource(song, context)
        playerView.player = exoPlayer
        Log.d(TAG, "MusicPlayer: $song")
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            viewModel.disposePlayer()
        }
    }

    AndroidView(factory = { playerView }, modifier = Modifier.fillMaxWidth())

//    if (playerState.error != null) {
//        // Display error message
//        Text(text = "Error: ${playerState.error}", color = MaterialTheme.colorScheme.error)
//    } else {
//        Column(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            // ... (previous Image, title, artist code)
//            //... (next Image, title, artist code)
//
//
//            Spacer(modifier = Modifier.height(18.dp))
//            Slider(
//                value = playerState.currentPosition.toFloat() / playerState.duration.toFloat()
//                    .coerceAtLeast(1f),
//                onValueChange = {
//                    viewModel.seekToPlayer((it * playerState.duration).toLong())
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
//            )
//
//            Spacer(modifier = Modifier.height(18.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                IconButton(onClick = {}) { // Replace with previous track function }
//                    Icon(
//                        imageVector = Icons.Filled.SkipPrevious,
//                        contentDescription = "Previous Track",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                IconButton(onClick = { viewModel.playPause() }) {
//                    Icon(
//                        imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
//                        contentDescription = "Play/Pause",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//
//                IconButton(onClick = {}) { // Replace with next track function }
//                    Icon(
//                        imageVector = Icons.Filled.SkipNext,
//                        contentDescription = "Next Track",
//                        tint = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            Text(
//                text = "${millisToFormattedTime(playerState.currentPosition)} / ${
//                    millisToFormattedTime(
//                        playerState.duration
//                    )
//                }",
//                modifier = Modifier.align(Alignment.CenterHorizontally),
//                style = MaterialTheme.typography.bodySmall
//            )
//        }
//    }
}

fun millisToFormattedTime(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "${minutes}:${remainingSeconds}"
}