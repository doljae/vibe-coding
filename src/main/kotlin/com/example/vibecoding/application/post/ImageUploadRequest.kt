package com.example.vibecoding.application.post

import java.io.InputStream

/**
 * Data transfer object for image upload requests
 */
data class ImageUploadRequest(
    val filename: String,
    val contentType: String,
    val fileSizeBytes: Long,
    val inputStream: InputStream
) {
    init {
        require(filename.isNotBlank()) { "Filename cannot be blank" }
        require(contentType.isNotBlank()) { "Content type cannot be blank" }
        require(fileSizeBytes > 0) { "File size must be positive" }
    }
}

