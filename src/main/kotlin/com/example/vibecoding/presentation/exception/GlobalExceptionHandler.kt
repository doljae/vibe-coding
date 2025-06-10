package com.example.vibecoding.presentation.exception

import com.example.vibecoding.application.category.CategoryAlreadyExistsException
import com.example.vibecoding.application.category.CategoryHasPostsException
import com.example.vibecoding.application.category.CategoryNotFoundException
import com.example.vibecoding.application.post.ImageAttachmentException
import com.example.vibecoding.application.post.PostNotFoundException
import com.example.vibecoding.application.post.UserNotFoundException
import com.example.vibecoding.presentation.dto.ErrorResponse
import com.example.vibecoding.presentation.dto.FieldError
import com.example.vibecoding.presentation.dto.ValidationErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

/**
 * Global exception handler for consistent error responses across all controllers
 */
@ControllerAdvice
class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ValidationErrorResponse> {
        val fieldErrors = ex.bindingResult.fieldErrors.map { fieldError ->
            FieldError(
                field = fieldError.field,
                rejectedValue = fieldError.rejectedValue,
                message = fieldError.defaultMessage ?: "Invalid value"
            )
        }

        val response = ValidationErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "Request validation failed",
            path = request.getDescription(false).removePrefix("uri="),
            fieldErrors = fieldErrors
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handle domain validation errors (IllegalArgumentException)
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        // Check if it's a UUID format error
        if (ex.message?.contains("Invalid UUID string") == true) {
            val response = ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = "Invalid ID format",
                path = request.getDescription(false).removePrefix("uri=")
            )
            return ResponseEntity.badRequest().body(response)
        }
        
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Invalid request",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handle Post not found errors
     */
    @ExceptionHandler(PostNotFoundException::class)
    fun handlePostNotFoundException(
        ex: PostNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Post not found",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    /**
     * Handle Category not found errors
     */
    @ExceptionHandler(CategoryNotFoundException::class)
    fun handleCategoryNotFoundException(
        ex: CategoryNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "Category not found",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    /**
     * Handle User not found errors
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(
        ex: UserNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.NOT_FOUND.value(),
            error = "Not Found",
            message = ex.message ?: "User not found",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    /**
     * Handle Category already exists errors
     */
    @ExceptionHandler(CategoryAlreadyExistsException::class)
    fun handleCategoryAlreadyExistsException(
        ex: CategoryAlreadyExistsException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "Category already exists",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    /**
     * Handle Category has posts errors
     */
    @ExceptionHandler(CategoryHasPostsException::class)
    fun handleCategoryHasPostsException(
        ex: CategoryHasPostsException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Conflict",
            message = ex.message ?: "Category cannot be deleted because it has posts",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response)
    }

    /**
     * Handle Image attachment errors
     */
    @ExceptionHandler(ImageAttachmentException::class)
    fun handleImageAttachmentException(
        ex: ImageAttachmentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Bad Request",
            message = ex.message ?: "Image attachment error",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.badRequest().body(response)
    }

    /**
     * Handle generic runtime exceptions
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.internalServerError().body(response)
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "An unexpected error occurred",
            path = request.getDescription(false).removePrefix("uri=")
        )

        return ResponseEntity.internalServerError().body(response)
    }
}

