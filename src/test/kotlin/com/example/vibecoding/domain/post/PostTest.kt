package com.example.vibecoding.domain.post

import com.example.vibecoding.domain.category.CategoryId
import com.example.vibecoding.domain.user.UserId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class PostTest {

    @Test
    fun `should create post with valid data`() {
        // Given
        val id = PostId.generate()
        val title = "My First Post"
        val content = "This is the content of my first post"
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When
        val post = Post(
            id = id,
            title = title,
            content = content,
            authorId = authorId,
            categoryId = categoryId,
            createdAt = now,
            updatedAt = now
        )

        // Then
        post.id shouldBe id
        post.title shouldBe title
        post.content shouldBe content
        post.authorId shouldBe authorId
        post.categoryId shouldBe categoryId
        post.createdAt shouldBe now
        post.updatedAt shouldBe now
    }

    @Test
    fun `should throw exception when title is blank`() {
        // Given
        val id = PostId.generate()
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Post(
                id = id,
                title = "",
                content = "Content",
                authorId = authorId,
                categoryId = categoryId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when title exceeds 200 characters`() {
        // Given
        val id = PostId.generate()
        val longTitle = "a".repeat(201)
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Post(
                id = id,
                title = longTitle,
                content = "Content",
                authorId = authorId,
                categoryId = categoryId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when content is blank`() {
        // Given
        val id = PostId.generate()
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Post(
                id = id,
                title = "Title",
                content = "",
                authorId = authorId,
                categoryId = categoryId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should throw exception when content exceeds 10000 characters`() {
        // Given
        val id = PostId.generate()
        val longContent = "a".repeat(10001)
        val authorId = UserId.generate()
        val categoryId = CategoryId.generate()
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Post(
                id = id,
                title = "Title",
                content = longContent,
                authorId = authorId,
                categoryId = categoryId,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should update title successfully`() {
        // Given
        val post = createTestPost("Original Title", "Content")
        val newTitle = "Updated Title"

        // When
        val updatedPost = post.updateTitle(newTitle)

        // Then
        updatedPost.title shouldBe newTitle
        updatedPost.content shouldBe post.content
        updatedPost.authorId shouldBe post.authorId
        updatedPost.categoryId shouldBe post.categoryId
        updatedPost.id shouldBe post.id
        updatedPost.createdAt shouldBe post.createdAt
        updatedPost.updatedAt shouldNotBe post.updatedAt
    }

    @Test
    fun `should update content successfully`() {
        // Given
        val post = createTestPost("Title", "Original content")
        val newContent = "Updated content"

        // When
        val updatedPost = post.updateContent(newContent)

        // Then
        updatedPost.content shouldBe newContent
        updatedPost.title shouldBe post.title
        updatedPost.authorId shouldBe post.authorId
        updatedPost.categoryId shouldBe post.categoryId
        updatedPost.id shouldBe post.id
        updatedPost.createdAt shouldBe post.createdAt
        updatedPost.updatedAt shouldNotBe post.updatedAt
    }

    @Test
    fun `should update category successfully`() {
        // Given
        val post = createTestPost("Title", "Content")
        val newCategoryId = CategoryId.generate()

        // When
        val updatedPost = post.updateCategory(newCategoryId)

        // Then
        updatedPost.categoryId shouldBe newCategoryId
        updatedPost.title shouldBe post.title
        updatedPost.content shouldBe post.content
        updatedPost.authorId shouldBe post.authorId
        updatedPost.id shouldBe post.id
        updatedPost.createdAt shouldBe post.createdAt
        updatedPost.updatedAt shouldNotBe post.updatedAt
    }

    @Test
    fun `should generate unique post IDs`() {
        // When
        val id1 = PostId.generate()
        val id2 = PostId.generate()

        // Then
        id1 shouldNotBe id2
    }

    @Test
    fun `should create post ID from string`() {
        // Given
        val uuidString = "123e4567-e89b-12d3-a456-426614174000"

        // When
        val postId = PostId.from(uuidString)

        // Then
        postId.value.toString() shouldBe uuidString
    }

    @Test
    fun `should create post with empty image attachments by default`() {
        // Given
        val post = createTestPost("Title", "Content")

        // Then
        post.imageAttachments shouldBe emptyList()
        post.hasImageAttachments() shouldBe false
        post.getImageAttachmentCount() shouldBe 0
        post.canAddMoreImages() shouldBe true
        post.getRemainingImageSlots() shouldBe Post.MAX_IMAGES_PER_POST
    }

    @Test
    fun `should add image attachment successfully`() {
        // Given
        val post = createTestPost("Title", "Content")
        val imageAttachment = createTestImageAttachment("test.jpg")

        // When
        val updatedPost = post.addImageAttachment(imageAttachment)

        // Then
        updatedPost.imageAttachments.size shouldBe 1
        updatedPost.imageAttachments[0] shouldBe imageAttachment
        updatedPost.hasImageAttachments() shouldBe true
        updatedPost.getImageAttachmentCount() shouldBe 1
        updatedPost.canAddMoreImages() shouldBe true
        updatedPost.getRemainingImageSlots() shouldBe 2
        updatedPost.updatedAt shouldNotBe post.updatedAt
    }

    @Test
    fun `should add multiple image attachments up to limit`() {
        // Given
        val post = createTestPost("Title", "Content")
        val image1 = createTestImageAttachment("image1.jpg")
        val image2 = createTestImageAttachment("image2.jpg")
        val image3 = createTestImageAttachment("image3.jpg")

        // When
        val postWith1Image = post.addImageAttachment(image1)
        val postWith2Images = postWith1Image.addImageAttachment(image2)
        val postWith3Images = postWith2Images.addImageAttachment(image3)

        // Then
        postWith3Images.imageAttachments.size shouldBe 3
        postWith3Images.getImageAttachmentCount() shouldBe 3
        postWith3Images.canAddMoreImages() shouldBe false
        postWith3Images.getRemainingImageSlots() shouldBe 0
    }

    @Test
    fun `should throw exception when adding more than maximum images`() {
        // Given
        val post = createTestPost("Title", "Content")
        val image1 = createTestImageAttachment("image1.jpg")
        val image2 = createTestImageAttachment("image2.jpg")
        val image3 = createTestImageAttachment("image3.jpg")
        val image4 = createTestImageAttachment("image4.jpg")

        val postWithMaxImages = post
            .addImageAttachment(image1)
            .addImageAttachment(image2)
            .addImageAttachment(image3)

        // When & Then
        shouldThrow<IllegalArgumentException> {
            postWithMaxImages.addImageAttachment(image4)
        }
    }

    @Test
    fun `should throw exception when adding duplicate image attachment`() {
        // Given
        val post = createTestPost("Title", "Content")
        val imageAttachment = createTestImageAttachment("test.jpg")
        val postWithImage = post.addImageAttachment(imageAttachment)

        // When & Then
        shouldThrow<IllegalArgumentException> {
            postWithImage.addImageAttachment(imageAttachment)
        }
    }

    @Test
    fun `should remove image attachment successfully`() {
        // Given
        val post = createTestPost("Title", "Content")
        val image1 = createTestImageAttachment("image1.jpg")
        val image2 = createTestImageAttachment("image2.jpg")
        val postWithImages = post.addImageAttachment(image1).addImageAttachment(image2)

        // When
        val updatedPost = postWithImages.removeImageAttachment(image1.id)

        // Then
        updatedPost.imageAttachments.size shouldBe 1
        updatedPost.imageAttachments[0] shouldBe image2
        updatedPost.getImageAttachment(image1.id) shouldBe null
        updatedPost.getImageAttachment(image2.id) shouldBe image2
        updatedPost.updatedAt shouldNotBe postWithImages.updatedAt
    }

    @Test
    fun `should throw exception when removing non-existent image attachment`() {
        // Given
        val post = createTestPost("Title", "Content")
        val imageAttachment = createTestImageAttachment("test.jpg")
        val nonExistentImageId = ImageId.generate()

        val postWithImage = post.addImageAttachment(imageAttachment)

        // When & Then
        shouldThrow<IllegalArgumentException> {
            postWithImage.removeImageAttachment(nonExistentImageId)
        }
    }

    @Test
    fun `should get image attachment by ID`() {
        // Given
        val post = createTestPost("Title", "Content")
        val imageAttachment = createTestImageAttachment("test.jpg")
        val postWithImage = post.addImageAttachment(imageAttachment)

        // When
        val retrievedImage = postWithImage.getImageAttachment(imageAttachment.id)

        // Then
        retrievedImage shouldBe imageAttachment
    }

    @Test
    fun `should return null when getting non-existent image attachment`() {
        // Given
        val post = createTestPost("Title", "Content")
        val nonExistentImageId = ImageId.generate()

        // When
        val retrievedImage = post.getImageAttachment(nonExistentImageId)

        // Then
        retrievedImage shouldBe null
    }

    @Test
    fun `should validate maximum images during post creation`() {
        // Given
        val images = listOf(
            createTestImageAttachment("image1.jpg"),
            createTestImageAttachment("image2.jpg"),
            createTestImageAttachment("image3.jpg"),
            createTestImageAttachment("image4.jpg") // One too many
        )
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Post(
                id = PostId.generate(),
                title = "Title",
                content = "Content",
                authorId = UserId.generate(),
                categoryId = CategoryId.generate(),
                imageAttachments = images,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should validate unique image attachments during post creation`() {
        // Given
        val imageAttachment = createTestImageAttachment("test.jpg")
        val duplicateImages = listOf(imageAttachment, imageAttachment)
        val now = LocalDateTime.now()

        // When & Then
        shouldThrow<IllegalArgumentException> {
            Post(
                id = PostId.generate(),
                title = "Title",
                content = "Content",
                authorId = UserId.generate(),
                categoryId = CategoryId.generate(),
                imageAttachments = duplicateImages,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    @Test
    fun `should create post with valid image attachments`() {
        // Given
        val images = listOf(
            createTestImageAttachment("image1.jpg"),
            createTestImageAttachment("image2.jpg"),
            createTestImageAttachment("image3.jpg")
        )
        val now = LocalDateTime.now()

        // When
        val post = Post(
            id = PostId.generate(),
            title = "Title",
            content = "Content",
            authorId = UserId.generate(),
            categoryId = CategoryId.generate(),
            imageAttachments = images,
            createdAt = now,
            updatedAt = now
        )

        // Then
        post.imageAttachments shouldBe images
        post.getImageAttachmentCount() shouldBe 3
        post.canAddMoreImages() shouldBe false
    }

    private fun createTestPost(title: String, content: String): Post {
        val now = LocalDateTime.now()
        return Post(
            id = PostId.generate(),
            title = title,
            content = content,
            authorId = UserId.generate(),
            categoryId = CategoryId.generate(),
            createdAt = now,
            updatedAt = now
        )
    }

    private fun createTestImageAttachment(filename: String): ImageAttachment {
        return ImageAttachment.create(
            filename = filename,
            storagePath = "test/path/$filename",
            contentType = "image/jpeg",
            fileSizeBytes = 1024L
        )
    }
}
