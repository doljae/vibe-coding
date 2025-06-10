package com.example.vibecoding.domain.post

import java.io.InputStream

/**
 * Domain service interface for image storage operations
 */
interface ImageStorageService {
    
    /**
     * Store an image and return the storage path
     * @param filename Original filename
     * @param contentType MIME type of the image
     * @param inputStream Image data stream
     * @return Storage path where the image was saved
     * @throws ImageStorageException if storage operation fails
     */
    fun storeImage(filename: String, contentType: String, inputStream: InputStream): String
    
    /**
     * Retrieve an image as input stream
     * @param storagePath Path where the image is stored
     * @return Input stream of the image data
     * @throws ImageStorageException if image not found or retrieval fails
     */
    fun retrieveImage(storagePath: String): InputStream
    
    /**
     * Delete an image from storage
     * @param storagePath Path where the image is stored
     * @throws ImageStorageException if deletion fails
     */
    fun deleteImage(storagePath: String)
    
    /**
     * Check if an image exists in storage
     * @param storagePath Path where the image should be stored
     * @return true if image exists, false otherwise
     */
    fun imageExists(storagePath: String): Boolean
    
    /**
     * Get the size of an image in bytes
     * @param storagePath Path where the image is stored
     * @return Size in bytes
     * @throws ImageStorageException if image not found
     */
    fun getImageSize(storagePath: String): Long
}

/**
 * Exception thrown when image storage operations fail
 */
class ImageStorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

