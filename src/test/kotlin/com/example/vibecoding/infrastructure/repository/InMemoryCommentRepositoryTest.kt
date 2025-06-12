package com.example.vibecoding.infrastructure.repository

import com.example.vibecoding.domain.comment.Comment
import com.example.vibecoding.domain.comment.CommentId
import com.example.vibecoding.domain.post.PostId
import com.example.vibecoding.domain.user.UserId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

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

        assertEquals(comment, savedComment)
        assertEquals(comment, foundComment)
    }

    @Test
    fun `should return null when comment not found`() {
        val nonExistentId = CommentId.generate()
        val foundComment = repository.findById(nonExistentId)

        assertNull(foundComment)
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

        assertEquals(2, commentsForPost.size)
        assertTrue(commentsForPost.contains(comment1))
        assertTrue(commentsForPost.contains(comment2))
        assertFalse(commentsForPost.contains(differentPostComment))
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

        assertEquals(1, rootComments.size)
        assertEquals(rootComment, rootComments[0])
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

        assertEquals(2, replies.size)
        assertTrue(replies.contains(reply1))
        assertTrue(replies.contains(reply2))
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
        assertTrue(repository.existsById(comment.id))

        val deleted = repository.deleteById(comment.id)

        assertTrue(deleted)
        assertFalse(repository.existsById(comment.id))
        assertNull(repository.findById(comment.id))
    }

    @Test
    fun `should return false when deleting non-existent comment`() {
        val nonExistentId = CommentId.generate()
        val deleted = repository.deleteById(nonExistentId)

        assertFalse(deleted)
    }

    @Test
    fun `should check if comment exists`() {
        val comment = Comment.createRootComment(
            id = CommentId.generate(),
            content = "Test comment",
            authorId = authorId,
            postId = postId
        )

        assertFalse(repository.existsById(comment.id))

        repository.save(comment)
        assertTrue(repository.existsById(comment.id))

        repository.deleteById(comment.id)
        assertFalse(repository.existsById(comment.id))
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

        assertEquals(3, count) // 2 root comments + 1 reply
    }

    @Test
    fun `should return zero count for post with no comments`() {
        val emptyPostId = PostId.generate()
        val count = repository.countByPostId(emptyPostId)

        assertEquals(0, count)
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

        assertEquals(2, allComments.size)
        assertEquals(comment1, allComments[0]) // Should be first due to earlier creation time
        assertEquals(comment2, allComments[1])
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
        assertEquals(2, repository.findAll().size)

        repository.clear()
        assertEquals(0, repository.findAll().size)
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
        assertEquals("Updated content", foundComment?.content)
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

        assertEquals(2, comments.size)
        assertEquals(comment1, comments[0])
        assertEquals(comment2, comments[1])
    }
}

