package com.example.vibecoding.domain.comment

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.UUID

class CommentIdTest {

    @Test
    fun `should create CommentId with random UUID`() {
        // When
        val commentId = CommentId.generate()
        
        // Then
        assertNotNull(commentId)
        assertNotNull(commentId.value)
    }
    
    @Test
    fun `should create CommentId with specific UUID`() {
        // Given
        val uuid = UUID.randomUUID()
        
        // When
        val commentId = CommentId(uuid)
        
        // Then
        assertEquals(uuid, commentId.value)
    }
    
    @Test
    fun `should create CommentId from string`() {
        // Given
        val uuidString = "550e8400-e29b-41d4-a716-446655440000"
        val uuid = UUID.fromString(uuidString)
        
        // When
        val commentId = CommentId.from(uuidString)
        
        // Then
        assertEquals(uuid, commentId.value)
    }
    
    @Test
    fun `should throw exception when creating CommentId from invalid string`() {
        // Given
        val invalidUuidString = "not-a-uuid"
        
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            CommentId.from(invalidUuidString)
        }
    }
    
    @Test
    fun `should compare CommentIds correctly`() {
        // Given
        val uuid = UUID.randomUUID()
        val commentId1 = CommentId(uuid)
        val commentId2 = CommentId(uuid)
        val commentId3 = CommentId.generate()
        
        // Then
        assertEquals(commentId1, commentId2)
        assertNotEquals(commentId1, commentId3)
        assertEquals(commentId1.hashCode(), commentId2.hashCode())
    }
    
    @Test
    fun `should convert CommentId to string correctly`() {
        // Given
        val uuid = UUID.randomUUID()
        val commentId = CommentId(uuid)
        
        // When
        val commentIdString = commentId.toString()
        
        // Then
        assertTrue(commentIdString.contains(uuid.toString()))
    }
}

