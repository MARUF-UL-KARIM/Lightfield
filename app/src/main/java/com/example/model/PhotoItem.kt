package com.example.model

import android.net.Uri

data class PhotoItem(
    val id: String,
    val uri: Uri? = null,
    val resourceId: Int? = null,
    val title: String,
    val dateAdded: Long = System.currentTimeMillis(),
    val iso: Int = 100,
    val shutterSpeed: String = "1/250s",
    val aperture: String = "f/2.8",
    val focalLength: String = "35mm",
    val cameraModel: String = "Sony Alpha A7 IV",
    val width: Int = 1920,
    val height: Int = 1080,
    val rating: Int = 0, // 0 to 5 stars
    val isFlagged: Boolean = false,
    val album: String = "All Photos",
    val isRaw: Boolean = false,
    val editState: EditState = EditState(photoId = id)
)

data class PresetItem(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val editState: EditState
)
