package com.ihsan.android_jetpack_compose_mediaplyer.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.GalleryViewModel
import com.ihsan.android_jetpack_compose_mediaplyer.ui.screens.gallery.components.GalleryScreen
import com.ihsan.android_jetpack_compose_mediaplyer.ui.theme.Android_jetpack_compose_mediaplyerTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // Permission granted, show the GalleryScreen
            setContent {
                Android_jetpack_compose_mediaplyerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        GalleryScreen(viewModel = GalleryViewModel(applicationContext))
                    }
                }
            }
        } else {
            // Permission denied, handle accordingly (e.g., show an error message)snackbar
            // You can implement your own logic here
            // For simplicity, we'll just finish the activity
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, show the GalleryScreen
            setContent {
                Android_jetpack_compose_mediaplyerTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        GalleryScreen(viewModel = GalleryViewModel(applicationContext))
                    }
                }
            }
        } else {
            // Permission not granted, request the permission
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
