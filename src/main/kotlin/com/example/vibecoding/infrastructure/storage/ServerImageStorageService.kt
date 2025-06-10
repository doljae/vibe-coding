package com.example.vibecoding.infrastructure.storage

import com.example.vibecoding.domain.post.ImageStorageException
import com.example.vibecoding.domain.post.ImageStorageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Server-side implementation of ImageStorageService
 * Stores images in local file system
 */
@Service
class ServerImageStorageService(
    @Value("\${app.image.storage.path:./uploads/images}") 
    private val baseStoragePath: String
) : ImageStorageService {

    init {
        // Create storage directory if it doesn't exist
        val storageDir = File(baseStoragePath)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
    }

    override fun storeImage(filename: String, contentType: String, inputStream: InputStream): String {
        try {
            val storagePath = generateStoragePath(filename)
            val file = File(baseStoragePath, storagePath)
            
            // Create parent directories if they don't exist
            file.parentFile?.mkdirs()
            
            // Copy input stream to file
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            
            return storagePath
        } catch (e: Exception) {
            throw ImageStorageException("Failed to store image: ${e.message}", e)
        }
    }

    override fun retrieveImage(storagePath: String): InputStream {
        try {
            val file = File(baseStoragePath, storagePath)
            if (!file.exists()) {
                throw ImageStorageException("Image not found at path: $storagePath")
            }
            return FileInputStream(file)
        } catch (e: Exception) {
            if (e is ImageStorageException) throw e
            throw ImageStorageException("Failed to retrieve image: ${e.message}", e)
        }
    }

    override fun deleteImage(storagePath: String) {
        try {
            val file = File(baseStoragePath, storagePath)
            if (file.exists() && !file.delete()) {
                throw ImageStorageException("Failed to delete image at path: $storagePath")
            }
        } catch (e: Exception) {
            if (e is ImageStorageException) throw e
            throw ImageStorageException("Failed to delete image: ${e.message}", e)
        }
    }

    override fun imageExists(storagePath: String): Boolean {
        return try {
            val file = File(baseStoragePath, storagePath)
            file.exists() && file.isFile
        } catch (e: Exception) {
            false
        }
    }

    override fun getImageSize(storagePath: String): Long {
        try {
            val file = File(baseStoragePath, storagePath)
            if (!file.exists()) {
                throw ImageStorageException("Image not found at path: $storagePath")
            }
            return file.length()
        } catch (e: Exception) {
            if (e is ImageStorageException) throw e
            throw ImageStorageException("Failed to get image size: ${e.message}", e)
        }
    }

    /**
     * Generate a unique storage path for an image
     * Format: yyyy/MM/dd/uuid_originalFilename
     */
    private fun generateStoragePath(originalFilename: String): String {
        val now = LocalDateTime.now()
        val dateFolder = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val uniqueId = UUID.randomUUID().toString()
        val sanitizedFilename = sanitizeFilename(originalFilename)
        
        return "$dateFolder/${uniqueId}_$sanitizedFilename"
    }

    /**
     * Sanitize filename to prevent path traversal and invalid characters
     */
    private fun sanitizeFilename(filename: String): String {
        return filename
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(100) // Limit filename length
    }
}

