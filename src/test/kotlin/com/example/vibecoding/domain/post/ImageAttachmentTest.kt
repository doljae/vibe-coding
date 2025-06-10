package com.example.vibecoding.domain.post

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ImageAttachmentTest {

    @Test
    fun `should create image attachment with valid data`() {
        // Given
        val filename = "test-image.jpg"
        val storagePath = "2024/01/01/uuid_test-image.jpg"
        val contentType = "image/jpeg"
        val fileSizeBytes = 1024L

        // When
        val imageAttachment = ImageAttachment.create(
            filename = filename,
            storagePath = storagePath,
            contentType = contentType,
            fileSizeBytes = fileSizeBytes
        )

        // Then
        imageAttachment.filename shouldBe filename
        imageAttachment.storagePath shouldBe storagePath
        imageAttachment.contentType shouldBe contentType
        imageAttachment.fileSizeBytes shouldBe fileSizeBytes
        imageAttachment.id shouldNotBe null
        imageAttachment.uploadedAt shouldNotBe null
    }

    @Test
    fun `should throw exception when filename is blank`() {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ImageAttachment.create(
                filename = "",
                storagePath = "path/to/image.jpg",
                contentType = "image/jpeg",
                fileSizeBytes = 1024L
            )
        }
    }

    @Test
    fun `should throw exception when filename exceeds 255 characters`() {
        // Given
        val longFilename = "a".repeat(256) + ".jpg"

        // When & Then
        shouldThrow<IllegalArgumentException> {
            ImageAttachment.create(
                filename = longFilename,
                storagePath = "path/to/image.jpg",
                contentType = "image/jpeg",
                fileSizeBytes = 1024L
            )
        }
    }

    @Test
    fun `should throw exception when storage path is blank`() {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ImageAttachment.create(
                filename = "test.jpg",
                storagePath = "",
                contentType = "image/jpeg",
                fileSizeBytes = 1024L
            )
        }
    }

    @Test
    fun `should throw exception when content type is blank`() {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ImageAttachment.create(
                filename = "test.jpg",
                storagePath = "path/to/image.jpg",
                contentType = "",
                fileSizeBytes = 1024L
            )
        }
    }

    @Test
    fun `should throw exception when content type is invalid`() {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ImageAttachment.create(
                filename = "test.txt",
                storagePath = "path/to/file.txt",
                contentType = "text/plain",
                fileSizeBytes = 1024L
            )
        }
    }

    @Test
    fun `should accept valid image content types`() {
        val validContentTypes = listOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp",
            "IMAGE/JPEG" // Case insensitive
        )

        validContentTypes.forEach { contentType ->
            // Should not throw exception
            ImageAttachment.create(
                filename = "test.jpg",
                storagePath = "path/to/image.jpg",
                contentType = contentType,
                fileSizeBytes = 1024L
            )
        }
    }

    @Test
    fun `should throw exception when file size is zero or negative`() {
        // When & Then
        shouldThrow<IllegalArgumentException> {
            ImageAttachment.create(
                filename = "test.jpg",
                storagePath = "path/to/image.jpg",
                contentType = "image/jpeg",
                fileSizeBytes = 0L
            )
        }

        shouldThrow<IllegalArgumentException> {
            ImageAttachment.create(
                filename = "test.jpg",
                storagePath = "path/to/image.jpg",
                contentType = "image/jpeg",
                fileSizeBytes = -1L
            )
        }
    }

    @Test
    fun `should throw exception when file size exceeds maximum`() {
        // Given
        val oversizedFile = ImageAttachment.MAX_FILE_SIZE_BYTES + 1

        // When & Then
        shouldThrow<IllegalArgumentException> {
            ImageAttachment.create(
                filename = "large-image.jpg",
                storagePath = "path/to/image.jpg",
                contentType = "image/jpeg",
                fileSizeBytes = oversizedFile
            )
        }
    }

    @Test
    fun `should accept maximum allowed file size`() {
        // Given
        val maxAllowedSize = ImageAttachment.MAX_FILE_SIZE_BYTES

        // When
        val imageAttachment = ImageAttachment.create(
            filename = "max-size-image.jpg",
            storagePath = "path/to/image.jpg",
            contentType = "image/jpeg",
            fileSizeBytes = maxAllowedSize
        )

        // Then
        imageAttachment.fileSizeBytes shouldBe maxAllowedSize
    }

    @Test
    fun `should get file extension correctly`() {
        // Given
        val imageAttachment = ImageAttachment.create(
            filename = "test-image.jpg",
            storagePath = "path/to/image.jpg",
            contentType = "image/jpeg",
            fileSizeBytes = 1024L
        )

        // When & Then
        imageAttachment.getFileExtension() shouldBe "jpg"
    }

    @Test
    fun `should handle filename without extension`() {
        // Given
        val imageAttachment = ImageAttachment.create(
            filename = "test-image",
            storagePath = "path/to/image",
            contentType = "image/jpeg",
            fileSizeBytes = 1024L
        )

        // When & Then
        imageAttachment.getFileExtension() shouldBe ""
    }

    @Test
    fun `should check content type correctly`() {
        // Given
        val imageAttachment = ImageAttachment.create(
            filename = "test.jpg",
            storagePath = "path/to/image.jpg",
            contentType = "image/jpeg",
            fileSizeBytes = 1024L
        )

        // When & Then
        imageAttachment.isOfType("image/jpeg") shouldBe true
        imageAttachment.isOfType("IMAGE/JPEG") shouldBe true
        imageAttachment.isOfType("image/png") shouldBe false
    }

    @Test
    fun `should format file size correctly`() {
        // Test bytes
        val bytesImage = ImageAttachment.create(
            filename = "small.jpg",
            storagePath = "path/small.jpg",
            contentType = "image/jpeg",
            fileSizeBytes = 512L
        )
        bytesImage.getFormattedFileSize() shouldBe "512B"

        // Test kilobytes
        val kbImage = ImageAttachment.create(
            filename = "medium.jpg",
            storagePath = "path/medium.jpg",
            contentType = "image/jpeg",
            fileSizeBytes = 2048L
        )
        kbImage.getFormattedFileSize() shouldBe "2KB"

        // Test megabytes
        val mbImage = ImageAttachment.create(
            filename = "large.jpg",
            storagePath = "path/large.jpg",
            contentType = "image/jpeg",
            fileSizeBytes = 2 * 1024 * 1024L
        )
        mbImage.getFormattedFileSize() shouldBe "2MB"
    }

    @Test
    fun `should generate unique image IDs`() {
        // When
        val id1 = ImageId.generate()
        val id2 = ImageId.generate()

        // Then
        id1 shouldNotBe id2
    }

    @Test
    fun `should create image ID from string`() {
        // Given
        val uuidString = "123e4567-e89b-12d3-a456-426614174000"

        // When
        val imageId = ImageId.from(uuidString)

        // Then
        imageId.value.toString() shouldBe uuidString
    }

    @Test
    fun `should create image attachment with all fields properly set`() {
        // Given
        val id = ImageId.generate()
        val filename = "test.png"
        val storagePath = "2024/01/01/uuid_test.png"
        val contentType = "image/png"
        val fileSizeBytes = 2048L
        val uploadedAt = LocalDateTime.now()

        // When
        val imageAttachment = ImageAttachment(
            id = id,
            filename = filename,
            storagePath = storagePath,
            contentType = contentType,
            fileSizeBytes = fileSizeBytes,
            uploadedAt = uploadedAt
        )

        // Then
        imageAttachment.id shouldBe id
        imageAttachment.filename shouldBe filename
        imageAttachment.storagePath shouldBe storagePath
        imageAttachment.contentType shouldBe contentType
        imageAttachment.fileSizeBytes shouldBe fileSizeBytes
        imageAttachment.uploadedAt shouldBe uploadedAt
    }
}

