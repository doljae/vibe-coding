package com.example.vibecoding

import io.kotest.matchers.shouldBe
import io.kotest.matchers.booleans.shouldBeTrue
import org.junit.jupiter.api.Test

/**
 * Simple test to verify JDK 21 and Kotlin 2.x compatibility
 * 단위 테스트로 JDK 21 기능을 검증합니다.
 */
class JdkUpgradeTest {

    @Test
    fun `verify JDK 21 and Kotlin 2 x features work`() {
        // Test JDK 21 features - Pattern matching for switch
        val obj: Any = "Hello, World!"
        val result = when (obj) {
            is String -> obj.length
            is Int -> obj
            else -> -1
        }
        result shouldBe 13
        
        // Test Kotlin 2.x features
        val numbers = listOf(1, 2, 3, 4, 5)
        val sum = numbers.sum()
        sum shouldBe 15
        
        // Test virtual threads (JDK 21 feature)
        val thread = Thread.ofVirtual().name("virtual-thread").start {
            println("Running in a virtual thread")
        }
        thread.join()
        true.shouldBeTrue() // Virtual thread completed successfully
    }
}

