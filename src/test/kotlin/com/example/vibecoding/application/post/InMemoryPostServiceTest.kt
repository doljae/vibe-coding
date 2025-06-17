package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.Category
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.User
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.comment.CommentRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class InMemoryPostServiceTest {

    private lateinit var postRepository: InMemoryPostRepository
    private lateinit var categoryRepository: InMemoryCategoryRepository
    private lateinit var userRepository: InMemoryUserRepository
    private lateinit var imageStorageService: InMemoryImageStorageService
    private lateinit var likeRepository: InMemoryLikeRepository
    private lateinit var commentRepository: InMemoryCommentRepository
    private lateinit var postService: PostService
    
    private lateinit var testUser: User
    private lateinit var testCategory: Category

    @BeforeEach
    fun setUp() {
        postRepository = InMemoryPostRepository()
        categoryRepository = InMemoryCategoryRepository()
        userRepository = InMemoryUserRepository()
        imageStorageService = InMemoryImageStorageService()
        likeRepository = InMemoryLikeRepository()
        commentRepository = InMemoryCommentRepository()
        
        postService = PostService(
            postRepository, 
            categoryRepository, 
            userRepository, 
            imageStorageService, 
            likeRepository, 
            commentRepository
        )

        testUser = User(
            id = UserId.generate(),
            username = "testuser",
            email = "test@example.com",
            displayName = "Test User",
            bio = "Test bio",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        testCategory = Category(
            id = CategoryId.generate(),
            name = "Test Category",
            description = "Test Description",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        userRepository.save(testUser)
        categoryRepository.save(testCategory)
    }

    @Test
    fun `should delete post successfully`() {
        // Given
        val post = createTestPost()
        postRepository.save(post)
        
        // Create some likes for the post
        val like1 = Like(LikeId.generate(), post.id, UserId.generate(), LocalDateTime.now())
        val like2 = Like(LikeId.generate(), post.id, UserId.generate(), LocalDateTime.now())
        likeRepository.save(like1)
        likeRepository.save(like2)
        
        // Create some comments for the post
        val comment1 = Comment(
            id = CommentId.generate(),
            content = "Comment 1",
            authorId = testUser.id,
            postId = post.id,
            parentCommentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val comment2 = Comment(
            id = CommentId.generate(),
            content = "Comment 2",
            authorId = testUser.id,
            postId = post.id,
            parentCommentId = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        commentRepository.save(comment1)
        commentRepository.save(comment2)
        
        // When
        postService.deletePost(post.id)
        
        // Then
        assertNull(postRepository.findById(post.id))
        assertEquals(0, likeRepository.findByPostId(post.id).size)
        assertEquals(0, commentRepository.findByPostId(post.id).size)
    }
    
    @Test
    fun `should throw exception when post not found for deletion`() {
        // Given
        val postId = PostId.generate()
        
        // When & Then
        assertThrows(PostNotFoundException::class.java) {
            postService.deletePost(postId)
        }
    }
    
    private fun createTestPost(): Post {
        val now = LocalDateTime.now()
        return Post(
            id = PostId.generate(),
            title = "Test Post",
            content = "Test Content",
            authorId = testUser.id,
            categoryId = testCategory.id,
            createdAt = now,
            updatedAt = now
        )
    }
    
    // In-memory repository implementations
    
    class InMemoryPostRepository : PostRepository {
        private val posts = ConcurrentHashMap<PostId, Post>()
        
        override fun save(post: Post): Post {
            posts[post.id] = post
            return post
        }
        
        override fun findById(id: PostId): Post? = posts[id]
        
        override fun findAll(): List<Post> = posts.values.toList()
        
        override fun findByAuthorId(authorId: UserId): List<Post> = 
            posts.values.filter { it.authorId == authorId }
        
        override fun findByCategoryId(categoryId: CategoryId): List<Post> = 
            posts.values.filter { it.categoryId == categoryId }
        
        override fun findByTitle(title: String): List<Post> = 
            posts.values.filter { it.title.contains(title, ignoreCase = true) }
        
        override fun delete(id: PostId): Boolean = posts.remove(id) != null
        
        override fun existsById(id: PostId): Boolean = posts.containsKey(id)
        
        override fun countByAuthorId(authorId: UserId): Long = 
            findByAuthorId(authorId).size.toLong()
        
        override fun countByCategoryId(categoryId: CategoryId): Long = 
            findByCategoryId(categoryId).size.toLong()
    }
    
    class InMemoryCategoryRepository : CategoryRepository {
        private val categories = ConcurrentHashMap<CategoryId, Category>()
        
        override fun save(category: Category): Category {
            categories[category.id] = category
            return category
        }
        
        override fun findById(id: CategoryId): Category? = categories[id]
        
        override fun findAll(): List<Category> = categories.values.toList()
        
        override fun findByName(name: String): Category? =
            categories.values.find { it.name == name }
        
        override fun delete(id: CategoryId): Boolean = categories.remove(id) != null
        
        override fun existsById(id: CategoryId): Boolean = categories.containsKey(id)
        
        override fun existsByName(name: String): Boolean = findByName(name) != null
    }
    
    class InMemoryUserRepository : UserRepository {
        private val users = ConcurrentHashMap<UserId, User>()
        
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
        
        override fun deleteById(id: UserId): Boolean = users.remove(id) != null
        
        override fun existsByUsername(username: String): Boolean = 
            findByUsername(username) != null
        
        override fun existsByEmail(email: String): Boolean = 
            findByEmail(email) != null
    }
    
    class InMemoryImageStorageService : ImageStorageService {
        private val images = ConcurrentHashMap<String, ByteArray>()
        
        override fun storeImage(filename: String, contentType: String, inputStream: InputStream): String {
            val storagePath = "storage/$filename"
            images[storagePath] = inputStream.readAllBytes()
            return storagePath
        }
        
        override fun retrieveImage(storagePath: String): InputStream {
            val imageData = images[storagePath] ?: throw ImageStorageException("Image not found: $storagePath")
            return ByteArrayInputStream(imageData)
        }
        
        override fun deleteImage(storagePath: String) {
            if (!images.containsKey(storagePath)) {
                throw ImageStorageException("Image not found: $storagePath")
            }
            images.remove(storagePath)
        }
        
        override fun imageExists(storagePath: String): Boolean = images.containsKey(storagePath)
        
        override fun getImageSize(storagePath: String): Long {
            val imageData = images[storagePath] ?: throw ImageStorageException("Image not found: $storagePath")
            return imageData.size.toLong()
        }
    }
    
    class InMemoryLikeRepository : LikeRepository {
        private val likes = ConcurrentHashMap<LikeId, Like>()
        
        override fun save(like: Like): Like {
            likes[like.id] = like
            return like
        }
        
        override fun findById(id: LikeId): Like? = likes[id]
        
        override fun findByPostId(postId: PostId): List<Like> = 
            likes.values.filter { it.postId == postId }
        
        override fun findByUserId(userId: UserId): List<Like> = 
            likes.values.filter { it.userId == userId }
        
        override fun findByPostIdAndUserId(postId: PostId, userId: UserId): Like? = 
            likes.values.find { it.postId == postId && it.userId == userId }
        
        override fun delete(id: LikeId): Boolean = likes.remove(id) != null
        
        override fun deleteByPostIdAndUserId(postId: PostId, userId: UserId): Boolean {
            val like = findByPostIdAndUserId(postId, userId) ?: return false
            return delete(like.id)
        }
        
        override fun deleteByPostId(postId: PostId): Int {
            val likesToDelete = findByPostId(postId)
            likesToDelete.forEach { delete(it.id) }
            return likesToDelete.size
        }
        
        override fun existsByPostIdAndUserId(postId: PostId, userId: UserId): Boolean = 
            findByPostIdAndUserId(postId, userId) != null
        
        override fun countByPostId(postId: PostId): Long = 
            findByPostId(postId).size.toLong()
        
        override fun countByUserId(userId: UserId): Long = 
            findByUserId(userId).size.toLong()
    }
    
    class InMemoryCommentRepository : CommentRepository {
        private val comments = ConcurrentHashMap<CommentId, Comment>()
        
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
        
        override fun deleteById(id: CommentId): Boolean = comments.remove(id) != null
        
        override fun deleteByPostId(postId: PostId): Int {
            val commentsToDelete = findByPostId(postId)
            commentsToDelete.forEach { deleteById(it.id) }
            return commentsToDelete.size
        }
        
        override fun existsById(id: CommentId): Boolean = comments.containsKey(id)
        
        override fun countByPostId(postId: PostId): Long = 
            findByPostId(postId).size.toLong()
        
        override fun findAll(): List<Comment> = comments.values.toList()
    }
}

