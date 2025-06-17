package com.example.vibecoding

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Tests for JDK features compatibility
 */
class JdkUpgradeTest {

    @Test
    fun `test string templates`() {
        val name = "World"
        val greeting = "Hello, $name!"
        
        assertEquals("Hello, World!", greeting)
    }
    
    @Test
    fun `test collection operations`() {
        val numbers = listOf(1, 2, 3, 4, 5)
        val doubled = numbers.map { it * 2 }
        
        assertEquals(listOf(2, 4, 6, 8, 10), doubled)
    }
    
    @Test
    fun `test thread creation`() {
        val executed = AtomicBoolean(false)
        
        // Test thread creation (compatible with JDK 17)
        val thread = Thread { 
            executed.set(true)
        }
        thread.start()
        thread.join()
        
        assertTrue(executed.get())
    }
}

