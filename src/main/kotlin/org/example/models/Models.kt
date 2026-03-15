package org.example.models

import kotlinx.serialization.Serializable

@Serializable
data class AppSession(val placeholder: String? = null)

@Serializable
data class ImageMetadata(
    val name: String,
    val width: Int = 0,
    val height: Int = 0,
    val isFavorite: Boolean = false,
    val starCount: Int = 0,
    val isFlagged: Boolean = false,
    val lastModified: Long = 0
)

@Serializable
data class ImageList(val images: List<ImageMetadata>, val totalCount: Int)

@Serializable
data class FavoriteRequest(val path: String, val isFavorite: Boolean)

@Serializable
data class StarRequest(val path: String)

@Serializable
data class FlagRequest(val path: String, val isFlagged: Boolean)

@Serializable
data class AdminConfig(val dir: String, val thumbnailQuality: Int = 80)
