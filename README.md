# Vibe Coding Blog System

A comprehensive blog platform built with Spring Boot and Kotlin, featuring a modern web interface, REST API, and advanced blogging capabilities.

## 🌟 Features

### Core Blog Features
- 📝 **Post Management**: Create, read, update, and delete blog posts
- 🖼️ **Image Attachments**: Upload up to 3 images per post with automatic storage
- 👥 **User Management**: User profiles with customizable display names and bios
- 📂 **Category System**: Organize posts with hierarchical categories
- 💬 **Comment System**: Nested comments with full CRUD operations
- ❤️ **Like System**: Like posts and track engagement
- 🔍 **Search & Filter**: Search posts by title, author, or category

### Technical Features
- 🏗️ **Domain-Driven Design**: Clean architecture with separate domain, application, and infrastructure layers
- 🔄 **RESTful API**: Comprehensive REST endpoints with OpenAPI documentation
- 🌐 **Web Interface**: Modern, responsive Korean-language web UI
- 📁 **File Management**: Image upload and download with proper MIME type handling
- 🧪 **Comprehensive Testing**: Unit, integration, and API tests with MockK and Kotest
- 📊 **Monitoring**: Spring Boot Actuator endpoints for health and metrics

## 🚀 Technology Stack

### Core Technologies
- **Language**: Kotlin 2.1.20
- **Framework**: Spring Boot 3.5.0
- **Build Tool**: Gradle 8.8 with Kotlin DSL
- **JDK**: OpenJDK 21 (Temurin)

### Key Dependencies
- **Web**: Spring Boot Starter Web
- **JSON**: Jackson Module for Kotlin
- **Documentation**: SpringDoc OpenAPI UI (Swagger)
- **Testing**: Spring Boot Test, MockK, Kotest, SpringMockK
- **Utilities**: Kotlin Reflection, ByteBuddy

## 📋 Prerequisites

- **JDK 21** (Temurin recommended)
- **IntelliJ IDEA** (recommended IDE)
- **Git** for version control

## 🛠️ Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/doljae/vibe-coding.git
cd vibe-coding
```

### 2. Build the Project
```bash
./gradlew clean build
```

### 3. Run the Application
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### 4. Access the Application
- **Web Interface**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/actuator/health

## 🌐 Web Interface

The application includes a modern, responsive web interface with the following pages:

- **Home Page** (`/`): Welcome page with feature overview
- **Posts List** (`/posts.html`): Browse all blog posts
- **Post Detail** (`/post-detail.html`): View individual posts with comments and likes
- **Create Post** (`/post-form.html`): Create new blog posts with image uploads

## 🔗 API Endpoints

### Posts API (`/api/posts`)
- `GET /api/posts` - Get all posts
- `POST /api/posts` - Create a new post
- `GET /api/posts/{id}` - Get a specific post
- `PUT /api/posts/{id}` - Update a post
- `DELETE /api/posts/{id}` - Delete a post
- `POST /api/posts/{id}/images` - Add image to post
- `DELETE /api/posts/{id}/images/{imageId}` - Remove image from post
- `GET /api/posts/{id}/images/{imageId}` - Download image

### Users API (`/api/users`)
- `GET /api/users` - Get all users
- `POST /api/users` - Create a new user
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Comments API (`/api/comments`)
- `GET /api/posts/{postId}/comments` - Get comments for a post
- `POST /api/posts/{postId}/comments` - Create a comment
- `PUT /api/comments/{id}` - Update a comment
- `DELETE /api/comments/{id}` - Delete a comment

### Categories API (`/api/categories`)
- `GET /api/categories` - Get all categories
- `POST /api/categories` - Create a category
- `PUT /api/categories/{id}` - Update a category
- `DELETE /api/categories/{id}` - Delete a category

### Likes API (`/api/likes`)
- `POST /api/posts/{postId}/like` - Like a post
- `DELETE /api/posts/{postId}/like` - Unlike a post
- `GET /api/posts/{postId}/likes` - Get likes for a post

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Test Categories
- **Unit Tests**: Domain logic and service layer tests
- **Integration Tests**: Full application context tests
- **API Tests**: HTTP endpoint testing with test files in `api-tests/`

### Test Coverage
The project includes comprehensive test coverage with:
- Domain entity validation tests
- Service layer business logic tests
- Repository implementation tests
- Controller integration tests
- End-to-end feature tests

## 📁 Project Structure

```
src/
├── main/
│   ├── kotlin/com/example/vibecoding/
│   │   ├── application/          # Application services
│   │   │   ├── category/         # Category business logic
│   │   │   ├── comment/          # Comment business logic
│   │   │   ├── post/             # Post business logic
│   │   │   └── user/             # User business logic
│   │   ├── domain/               # Domain models and interfaces
│   │   │   ├── category/         # Category domain
│   │   │   ├── comment/          # Comment domain
│   │   │   ├── post/             # Post domain
│   │   │   └── user/             # User domain
│   │   ├── infrastructure/       # Infrastructure implementations
│   │   │   ├── repository/       # In-memory repository implementations
│   │   │   └── storage/          # File storage implementations
│   │   ├── presentation/         # Web and API layer
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data transfer objects
│   │   │   └── exception/        # Exception handling
│   │   └── VibeCodingApplication.kt
│   └── resources/
│       ├── static/               # Web UI assets (HTML, CSS, JS)
│       └── application.yml       # Application configuration
└── test/
    └── kotlin/com/example/vibecoding/
        ├── application/          # Service tests
        ├── domain/               # Domain tests
        ├── infrastructure/       # Infrastructure tests
        ├── integration/          # Integration tests
        └── presentation/         # Controller tests
