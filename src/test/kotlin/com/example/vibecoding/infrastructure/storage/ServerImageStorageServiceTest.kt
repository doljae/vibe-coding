package com.example.vibecoding.infrastructure.storage

import com.example.vibecoding.domain.post.ImageStorageException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Path

class ServerImageStorageServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var storageService: ServerImageStorageService
    private lateinit var testImageData: ByteArray

    @BeforeEach
    fun setUp() {
        storageService = ServerImageStorageService(tempDir.toString())
        testImageData = "fake image data".toByteArray()
    }

    @AfterEach
    fun tearDown() {
        // Cleanup is handled by @TempDir
    }

    @Test
    fun `should store image successfully`() {
        // Given
        val filename = "test-image.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)

        // When
        val storagePath = storageService.storeImage(filename, contentType, inputStream)

        // Then
        storagePath shouldNotBe null
        storagePath shouldContain filename
        storageService.imageExists(storagePath) shouldBe true
    }

    @Test
    fun `should retrieve stored image`() {
        // Given
        val filename = "test-image.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)
        val storagePath = storageService.storeImage(filename, contentType, inputStream)

        // When
        val retrievedStream = storageService.retrieveImage(storagePath)
        val retrievedData = retrievedStream.readBytes()

        // Then
        retrievedData shouldBe testImageData
    }

    @Test
    fun `should throw exception when retrieving non-existent image`() {
        // Given
        val nonExistentPath = "non/existent/path.jpg"

        // When & Then
        shouldThrow<ImageStorageException> {
            storageService.retrieveImage(nonExistentPath)
        }
    }

    @Test
    fun `should delete image successfully`() {
        // Given
        val filename = "test-image.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)
        val storagePath = storageService.storeImage(filename, contentType, inputStream)

        // Verify image exists
        storageService.imageExists(storagePath) shouldBe true

        // When
        storageService.deleteImage(storagePath)

        // Then
        storageService.imageExists(storagePath) shouldBe false
    }

    @Test
    fun `should not throw exception when deleting non-existent image`() {
        // Given
        val nonExistentPath = "non/existent/path.jpg"

        // When & Then (should not throw)
        storageService.deleteImage(nonExistentPath)
    }

    @Test
    fun `should check image existence correctly`() {
        // Given
        val filename = "test-image.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)

        // When
        val existsBefore = storageService.imageExists("non/existent/path.jpg")
        val storagePath = storageService.storeImage(filename, contentType, inputStream)
        val existsAfter = storageService.imageExists(storagePath)

        // Then
        existsBefore shouldBe false
        existsAfter shouldBe true
    }

    @Test
    fun `should get image size correctly`() {
        // Given
        val filename = "test-image.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)
        val storagePath = storageService.storeImage(filename, contentType, inputStream)

        // When
        val size = storageService.getImageSize(storagePath)

        // Then
        size shouldBe testImageData.size.toLong()
    }

    @Test
    fun `should throw exception when getting size of non-existent image`() {
        // Given
        val nonExistentPath = "non/existent/path.jpg"

        // When & Then
        shouldThrow<ImageStorageException> {
            storageService.getImageSize(nonExistentPath)
        }
    }

    @Test
    fun `should create storage directory if it does not exist`() {
        // Given
        val newStorageDir = tempDir.resolve("new-storage").toString()
        val newStorageService = ServerImageStorageService(newStorageDir)

        // When
        val filename = "test-image.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)
        val storagePath = newStorageService.storeImage(filename, contentType, inputStream)

        // Then
        File(newStorageDir).exists() shouldBe true
        newStorageService.imageExists(storagePath) shouldBe true
    }

    @Test
    fun `should generate unique storage paths for same filename`() {
        // Given
        val filename = "test-image.jpg"
        val contentType = "image/jpeg"

        // When
        val storagePath1 = storageService.storeImage(filename, contentType, ByteArrayInputStream(testImageData))
        val storagePath2 = storageService.storeImage(filename, contentType, ByteArrayInputStream(testImageData))

        // Then
        storagePath1 shouldNotBe storagePath2
        storageService.imageExists(storagePath1) shouldBe true
        storageService.imageExists(storagePath2) shouldBe true
    }

    @Test
    fun `should sanitize filename with special characters`() {
        // Given
        val filename = "test image with spaces & special chars!.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)

        // When
        val storagePath = storageService.storeImage(filename, contentType, inputStream)

        // Then
        storageService.imageExists(storagePath) shouldBe true
        // Storage path should contain sanitized version
        storagePath shouldContain "_"
    }

    @Test
    fun `should organize images by date in storage path`() {
        // Given
        val filename = "test-image.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)

        // When
        val storagePath = storageService.storeImage(filename, contentType, inputStream)

        // Then
        // Storage path should contain date structure (yyyy/MM/dd)
        val pathParts = storagePath.split("/")
        pathParts.size shouldBe 4 // yyyy/MM/dd/filename
        pathParts[0].length shouldBe 4 // year
        pathParts[1].length shouldBe 2 // month
        pathParts[2].length shouldBe 2 // day
    }

    @Test
    fun `should handle large filenames by truncating`() {
        // Given
        val longFilename = "a".repeat(200) + ".jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(testImageData)

        // When
        val storagePath = storageService.storeImage(longFilename, contentType, inputStream)

        // Then
        storageService.imageExists(storagePath) shouldBe true
        // The actual stored filename should be truncated
        val actualFilename = storagePath.substringAfterLast("/")
        actualFilename.length shouldBe 137 // UUID (36) + underscore (1) + truncated filename (100)
    }

    @Test
    fun `should handle empty input stream`() {
        // Given
        val filename = "empty-image.jpg"
        val contentType = "image/jpeg"
        val inputStream = ByteArrayInputStream(byteArrayOf())

        // When
        val storagePath = storageService.storeImage(filename, contentType, inputStream)

        // Then
        storageService.imageExists(storagePath) shouldBe true
        storageService.getImageSize(storagePath) shouldBe 0L
    }
}

