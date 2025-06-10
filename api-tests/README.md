# Blog Service API Testing Scripts

This directory contains HTTP scripts for testing the Blog Service REST APIs manually. These scripts are designed to work with HTTP client tools like IntelliJ IDEA's HTTP Client, VS Code REST Client extension, or any other tool that supports `.http` files.

## Files Overview

### 📋 `blog-api-tests.http`
Comprehensive test suite covering all API endpoints with detailed examples:
- **Category Management**: CRUD operations, search, validation tests
- **User Management**: User operations, availability checks, validation
- **Post Management**: Post CRUD, image handling, search functionality
- **Error Handling**: Testing various error scenarios
- **Workflow Tests**: Complete end-to-end workflows

### ⚡ `quick-test.http`
Streamlined test script for quick verification of basic functionality:
- Creates sample data (category, user, post)
- Tests core operations
- Verifies relationships between entities
- **Manual variable substitution required** - see usage instructions below

### ⚡ `quick-test-intellij.http`
IntelliJ IDEA optimized version with automatic variable handling:
- Same test coverage as quick-test.http
- **Automatic variable substitution** using IntelliJ's HTTP client features
- Variables are automatically populated between requests
- Perfect for IntelliJ IDEA users

## Prerequisites

1. **Start the Application**
   ```bash
   ./gradlew bootRun
   ```
   The application should be running on `http://localhost:8080`

2. **HTTP Client Tool**
   - **IntelliJ IDEA**: Built-in HTTP Client (recommended)
   - **VS Code**: REST Client extension
   - **Postman**: Import the `.http` files
   - **curl**: Convert requests manually

## Usage Instructions

### Option 1: IntelliJ IDEA (Recommended)
1. **For automatic variables**: Use `quick-test-intellij.http`
   - Open the file in IntelliJ IDEA
   - Execute requests 1-11 in order by clicking the green arrow (▶️)
   - Variables are automatically populated between requests
   
2. **For manual variables**: Use `quick-test.http`
   - Execute request 1 (Create category) and copy the `id` from response
   - Execute request 2 (Create user) and copy the `id` from response  
   - Update the variables at the top of the file:
     ```
     @categoryId = paste-category-id-here
     @userId = paste-user-id-here
     ```
   - Continue with remaining requests

### Option 2: VS Code with REST Client
1. Install the "REST Client" extension
2. Use `quick-test.http` with manual variable substitution
3. Copy IDs from responses and update variables manually
4. Click "Send Request" above each request

### Option 3: Manual Testing
1. Copy request details from the `.http` files
2. Use your preferred HTTP client (Postman, Insomnia, curl)
3. Replace `{{variables}}` with actual values from previous responses

## Quick Start Guide

1. **Start with Quick Test**
   ```
   Open: api-tests/quick-test.http
   Execute requests 1-11 in order
   ```

2. **Comprehensive Testing**
   ```
   Open: api-tests/blog-api-tests.http
   Follow the sections in order:
   - Category API Tests
   - User API Tests  
   - Post API Tests
   - Image Upload Tests
   - Error Handling Tests
   ```

## API Endpoints Covered

### 🏷️ Categories
- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category
- `GET /api/categories/search?name={name}` - Search by name

### 👥 Users
- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user by ID
- `POST /api/users` - Create user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/search?username={username}` - Search by username
- `GET /api/users/{id}/posts` - Get user's posts
- `GET /api/users/check-username?username={username}` - Check availability
- `GET /api/users/check-email?email={email}` - Check availability

### 📝 Posts
- `GET /api/posts` - List all posts
- `GET /api/posts/{id}` - Get post by ID
- `POST /api/posts` - Create post
- `POST /api/posts` (multipart) - Create post with images
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post
- `GET /api/posts/search` - Search posts
- `GET /api/posts/category/{categoryId}` - Get posts by category
- `POST /api/posts/{id}/images` - Add image to post
- `DELETE /api/posts/{id}/images/{imageId}` - Remove image
- `GET /api/posts/{id}/images/{imageId}` - Download image

## Variable Usage

The scripts use variables to chain requests together:

```http
### Create a user
# @name createUser
POST {{baseUrl}}/api/users
Content-Type: application/json
{
  "username": "testuser",
  "email": "test@example.com",
  "displayName": "Test User"
}

### Use the created user ID in another request
POST {{baseUrl}}/api/posts
Content-Type: application/json
{
  "title": "My Post",
  "authorId": "{{createUser.response.body.id}}"
}
```

## Image Upload Testing

For image upload tests, you'll need sample image files:

1. Create a `sample-images` directory in the project root
2. Add sample images (PNG, JPG, etc.)
3. Update the file paths in the scripts accordingly

Example:
```http
POST {{baseUrl}}/api/posts/{{postId}}/images
Content-Type: multipart/form-data; boundary=boundary

--boundary
Content-Disposition: form-data; name="image"; filename="test.png"
Content-Type: image/png

< ./sample-images/test.png
--boundary--
```

## Error Testing

The scripts include tests for various error scenarios:
- Invalid data validation
- Non-existent resource access (404)
- Duplicate resource creation (409)
- Invalid UUID formats (400)

## Tips for Effective Testing

1. **Execute in Order**: Some requests depend on previous ones
2. **Check Responses**: Verify HTTP status codes and response bodies
3. **Use Variables**: Leverage response variables for chaining requests
4. **Monitor Logs**: Watch application logs for debugging
5. **Test Edge Cases**: Try invalid data to test validation
6. **Clean Up**: Use DELETE requests to clean up test data

## Swagger Documentation

For interactive API documentation, visit:
```
http://localhost:8080/swagger-ui.html
```

## Troubleshooting

### Common Issues

1. **Variable Substitution Errors**
   - **Problem**: `Invalid request because of unsubstituted variable`
   - **Solution**: 
     - For IntelliJ IDEA: Use `quick-test-intellij.http` for automatic variables
     - For other clients: Use `quick-test.http` and manually update variables
     - Execute requests in sequence (1 → 2 → update variables → 3 → etc.)
     - Copy exact ID values from JSON responses

2. **Connection Refused**
   - Ensure the application is running on port 8080
   - Check if another service is using the port

3. **404 Not Found**
   - Verify the endpoint URLs are correct
   - Check if the application started successfully

4. **400 Bad Request**
   - Validate JSON syntax in request bodies
   - Ensure required fields are provided
   - Check data types and formats

5. **Variable Not Found**
   - Ensure previous requests executed successfully
   - Check variable names match the `@name` annotations
   - Verify response structure contains expected fields

### Getting Help

- Check application logs for detailed error messages
- Use the Swagger UI for API documentation
- Verify request/response formats match the API specification
- Test with simple requests first, then build complexity

## Contributing

When adding new API endpoints:
1. Add corresponding test requests to `blog-api-tests.http`
2. Update the quick test if it's a core feature
3. Document any new variables or dependencies
4. Include both success and error test cases
