package com.example.posterr.domain.useCase

import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import javax.inject.Inject

class PostValidationUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val message: String) : ValidationResult()
    }

    suspend fun validateOriginalPost(content: String): ValidationResult {
        val loggedInUser = userRepository.getLoggedInUser()
        if (loggedInUser == null) {
            return ValidationResult.Invalid("User is not logged in")
        }

        if (content.isBlank()) {
            return ValidationResult.Invalid("Content cannot be empty")
        }

        if (content.length > 777) {
            return ValidationResult.Invalid("Content cannot exceed 777 characters")
        }

        if (!postRepository.canCreatePostToday(loggedInUser.id)) {
            return ValidationResult.Invalid("Daily limit of 5 posts reached")
        }

        return ValidationResult.Valid
    }

    suspend fun validateRepost(): ValidationResult {
        val loggedInUser = userRepository.getLoggedInUser()
        if (loggedInUser == null) {
            return ValidationResult.Invalid("User is not logged in")
        }

        if (!postRepository.canCreatePostToday(loggedInUser.id)) {
            return ValidationResult.Invalid("Daily limit of 5 posts reached")
        }

        return ValidationResult.Valid
    }

    suspend fun validateQuotePost(content: String,): ValidationResult {
        val loggedInUser = userRepository.getLoggedInUser()
        if (loggedInUser == null) {
            return ValidationResult.Invalid("User is not logged in")
        }

        if (content.isBlank()) {
            return ValidationResult.Invalid("Content cannot be empty")
        }

        if (content.length > 777) {
            return ValidationResult.Invalid("Content cannot exceed 777 characters")
        }

        if (!postRepository.canCreatePostToday(loggedInUser.id)) {
            return ValidationResult.Invalid("Daily limit of 5 posts reached")
        }

        return ValidationResult.Valid
    }

    suspend fun getRemainingPostsToday(): Int {
        val loggedInUser = userRepository.getLoggedInUser() ?: return 0
        val postsCount = postRepository.getPostsCountToday(loggedInUser.id)
        return (5 - postsCount).coerceAtLeast(0)
    }

    suspend fun getPostsCountToday(): Int {
        val loggedInUser = userRepository.getLoggedInUser() ?: return 0
        return postRepository.getPostsCountToday(loggedInUser.id)
    }

    suspend fun canCreatePostToday(): Boolean {
        val loggedInUser = userRepository.getLoggedInUser() ?: return false
        return postRepository.canCreatePostToday(loggedInUser.id)
    }
}