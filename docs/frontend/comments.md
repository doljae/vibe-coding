# Comment Functionality - Frontend Documentation

This document describes the frontend implementation of the comment functionality in the bulletin board service.

## Overview

The comment functionality allows users to:
- Create comments on posts
- Create replies to comments
- Edit their own comments
- Delete their own comments
- View all comments for a post

## Implementation

The comment functionality is implemented in the following files:

- `src/main/resources/static/js/comments.js`: The main JavaScript file for comment functionality
- `src/main/resources/static/js/app.js`: Contains the API calls for comment operations
- `src/main/resources/static/post-detail.html`: Contains the HTML structure for the comments section

### CommentsManager Class

The `CommentsManager` class in `comments.js` is responsible for managing all comment-related functionality. It provides methods for:

- Loading comments for a post
- Rendering comments and replies
- Creating new comments
- Creating replies to comments
- Editing comments
- Deleting comments
- Showing/hiding the reply form
- Displaying notifications

### API Calls

The comment API calls are defined in the `api.comments` object in `app.js`:

```javascript
comments: {
    getForPost(postId) {
        return api.call(`/comments/posts/${postId}`);
    },

    create(data) {
        return api.call('/comments', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    createReply(data) {
        return api.call('/comments/replies', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    update(id, data) {
        return api.call(`/comments/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(id, authorId) {
        return api.call(`/comments/${id}?authorId=${authorId}`, {
            method: 'DELETE'
        });
    }
}
```

### HTML Structure

The comments section in `post-detail.html` consists of:

1. A title section with the comment count
2. A form for creating new comments
3. A container for displaying all comments and replies
4. A modal for creating replies to comments

```html
<div id="comments-section" class="comments-section">
    <h3 class="comments-title">
        <i class="fas fa-comments"></i>
        댓글 <span id="comments-count">0</span>개
    </h3>

    <!-- Comment Form -->
    <div class="comment-form-container">
        <form id="comment-form" class="comment-form">
            <!-- Form fields -->
        </form>
    </div>

    <!-- Comments List -->
    <div id="comments-list" class="comments-list">
        <!-- Comments will be rendered here -->
    </div>
</div>

<!-- Reply Modal -->
<div id="reply-modal" class="modal">
    <div class="modal-content">
        <!-- Reply form -->
    </div>
</div>
```

## Comment Creation

When a user submits the comment form:

1. The `handleCommentSubmit` method is called
2. It validates the form data
3. It sends a POST request to `/api/comments`
4. It adds the new comment to the comments array
5. It updates the UI to display the new comment
6. It shows a success notification

## Reply Creation

When a user submits the reply form:

1. The `handleReplySubmit` method is called
2. It validates the form data
3. It sends a POST request to `/api/comments/replies`
4. It adds the new reply to the comments array
5. It updates the UI to display the new reply
6. It shows a success notification

## Comment Editing

When a user clicks the edit button on a comment:

1. The `editComment` method is called
2. It displays a prompt with the current comment content
3. If the user enters new content, it sends a PUT request to `/api/comments/{commentId}`
4. It updates the comment in the comments array
5. It updates the UI to display the edited comment
6. It shows a success notification

## Comment Deletion

When a user clicks the delete button on a comment:

1. The `deleteComment` method is called
2. It displays a confirmation dialog
3. If the user confirms, it sends a DELETE request to `/api/comments/{commentId}`
4. It removes the comment (and its replies if it's a parent comment) from the comments array
5. It updates the UI to remove the deleted comment(s)
6. It shows a success notification

## Rendering Comments

The `renderComments` method:

1. Filters the comments array to separate root comments and replies
2. For each root comment, it finds its replies
3. It generates HTML for each comment and its replies
4. It updates the comments list container with the generated HTML

## CSS Styling

The comments are styled using CSS defined in the `commentsStyles` variable in `comments.js`. The styles include:

- Comment containers
- Comment headers
- Comment content
- Comment actions (reply, edit, delete)
- Reply containers
- Forms for creating comments and replies
- Notifications

## User Identification

For simplicity, the application uses the author's name to identify users. In a real application, this would be replaced with proper authentication and user management.

## Error Handling

All API calls include error handling that:

1. Logs the error to the console
2. Shows an error notification to the user
3. Maintains the UI in a consistent state

