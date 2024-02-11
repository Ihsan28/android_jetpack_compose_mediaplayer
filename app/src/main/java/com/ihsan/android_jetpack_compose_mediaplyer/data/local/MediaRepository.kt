package com.ihsan.android_jetpack_compose_mediaplyer.data.local

import android.content.ContentResolver
import android.provider.MediaStore
import android.util.Log
import com.ihsan.android_jetpack_compose_mediaplyer.model.LocalMediaItem
import com.ihsan.android_jetpack_compose_mediaplyer.model.MediaType

private const val TAG = "MediaRepository"

class MediaRepository(private val contentResolver: ContentResolver) {

    fun getAllMedia(): List<LocalMediaItem> {
        val mediaList = mutableListOf<LocalMediaItem>()

        // Projection for querying media files
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATA
        )

        // Selection to get only media files
        val selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO} OR " +
                "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO} OR " +
                "${MediaStore.Files.FileColumns.MEDIA_TYPE}=${MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE}"

        contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val mediaTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
                val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                //audio preview image column
                //val audioPreviewImageColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)



                do {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn) ?: "Unknown"
                    Log.d(TAG, "displayName: $displayName")
                    val mediaType = cursor.getInt(mediaTypeColumn)
                    Log.d(TAG, "mediaType: ${getMediaType(mediaType)}")
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val size = cursor.getLong(sizeColumn)
                    val data = cursor.getString(dataColumn)

                    val localMediaItem =
                        LocalMediaItem(id, getMediaType(mediaType), displayName, dateAdded, size, data)
                    mediaList.add(localMediaItem)

                    // Log information about the media item
                    Log.d(TAG, "MediaItem: $localMediaItem")
                } while (cursor.moveToNext())
            }
        }

        Log.d(TAG, "getAllMedia: ${mediaList.size}")

        return mediaList
    }

    private fun getMediaType(mediaType: Int): MediaType {
        return when (mediaType) {
            MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO -> MediaType.AUDIO
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> MediaType.VIDEO
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE -> MediaType.IMAGE
            else -> MediaType.UNKNOWN
        }
    }
}
