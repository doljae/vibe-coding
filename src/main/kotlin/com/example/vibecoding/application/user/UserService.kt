package com.example.vibecoding.application.user

import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Application service for User domain operations
 */
@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun createUser(username: String, email: String, displayName: String, bio: String? = null): User {
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username '$username' already exists")
        }
        
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email '$email' already exists")
        }

        val now = LocalDateTime.now()
        val user = User(
            id = UserId.generate(),
            username = username,
            email = email,
            displayName = displayName,
            bio = bio,
            createdAt = now,
            updatedAt = now
        )

        return userRepository.save(user)
    }

    fun getUserById(id: UserId): User {
        return userRepository.findById(id)
            ?: throw IllegalArgumentException("User with id '$id' not found")
    }

    fun getUserByUsername(username: String): User {
        return userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User with username '$username' not found")
    }

    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User with email '$email' not found")
    }

    fun getAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun updateUserDisplayName(id: UserId, newDisplayName: String): User {
        val user = getUserById(id)
        val updatedUser = user.updateDisplayName(newDisplayName)
        return userRepository.save(updatedUser)
    }

    fun updateUserBio(id: UserId, newBio: String?): User {
        val user = getUserById(id)
        val updatedUser = user.updateBio(newBio)
        return userRepository.save(updatedUser)
    }

    fun updateUserEmail(id: UserId, newEmail: String): User {
        val user = getUserById(id)
        
        // Check if the new email is already taken by another user
        val existingUser = userRepository.findByEmail(newEmail)
        if (existingUser != null && existingUser.id != id) {
            throw IllegalArgumentException("Email '$newEmail' is already taken")
        }
        
        val updatedUser = user.updateEmail(newEmail)
        return userRepository.save(updatedUser)
    }

    fun deleteUser(id: UserId): Boolean {
        if (userRepository.findById(id) == null) {
            throw IllegalArgumentException("User with id '$id' not found")
        }
        return userRepository.deleteById(id)
    }

    fun isUsernameAvailable(username: String): Boolean {
        return !userRepository.existsByUsername(username)
    }

    fun isEmailAvailable(email: String): Boolean {
        return !userRepository.existsByEmail(email)
    }
}
