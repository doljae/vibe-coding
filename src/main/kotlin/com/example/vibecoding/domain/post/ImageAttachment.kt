package com.example.vibecoding.domain.post

import java.time.LocalDateTime
import java.util.*

/**
 * Value object representing an image attachment for a post
 */
data class ImageAttachment(
    val id: ImageId,
    val filename: String,
    val storagePath: String,
    val contentType: String,
    val fileSizeBytes: Long,
    val uploadedAt: LocalDateTime
) {
    init {
        require(filename.isNotBlank()) { "Image filename cannot be blank" }
        require(filename.length <= 255) { "Image filename cannot exceed 255 characters" }
        require(storagePath.isNotBlank()) { "Image storage path cannot be blank" }
        require(contentType.isNotBlank()) { "Image content type cannot be blank" }
        require(isValidImageContentType(contentType)) { "Invalid image content type: $contentType" }
        require(fileSizeBytes > 0) { "Image file size must be positive" }
        require(fileSizeBytes <= MAX_FILE_SIZE_BYTES) { "Image file size cannot exceed ${MAX_FILE_SIZE_BYTES / (1024 * 1024)}MB" }
    }

    companion object {
        const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L // 10MB
        private val VALID_CONTENT_TYPES = setOf(
            "image/jpeg",
            "image/jpg", 
            "image/png",
            "image/gif",
            "image/webp"
        )

        fun create(
            filename: String,
            storagePath: String,
            contentType: String,
            fileSizeBytes: Long
        ): ImageAttachment {
            return ImageAttachment(
                id = ImageId.generate(),
                filename = filename,
                storagePath = storagePath,
                contentType = contentType,
                fileSizeBytes = fileSizeBytes,
                uploadedAt = LocalDateTime.now()
            )
        }

        private fun isValidImageContentType(contentType: String): Boolean {
            return VALID_CONTENT_TYPES.contains(contentType.lowercase())
        }
    }

    /**
     * Get file extension from filename
     */
    fun getFileExtension(): String {
        return filename.substringAfterLast('.', "")
    }

    /**
     * Check if the image is of a specific type
     */
    fun isOfType(contentType: String): Boolean {
        return this.contentType.equals(contentType, ignoreCase = true)
    }

    /**
     * Get human-readable file size
     */
    fun getFormattedFileSize(): String {
        return when {
            fileSizeBytes < 1024 -> "${fileSizeBytes}B"
            fileSizeBytes < 1024 * 1024 -> "${fileSizeBytes / 1024}KB"
            else -> "${fileSizeBytes / (1024 * 1024)}MB"
        }
    }
}

/**
 * Value object for Image ID
 */
@JvmInline
value class ImageId(val value: UUID) {
    companion object {
        fun generate(): ImageId = ImageId(UUID.randomUUID())
        fun from(value: String): ImageId = ImageId(UUID.fromString(value))
    }
}

