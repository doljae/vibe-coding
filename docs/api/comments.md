# Comment API Documentation

This document describes the API endpoints for the comment functionality in the bulletin board service.

## Endpoints

### Get Comments for a Post

Retrieves all comments for a specific post, organized hierarchically with root comments and their replies.

**URL**: `/api/comments/posts/{postId}`

**Method**: `GET`

**URL Parameters**:
- `postId`: The ID of the post to get comments for

**Response**:
```json
{
  "postId": "string",
  "comments": [
    {
      "comment": {
        "id": "string",
        "content": "string",
        "authorId": "string",
        "authorName": "string",
        "postId": "string",
        "parentCommentId": null,
        "isReply": false,
        "createdAt": "string (ISO date)",
        "updatedAt": "string (ISO date)"
      },
      "replies": [
        {
          "id": "string",
          "content": "string",
          "authorId": "string",
          "authorName": "string",
          "postId": "string",
          "parentCommentId": "string",
          "isReply": true,
          "createdAt": "string (ISO date)",
          "updatedAt": "string (ISO date)"
        }
      ],
      "replyCount": 0
    }
  ],
  "totalCommentCount": 0
}
```

### Create a Comment

Creates a new comment on a post.

**URL**: `/api/comments`

**Method**: `POST`

**Request Body**:
```json
{
  "content": "string",
  "authorName": "string",
  "postId": "string"
}
```

**Response**:
```json
{
  "id": "string",
  "content": "string",
  "authorId": "string",
  "authorName": "string",
  "postId": "string",
  "parentCommentId": null,
  "isReply": false,
  "createdAt": "string (ISO date)",
  "updatedAt": "string (ISO date)"
}
```

### Create a Reply

Creates a reply to an existing comment.

**URL**: `/api/comments/replies`

**Method**: `POST`

**Request Body**:
```json
{
  "content": "string",
  "authorName": "string",
  "postId": "string",
  "parentCommentId": "string"
}
```

**Response**:
```json
{
  "id": "string",
  "content": "string",
  "authorId": "string",
  "authorName": "string",
  "postId": "string",
  "parentCommentId": "string",
  "isReply": true,
  "createdAt": "string (ISO date)",
  "updatedAt": "string (ISO date)"
}
```

### Get a Comment

Retrieves a specific comment by its ID.

**URL**: `/api/comments/{commentId}`

**Method**: `GET`

**URL Parameters**:
- `commentId`: The ID of the comment to retrieve

**Response**:
```json
{
  "id": "string",
  "content": "string",
  "authorId": "string",
  "authorName": "string",
  "postId": "string",
  "parentCommentId": "string",
  "isReply": true,
  "createdAt": "string (ISO date)",
  "updatedAt": "string (ISO date)"
}
```

### Update a Comment

Updates the content of an existing comment.

**URL**: `/api/comments/{commentId}`

**Method**: `PUT`

**URL Parameters**:
- `commentId`: The ID of the comment to update

**Request Body**:
```json
{
  "content": "string",
  "authorId": "string"
}
```

**Response**:
```json
{
  "id": "string",
  "content": "string",
  "authorId": "string",
  "authorName": "string",
  "postId": "string",
  "parentCommentId": "string",
  "isReply": true,
  "createdAt": "string (ISO date)",
  "updatedAt": "string (ISO date)"
}
```

### Delete a Comment

Deletes a comment and all its replies.

**URL**: `/api/comments/{commentId}`

**Method**: `DELETE`

**URL Parameters**:
- `commentId`: The ID of the comment to delete

**Query Parameters**:
- `authorId`: The ID of the author of the comment

**Response**:
- `204 No Content` if successful

### Get Comment Count for a Post

Retrieves the number of comments for a specific post.

**URL**: `/api/comments/posts/{postId}/count`

**Method**: `GET`

**URL Parameters**:
- `postId`: The ID of the post to get comment count for

**Response**:
```json
{
  "count": 0
}
```

### Check if a Comment Exists

Checks if a comment with the specified ID exists.

**URL**: `/api/comments/{commentId}/exists`

**Method**: `GET`

**URL Parameters**:
- `commentId`: The ID of the comment to check

**Response**:
```json
{
  "exists": true
}
```

## Error Responses

### 400 Bad Request

Returned when the request is invalid, such as when required fields are missing or validation fails.

```json
{
  "status": 400,
  "message": "string",
  "timestamp": "string (ISO date)"
}
```

### 404 Not Found

Returned when the requested resource (post, comment, etc.) does not exist.

```json
{
  "status": 404,
  "message": "string",
  "timestamp": "string (ISO date)"
}
```

### 401 Unauthorized

Returned when the user is not authorized to perform the requested action, such as updating or deleting a comment they did not create.

```json
{
  "status": 401,
  "message": "string",
  "timestamp": "string (ISO date)"
}
```

