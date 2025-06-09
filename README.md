# Vibe Coding

A Spring Boot application built with Kotlin and Gradle, designed for modern development practices.

## 🚀 Technology Stack

- **Language**: Kotlin
- **Framework**: Spring Boot 3.2.6
- **Build Tool**: Gradle 8.8
- **JDK**: Temurin JDK 21
- **Dependencies**:
  - Spring Boot Starter Web
  - Jackson Module for Kotlin
  - Kotlin Reflection

## 📋 Prerequisites

- JDK 21 (Temurin recommended)
- IntelliJ IDEA (recommended IDE)

## 🛠️ Getting Started

### Clone the repository
```bash
git clone https://github.com/doljae/vibe-coding.git
cd vibe-coding
```

### Build the project
```bash
./gradlew clean build
```

### Run the application
```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## 🧪 Testing

Run all tests:
```bash
./gradlew test
```

## 📁 Project Structure

```
src/
├── main/
│   ├── kotlin/
│   │   └── com/example/vibecoding/
│   │       └── VibeCodingApplication.kt
│   └── resources/
│       └── application.properties
└── test/
    └── kotlin/
        └── com/example/vibecoding/
            └── VibeCodingApplicationTests.kt
```

## 🔧 Development

This project is configured for development with IntelliJ IDEA and follows Kotlin coding standards.

### Available Gradle Tasks

- `./gradlew clean` - Clean build artifacts
- `./gradlew build` - Build the project
- `./gradlew test` - Run tests
- `./gradlew bootRun` - Run the Spring Boot application
- `./gradlew bootJar` - Create executable JAR

## 📝 Contributing

This project follows [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/) for commit messages.

Example commit messages:
- `feat: add user authentication endpoint`
- `fix: resolve database connection issue`
- `docs: update README with setup instructions`

## 📄 License

This project is licensed under the MIT License.

