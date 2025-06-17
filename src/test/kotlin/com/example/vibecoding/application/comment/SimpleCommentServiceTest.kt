package com.example.vibecoding.application.comment

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import com.example.vibecoding.domain.post.Post
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.post.PostRepository
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class SimpleCommentServiceTest {

    // Simple test repository implementation
    private class TestCommentRepository : CommentRepository {
        private val comments = mutableMapOf<CommentId, Comment>()
        
        override fun save(comment: Comment): Comment {
            comments[comment.id] = comment
            return comment
        }
        
        override fun findById(id: CommentId): Comment? = comments[id]
        
        override fun findByPostId(postId: PostId): List<Comment> = 
            comments.values.filter { it.postId == postId }
        
        override fun findRootCommentsByPostId(postId: PostId): List<Comment> =
            comments.values.filter { it.postId == postId && it.parentCommentId == null }
        
        override fun findRepliesByParentCommentId(parentCommentId: CommentId): List<Comment> =
            comments.values.filter { it.parentCommentId == parentCommentId }
        
        override fun deleteById(id: CommentId): Boolean {
            return if (comments.containsKey(id)) {
                comments.remove(id)
                true
            } else {
                false
            }
        }
        
        override fun existsById(id: CommentId): Boolean = comments.containsKey(id)
        
        override fun countByPostId(postId: PostId): Long = 
            comments.values.count { it.postId == postId }.toLong()
        
        override fun findAll(): List<Comment> = comments.values.toList()
    }
    
    // Simple test repository implementation
    private class TestPostRepository : PostRepository {
        private val posts = mutableMapOf<PostId, Post>()
        
        override fun save(post: Post): Post {
            posts[post.id] = post
            return post
        }
        
        override fun findById(id: PostId): Post? = posts[id]
        
        override fun findAll(): List<Post> = posts.values.toList()
        
        override fun findByCategoryId(categoryId: CategoryId): List<Post> =
            posts.values.filter { it.categoryId == categoryId }
        
        override fun findByAuthorId(authorId: UserId): List<Post> =
            posts.values.filter { it.authorId == authorId }
        
        override fun findByTitle(title: String): List<Post> =
            posts.values.filter { it.title.contains(title, ignoreCase = true) }
        
        override fun delete(id: PostId): Boolean {
            return if (posts.containsKey(id)) {
                posts.remove(id)
                true
            } else {
                false
            }
        }
        
        override fun existsById(id: PostId): Boolean = posts.containsKey(id)
        
        override fun countByCategoryId(categoryId: CategoryId): Long =
            posts.values.count { it.categoryId == categoryId }.toLong()
        
        override fun countByAuthorId(authorId: UserId): Long =
            posts.values.count { it.authorId == authorId }.toLong()
    }
    
    // Simple test repository implementation
    private class TestUserRepository : UserRepository {
        private val users = mutableMapOf<UserId, User>()
        
        override fun save(user: User): User {
            users[user.id] = user
            return user
        }
        
        override fun findById(id: UserId): User? = users[id]
        
        override fun findByUsername(username: String): User? =
            users.values.find { it.username == username }
        
        override fun findByEmail(email: String): User? =
            users.values.find { it.email == email }
        
        override fun findAll(): List<User> = users.values.toList()
        
        override fun deleteById(id: UserId): Boolean {
            return if (users.containsKey(id)) {
                users.remove(id)
                true
            } else {
                false
            }
        }
        
        override fun existsByUsername(username: String): Boolean =
            users.values.any { it.username == username }
        
        override fun existsByEmail(email: String): Boolean =
            users.values.any { it.email == email }
    }

    @Test
    fun `should create and retrieve comment`() {
        // Given
        val commentRepository = TestCommentRepository()
        val postRepository = TestPostRepository()
        val userRepository = TestUserRepository()
        
        // Create a test post and user
        val userId = UserId.generate()
        val postId = PostId.generate()
        val categoryId = CategoryId.generate()
        
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val post = Post(
            id = postId,
            title = "Test Post",
            content = "Test content",
            authorId = userId,
            categoryId = categoryId,
            imageAttachments = emptyList(),
            likeCount = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        userRepository.save(user)
        postRepository.save(post)
        
        val commentService = CommentService(commentRepository, postRepository, userRepository)
        val content = "Test comment"
        
        // When
        val createdComment = commentService.createComment(content, userId, postId)
        
        // Then
        assertNotNull(createdComment)
        assertEquals(content, createdComment.content)
        assertEquals(userId, createdComment.authorId)
        assertEquals(postId, createdComment.postId)
        
        // When retrieving
        val retrievedComment = commentService.getComment(createdComment.id)
        
        // Then
        assertEquals(createdComment.id, retrievedComment.id)
        assertEquals(content, retrievedComment.content)
    }
    
    @Test
    fun `should create and retrieve comment with replies`() {
        // Given
        val commentRepository = TestCommentRepository()
        val postRepository = TestPostRepository()
        val userRepository = TestUserRepository()
        
        // Create a test post and user
        val userId = UserId.generate()
        val postId = PostId.generate()
        val categoryId = CategoryId.generate()
        
        val user = User(
            id = userId,
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val post = Post(
            id = postId,
            title = "Test Post",
            content = "Test content",
            authorId = userId,
            categoryId = categoryId,
            imageAttachments = emptyList(),
            likeCount = 0,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        userRepository.save(user)
        postRepository.save(post)
        
        val commentService = CommentService(commentRepository, postRepository, userRepository)
        val rootContent = "Root comment"
        val replyContent = "Reply comment"
        
        // When creating root comment
        val rootComment = commentService.createComment(rootContent, userId, postId)
        
        // Then create reply
        val reply = commentService.createReply(replyContent, userId, postId, rootComment.id)
        
        // When retrieving comments for post
        val commentsWithReplies = commentService.getCommentsForPost(postId)
        
        // Then
        assertEquals(1, commentsWithReplies.size)
        assertEquals(rootComment.id, commentsWithReplies[0].comment.id)
        assertEquals(1, commentsWithReplies[0].replies.size)
        assertEquals(reply.id, commentsWithReplies[0].replies[0].id)
        assertEquals(replyContent, commentsWithReplies[0].replies[0].content)
    }
}

