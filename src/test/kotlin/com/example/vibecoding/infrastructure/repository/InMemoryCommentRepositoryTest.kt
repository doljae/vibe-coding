package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.booleans.shouldBeFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryCommentRepositoryTest {

    private lateinit var repository: InMemoryCommentRepository
    private val postId = PostId.generate()
    private val authorId = UserId.generate()

    @BeforeEach
    fun setUp() {
        repository = InMemoryCommentRepository()
    }

    @Test
    fun `should save and find comment by id`() {
        val comment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Test comment",
            authorId = authorId,
            postId = postId
        )

        val savedComment = repository.save(comment)
        val foundComment = repository.findById(comment.id)

        savedComment shouldBe comment
        foundComment shouldBe comment
    }

    @Test
    fun `should return null when comment not found`() {
        val nonExistentId = CommentId.generate()
        val foundComment = repository.findById(nonExistentId)

        foundComment.shouldBeNull()
    }

    @Test
    fun `should find comments by post id`() {
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "First comment",
            authorId = authorId,
            postId = postId
        )
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Second comment",
            authorId = authorId,
            postId = postId
        )
        val differentPostComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Different post comment",
            authorId = authorId,
            postId = PostId.generate()
        )

        repository.save(comment1)
        repository.save(comment2)
        repository.save(differentPostComment)

        val commentsForPost = repository.findByPostId(postId)

        commentsForPost.size shouldBe 2
        commentsForPost shouldContain comment1
        commentsForPost shouldContain comment2
        commentsForPost shouldNotContain differentPostComment
    }

    @Test
    fun `should find root comments by post id`() {
        val rootComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Root comment",
            authorId = authorId,
            postId = postId
        )
        val reply = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply comment",
            authorId = authorId,
            postId = postId,
            parentComment = rootComment
        )

        repository.save(rootComment)
        repository.save(reply)

        val rootComments = repository.findRootCommentsByPostId(postId)

        rootComments.size shouldBe 1
        rootComments[0] shouldBe rootComment
    }

    @Test
    fun `should find replies by parent comment id`() {
        val parentComment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Parent comment",
            authorId = authorId,
            postId = postId
        )
        val reply1 = Comment.createReply(
            id = CommentId.generate(),
            content = "First reply",
            authorId = authorId,
            postId = postId,
            parentComment = parentComment
        )
        val reply2 = Comment.createReply(
            id = CommentId.generate(),
            content = "Second reply",
            authorId = authorId,
            postId = postId,
            parentComment = parentComment
        )

        repository.save(parentComment)
        repository.save(reply1)
        repository.save(reply2)

        val replies = repository.findRepliesByParentCommentId(parentComment.id)

        replies.size shouldBe 2
        replies shouldContain reply1
        replies shouldContain reply2
    }

    @Test
    fun `should delete comment by id`() {
        val comment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Test comment",
            authorId = authorId,
            postId = postId
        )

        repository.save(comment)
        repository.existsById(comment.id).shouldBeTrue()

        val deleted = repository.deleteById(comment.id)

        deleted.shouldBeTrue()
        repository.existsById(comment.id).shouldBeFalse()
        repository.findById(comment.id).shouldBeNull()
    }

    @Test
    fun `should return false when deleting non-existent comment`() {
        val nonExistentId = CommentId.generate()
        val deleted = repository.deleteById(nonExistentId)

        deleted.shouldBeFalse()
    }

    @Test
    fun `should check if comment exists`() {
        val comment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Test comment",
            authorId = authorId,
            postId = postId
        )

        repository.existsById(comment.id).shouldBeFalse()

        repository.save(comment)
        repository.existsById(comment.id).shouldBeTrue()

        repository.deleteById(comment.id)
        repository.existsById(comment.id).shouldBeFalse()
    }

    @Test
    fun `should count comments by post id`() {
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "First comment",
            authorId = authorId,
            postId = postId
        )
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Second comment",
            authorId = authorId,
            postId = postId
        )
        val reply = Comment.createReply(
            id = CommentId.generate(),
            content = "Reply",
            authorId = authorId,
            postId = postId,
            parentComment = comment1
        )

        repository.save(comment1)
        repository.save(comment2)
        repository.save(reply)

        val count = repository.countByPostId(postId)

        count shouldBe 3 // 2 root comments + 1 reply
    }

    @Test
    fun `should return zero count for post with no comments`() {
        val emptyPostId = PostId.generate()
        val count = repository.countByPostId(emptyPostId)

        count shouldBe 0
    }

    @Test
    fun `should find all comments sorted by creation time`() {
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "First comment",
            authorId = authorId,
            postId = postId
        )
        Thread.sleep(1) // Ensure different timestamps
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Second comment",
            authorId = authorId,
            postId = postId
        )

        repository.save(comment2) // Save in reverse order
        repository.save(comment1)

        val allComments = repository.findAll()

        allComments.size shouldBe 2
        allComments[0] shouldBe comment1 // Should be first due to earlier creation time
        allComments[1] shouldBe comment2
    }

    @Test
    fun `should clear all comments`() {
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "First comment",
            authorId = authorId,
            postId = postId
        )
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Second comment",
            authorId = authorId,
            postId = postId
        )

        repository.save(comment1)
        repository.save(comment2)
        repository.findAll().size shouldBe 2

        repository.clear()
        repository.findAll().size shouldBe 0
    }

    @Test
    fun `should update existing comment when saved again`() {
        val comment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Original content",
            authorId = authorId,
            postId = postId
        )

        repository.save(comment)
        val updatedComment = comment.updateContent("Updated content")
        repository.save(updatedComment)

        val foundComment = repository.findById(comment.id)
        foundComment?.content shouldBe "Updated content"
    }

    @Test
    fun `should maintain comment order by creation time in findByPostId`() {
        val comment1 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "First comment",
            authorId = authorId,
            postId = postId
        )
        Thread.sleep(1)
        val comment2 = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Second comment",
            authorId = authorId,
            postId = postId
        )

        repository.save(comment2) // Save in reverse order
        repository.save(comment1)

        val comments = repository.findByPostId(postId)

        comments.size shouldBe 2
        comments[0] shouldBe comment1
        comments[1] shouldBe comment2
    }
}
