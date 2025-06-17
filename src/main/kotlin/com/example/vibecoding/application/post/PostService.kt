package com.example.vibecoding.application.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.category.CategoryRepository
import com.example.vibecoding.domain.post.*
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.domain.user.UserRepository
import com.example.vibecoding.domain.comment.CommentRepository
import org.springframework.stereotype.Service
import java.io.InputStream
import java.time.LocalDateTime

/**
 * Application service for Post domain operations
 */
@Service
class PostService(
    private val postRepository: PostRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository,
    private val imageStorageService: ImageStorageService,
    private val likeRepository: LikeRepository,
    private val commentRepository: CommentRepository
) {

    fun createPost(title: String, content: String, authorId: UserId, categoryId: CategoryId): Post {
        if (userRepository.findById(authorId) == null) {
            throw UserNotFoundException("User with id '$authorId' not found")
        }
        
        if (!categoryRepository.existsById(categoryId)) {
            throw CategoryNotFoundException("Category with id '$categoryId' not found")
        }

        val now = LocalDateTime.now()
        val post = Post(
            id = PostId.generate(),
            title = title,
            content = content,
            authorId = authorId,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )

        return postRepository.save(post)
    }

    fun createPostWithImages(
        title: String, 
        content: String, 
        authorId: UserId, 
        categoryId: CategoryId,
        images: List<ImageUploadRequest>
    ): Post {
        if (userRepository.findById(authorId) == null) {
            throw UserNotFoundException("User with id '$authorId' not found")
        }
        
        if (!categoryRepository.existsById(categoryId)) {
            throw CategoryNotFoundException("Category with id '$categoryId' not found")
        }

        require(images.size <= Post.MAX_IMAGES_PER_POST) { 
            "Cannot create post with more than ${Post.MAX_IMAGES_PER_POST} images" 
        }

        val now = LocalDateTime.now()
        var post = Post(
            id = PostId.generate(),
            title = title,
            content = content,
            authorId = authorId,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )

        // Add images to the post
        images.forEach { imageRequest ->
            val imageAttachment = storeImageAttachment(imageRequest)
            post = post.addImageAttachment(imageAttachment)
        }

        return postRepository.save(post)
    }

    fun attachImageToPost(postId: PostId, imageRequest: ImageUploadRequest): Post {
        val existingPost = postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")

        if (!existingPost.canAddMoreImages()) {
            throw ImageAttachmentException(
                "Cannot attach image: Post already has maximum of ${Post.MAX_IMAGES_PER_POST} images"
            )
        }

        val imageAttachment = storeImageAttachment(imageRequest)
        val updatedPost = existingPost.addImageAttachment(imageAttachment)

        return postRepository.save(updatedPost)
    }

    fun removeImageFromPost(postId: PostId, imageId: ImageId): Post {
        val existingPost = postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")

        val imageAttachment = existingPost.getImageAttachment(imageId)
            ?: throw ImageAttachmentException("Image with id '$imageId' not found in post")

        // Remove from storage
        try {
            imageStorageService.deleteImage(imageAttachment.storagePath)
        } catch (e: ImageStorageException) {
            // Log the error but continue with removing from post
            // This handles cases where the file might already be deleted
        }

        val updatedPost = existingPost.removeImageAttachment(imageId)
        return postRepository.save(updatedPost)
    }

    fun getPostImage(postId: PostId, imageId: ImageId): ImageAttachment {
        val post = postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")

        return post.getImageAttachment(imageId)
            ?: throw ImageAttachmentException("Image with id '$imageId' not found in post")
    }

    fun getPostImageData(postId: PostId, imageId: ImageId): InputStream {
        val imageAttachment = getPostImage(postId, imageId)
        return imageStorageService.retrieveImage(imageAttachment.storagePath)
    }

    private fun storeImageAttachment(imageRequest: ImageUploadRequest): ImageAttachment {
        try {
            val storagePath = imageStorageService.storeImage(
                imageRequest.filename,
                imageRequest.contentType,
                imageRequest.inputStream
            )

            return ImageAttachment.create(
                filename = imageRequest.filename,
                storagePath = storagePath,
                contentType = imageRequest.contentType,
                fileSizeBytes = imageRequest.fileSizeBytes
            )
        } catch (e: ImageStorageException) {
            throw ImageAttachmentException("Failed to store image: ${e.message}", e)
        }
    }

    fun updatePost(id: PostId, title: String?, content: String?, categoryId: CategoryId?): Post {
        val existingPost = postRepository.findById(id)
            ?: throw PostNotFoundException("Post with id '$id' not found")

        var updatedPost = existingPost

        title?.let { newTitle ->
            updatedPost = updatedPost.updateTitle(newTitle)
        }

        content?.let { newContent ->
            updatedPost = updatedPost.updateContent(newContent)
        }

        categoryId?.let { newCategoryId ->
            if (!categoryRepository.existsById(newCategoryId)) {
                throw CategoryNotFoundException("Category with id '$newCategoryId' not found")
            }
            updatedPost = updatedPost.updateCategory(newCategoryId)
        }

        return postRepository.save(updatedPost)
    }

    fun getPostById(id: PostId): Post {
        return postRepository.findById(id)
            ?: throw PostNotFoundException("Post with id '$id' not found")
    }

    fun getAllPosts(): List<Post> {
        return postRepository.findAll()
    }

    fun getPostsByCategory(categoryId: CategoryId): List<Post> {
        if (!categoryRepository.existsById(categoryId)) {
            throw CategoryNotFoundException("Category with id '$categoryId' not found")
        }
        return postRepository.findByCategoryId(categoryId)
    }

    fun getPostsByAuthor(authorId: UserId): List<Post> {
        if (userRepository.findById(authorId) == null) {
            throw UserNotFoundException("User with id '$authorId' not found")
        }
        return postRepository.findByAuthorId(authorId)
    }

    fun searchPostsByTitle(title: String): List<Post> {
        return postRepository.findByTitle(title)
    }

    fun deletePost(id: PostId) {
        if (!postRepository.existsById(id)) {
            throw PostNotFoundException("Post with id '$id' not found")
        }

        // Delete all associated likes
        val deletedLikesCount = likeRepository.deleteByPostId(id)
        
        // Delete all associated comments
        val deletedCommentsCount = commentRepository.deleteByPostId(id)
        
        // Delete the post itself
        val result = postRepository.delete(id)
        
        if (!result) {
            throw RuntimeException("Failed to delete post with id '$id'")
        }
    }

    fun getPostCountByCategory(categoryId: CategoryId): Long {
        return postRepository.countByCategoryId(categoryId)
    }

    fun getPostCountByAuthor(authorId: UserId): Long {
        return postRepository.countByAuthorId(authorId)
    }

    /**
     * Synchronize the like count for a post with the actual count from likes
     * This method ensures data consistency between Post.likeCount and actual Like entities
     */
    fun synchronizeLikeCount(postId: PostId): Post {
        val post = postRepository.findById(postId)
            ?: throw PostNotFoundException("Post with id '$postId' not found")
        
        val actualLikeCount = likeRepository.countByPostId(postId)
        
        if (post.likeCount != actualLikeCount) {
            val updatedPost = post.updateLikeCount(actualLikeCount)
            return postRepository.save(updatedPost)
        }
        
        return post
    }

    /**
     * Get post with synchronized like count
     */
    fun getPostByIdWithSyncedLikes(id: PostId): Post {
        return synchronizeLikeCount(id)
    }

    /**
     * Get all posts with synchronized like counts
     */
    fun getAllPostsWithSyncedLikes(): List<Post> {
        return postRepository.findAll().map { post ->
            val actualLikeCount = likeRepository.countByPostId(post.id)
            if (post.likeCount != actualLikeCount) {
                val updatedPost = post.updateLikeCount(actualLikeCount)
                postRepository.save(updatedPost)
            } else {
                post
            }
        }
    }
}

class PostNotFoundException(message: String) : RuntimeException(message)
class CategoryNotFoundException(message: String) : RuntimeException(message)
class UserNotFoundException(message: String) : RuntimeException(message)
class ImageAttachmentException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