```

## 🔧 Development

### Architecture Patterns
- **Domain-Driven Design (DDD)**: Clear separation of concerns
- **Clean Architecture**: Dependency inversion and layer isolation
- **Repository Pattern**: Abstracted data access
- **Service Layer**: Business logic encapsulation

### Code Quality
- **Kotlin Conventions**: Idiomatic Kotlin code
- **Conventional Commits**: Standardized commit messages
- **Test-Driven Development**: Comprehensive test coverage
- **Type Safety**: Leveraging Kotlin's type system

### Available Gradle Tasks
```bash
./gradlew clean          # Clean build artifacts
./gradlew build          # Build the project
./gradlew test           # Run tests
./gradlew bootRun        # Run the application
./gradlew bootJar        # Create executable JAR
./gradlew check          # Run all checks
```

## 📊 Monitoring and Observability

### Actuator Endpoints
- `/actuator/health` - Application health status
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics

### Logging
- Configured with structured logging
- Log files stored in `logs/vibe-coding.log`
- Configurable log levels per package

## 🧩 API Testing

The project includes comprehensive API test files in the `api-tests/` directory:

- `blog-api-tests.http` - Complete blog workflow tests
- `comment-api-tests.http` - Comment system tests
- `like-feature-tests.http` - Like functionality tests
- `quick-test.http` - Quick development tests

These can be run directly in IntelliJ IDEA or any HTTP client.

## 📝 Contributing

1. **Branching**: Create feature branches with descriptive English names
2. **Commits**: Follow [Conventional Commits](https://www.conventionalcommits.org/) format
3. **Testing**: Ensure all tests pass before submitting
4. **Code Style**: Follow Kotlin coding conventions
5. **Documentation**: Update relevant documentation

### Example Commit Messages
- `feat: add image upload functionality to posts`
- `fix: resolve comment deletion permission issue`
- `docs: update API documentation with new endpoints`
- `test: add integration tests for like feature`

## 🐛 Troubleshooting

### Common Issues
1. **Port Already in Use**: Change server.port in application.yml
2. **File Upload Issues**: Check app.image.storage.path configuration
3. **Test Failures**: Ensure ByteBuddy agent is properly configured

### Debug Mode
Run with debug logging:
```bash
./gradlew bootRun --args='--logging.level.com.example.vibecoding=DEBUG'
```

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Vibe Coding** - Building a community where developers share knowledge and connect through code.
