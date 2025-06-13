package com.example.vibecoding.presentation.controller

import com.example.vibecoding.application.category.CategoryService
import com.example.vibecoding.application.post.ImageUploadRequest
import com.example.vibecoding.application.post.PostService
import com.example.vibecoding.application.user.UserService
import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.post.ImageId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import com.example.vibecoding.presentation.dto.*
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller for post management operations
 */
@RestController
@RequestMapping("/api/posts")
class PostController(
    private val postService: PostService,
    private val userService: UserService,
    private val categoryService: CategoryService
) {
    private val logger = LoggerFactory.getLogger(PostController::class.java)

    /**
     * Get all posts
     */
    @GetMapping
    fun getAllPosts(): ResponseEntity<List<PostSummaryResponse>> {
        val posts = postService.getAllPosts()
        val response = posts.map { post ->
            val author = userService.getUserById(post.authorId)
            val category = categoryService.getCategoryById(post.categoryId)
            PostSummaryResponse(
                id = post.id.value.toString(),
                title = post.title,
                author = UserSummaryResponse.from(author),
                category = CategorySummaryResponse.from(category),
                imageCount = post.getImageAttachmentCount(),
                likeCount = post.likeCount,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
        }
        return ResponseEntity.ok(response)
    }

    /**
     * Get a specific post by ID
     */
    @GetMapping("/{id}")
    fun getPostById(@PathVariable id: String): ResponseEntity<PostResponse> {
        val post = postService.getPostById(PostId.from(id))
        val author = userService.getUserById(post.authorId)
        val category = categoryService.getCategoryById(post.categoryId)
        
        val response = PostResponse(
            id = post.id.value.toString(),
            title = post.title,
            content = post.content,
            author = UserSummaryResponse.from(author),
            category = CategorySummaryResponse.from(category),
            imageAttachments = post.imageAttachments.map { 
                ImageAttachmentResponse.from(it, post.id.value.toString()) 
            },
            likeCount = post.likeCount,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt
        )
        
        return ResponseEntity.ok(response)
    }

    /**
     * Create a new post
     */
    @PostMapping
    fun createPost(@Valid @RequestBody request: CreatePostRequest): ResponseEntity<PostResponse> {
        return try {
            logger.info("Creating post - title: ${request.title}, authorId: ${request.authorId}")
            
            val post = postService.createPost(
                title = request.title,
                content = request.content,
                authorId = UserId.from(request.authorId),
                categoryId = CategoryId.from(request.categoryId)
            )
            
            val author = userService.getUserById(post.authorId)
            val category = categoryService.getCategoryById(post.categoryId)
            
            val response = PostResponse(
                id = post.id.value.toString(),
                title = post.title,
                content = post.content,
                author = UserSummaryResponse.from(author),
                category = CategorySummaryResponse.from(category),
                imageAttachments = post.imageAttachments.map { 
                    ImageAttachmentResponse.from(it, post.id.value.toString()) 
                },
                likeCount = post.likeCount,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
            
            logger.info("Successfully created post with ID: ${post.id.value}")
            ResponseEntity.status(HttpStatus.CREATED).body(response)
            
        } catch (e: Exception) {
            logger.error("Failed to create post", e)
            throw e
        }
    }

    /**
     * Create a new post with images
     */
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createPostWithImages(
        @RequestParam title: String,
        @RequestParam content: String,
        @RequestParam authorName: String,
        @RequestParam categoryId: String,
        @RequestParam(required = false) images: List<MultipartFile>?
    ): ResponseEntity<PostResponse> {
        return try {
            logger.info("Creating post with images - title: $title, authorName: $authorName, categoryId: $categoryId")
            
            // Find or create user by name
            val author = try {
                userService.getUserByUsername(authorName)
            } catch (e: Exception) {
                logger.info("User not found, creating new user: $authorName")
                userService.createUser(
                    username = authorName,
                    email = "${authorName.lowercase().replace(" ", "")}@example.com",
                    displayName = authorName,
                    bio = null
                )
            }
            
            val categoryIdValue = CategoryId.from(categoryId)
            
            val imageRequests = images?.map { file ->
                logger.info("Processing image: ${file.originalFilename}, size: ${file.size}")
                ImageUploadRequest(
                    filename = file.originalFilename ?: "unknown",
                    contentType = file.contentType ?: "application/octet-stream",
                    fileSizeBytes = file.size,
                    inputStream = file.inputStream
                )
            } ?: emptyList()
            
            val post = if (imageRequests.isNotEmpty()) {
                postService.createPostWithImages(
                    title = title,
                    content = content,
                    authorId = author.id,
                    categoryId = categoryIdValue,
                    images = imageRequests
                )
            } else {
                postService.createPost(
                    title = title,
                    content = content,
                    authorId = author.id,
                    categoryId = categoryIdValue
                )
            }
            
            val category = categoryService.getCategoryById(post.categoryId)
            
            val response = PostResponse(
                id = post.id.value.toString(),
                title = post.title,
                content = post.content,
                author = UserSummaryResponse.from(author),
                category = CategorySummaryResponse.from(category),
                imageAttachments = post.imageAttachments.map { 
                    ImageAttachmentResponse.from(it, post.id.value.toString()) 
                },
                createdAt = post.createdAt,
                likeCount = post.likeCount,
                updatedAt = post.updatedAt
            )
            
            logger.info("Successfully created post with ID: ${post.id.value}")
            ResponseEntity.status(HttpStatus.CREATED).body(response)
            
        } catch (e: Exception) {
            logger.error("Failed to create post with images", e)
            throw e
        }
    }

    /**
     * Update an existing post
     */
    @PutMapping("/{id}")
    fun updatePost(
        @PathVariable id: String,
        @Valid @RequestBody request: UpdatePostRequest
    ): ResponseEntity<PostResponse> {
        val categoryId = request.categoryId?.let { CategoryId.from(it) }
        
        val post = postService.updatePost(
            id = PostId.from(id),
            title = request.title,
            content = request.content,
            categoryId = categoryId
        )
        
        val author = userService.getUserById(post.authorId)
        val category = categoryService.getCategoryById(post.categoryId)
        
        val response = PostResponse(
            id = post.id.value.toString(),
            title = post.title,
            content = post.content,
            author = UserSummaryResponse.from(author),
            category = CategorySummaryResponse.from(category),
            imageAttachments = post.imageAttachments.map { 
                ImageAttachmentResponse.from(it, post.id.value.toString()) 
            },
            likeCount = post.likeCount,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt
        )
        
        return ResponseEntity.ok(response)
    }

    /**
     * Delete a post
     */
    @DeleteMapping("/{id}")
    fun deletePost(@PathVariable id: String): ResponseEntity<Void> {
        val postId = PostId.from(id)
        postService.deletePost(postId)
        return ResponseEntity.noContent().build()
    }

    /**
     * Add image to post
     */
    @PostMapping("/{id}/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addImageToPost(
        @PathVariable id: String,
        @RequestParam("image") image: MultipartFile
    ): ResponseEntity<ImageAttachmentResponse> {
        val postId = PostId.from(id)
        
        val imageRequest = ImageUploadRequest(
            filename = image.originalFilename ?: "unknown",
            contentType = image.contentType ?: "application/octet-stream",
            fileSizeBytes = image.size,
            inputStream = image.inputStream
        )
        
        val updatedPost = postService.attachImageToPost(postId, imageRequest)
        val newImage = updatedPost.imageAttachments.last()
        
        val response = ImageAttachmentResponse.from(newImage, postId.value.toString())
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Remove image from post
     */
    @DeleteMapping("/{id}/images/{imageId}")
    fun removeImageFromPost(
        @PathVariable id: String,
        @PathVariable imageId: String
    ): ResponseEntity<Void> {
        val postId = PostId.from(id)
        val imageIdValue = ImageId.from(imageId)
        
        postService.removeImageFromPost(postId, imageIdValue)
        return ResponseEntity.noContent().build()
    }

    /**
     * Download image from post
     */
    @GetMapping("/{id}/images/{imageId}")
    fun downloadImage(
        @PathVariable id: String,
        @PathVariable imageId: String
    ): ResponseEntity<InputStreamResource> {
        val postId = PostId.from(id)
        val imageIdValue = ImageId.from(imageId)
        
        val imageAttachment = postService.getPostImage(postId, imageIdValue)
        val imageData = postService.getPostImageData(postId, imageIdValue)
        
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType(imageAttachment.contentType)
        headers.contentLength = imageAttachment.fileSizeBytes
        headers.setContentDispositionFormData("attachment", imageAttachment.filename)
        
        return ResponseEntity.ok()
            .headers(headers)
            .body(InputStreamResource(imageData))
    }

    /**
     * Search posts
     */
    @GetMapping("/search")
    fun searchPosts(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) authorId: String?,
        @RequestParam(required = false) categoryId: String?
    ): ResponseEntity<List<PostSummaryResponse>> {
        val posts = when {
            title != null -> postService.searchPostsByTitle(title)
            authorId != null -> postService.getPostsByAuthor(UserId.from(authorId))
            categoryId != null -> postService.getPostsByCategory(CategoryId.from(categoryId))
            else -> postService.getAllPosts()
        }
        
        val response = posts.map { post ->
            val author = userService.getUserById(post.authorId)
            val category = categoryService.getCategoryById(post.categoryId)
            PostSummaryResponse(
                id = post.id.value.toString(),
                title = post.title,
                author = UserSummaryResponse.from(author),
                category = CategorySummaryResponse.from(category),
                imageCount = post.getImageAttachmentCount(),
                likeCount = post.likeCount,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
        }
        return ResponseEntity.ok(response)
    }

    /**
     * Get posts by category
     */
    @GetMapping("/category/{categoryId}")
    fun getPostsByCategory(@PathVariable categoryId: String): ResponseEntity<List<PostSummaryResponse>> {
        val categoryIdValue = CategoryId.from(categoryId)
        val posts = postService.getPostsByCategory(categoryIdValue)
        val category = categoryService.getCategoryById(categoryIdValue)
        
        val response = posts.map { post ->
            val author = userService.getUserById(post.authorId)
            PostSummaryResponse(
                id = post.id.value.toString(),
                title = post.title,
                author = UserSummaryResponse.from(author),
                category = CategorySummaryResponse.from(category),
                imageCount = post.getImageAttachmentCount(),
                likeCount = post.likeCount,
                createdAt = post.createdAt,
                updatedAt = post.updatedAt
            )
        }
        return ResponseEntity.ok(response)
    }
}
