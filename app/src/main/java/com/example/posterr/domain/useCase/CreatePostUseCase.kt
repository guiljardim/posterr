package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val validationUseCase: PostValidationUseCase
) {
    suspend fun createOriginalPost(content: String): PostResult {
        when (val validation = validationUseCase.validateOriginalPost(content)) {
            is PostValidationUseCase.ValidationResult.Valid -> {
                val loggedInUser = userRepository.getLoggedInUser()
                    ?: return PostResult.error("User is not logged in")

                return postRepository.createOriginalPost(content, loggedInUser.id)
            }

            is PostValidationUseCase.ValidationResult.Invalid -> {
                return PostResult.error(validation.message)
            }
        }
    }

    suspend fun createRepost(originalPostId: String): PostResult {
        when (val validation = validationUseCase.validateRepost()) {
            is PostValidationUseCase.ValidationResult.Valid -> {
                val loggedInUser = userRepository.getLoggedInUser()
                    ?: return PostResult.error("User is not logged in")

                return postRepository.createRepost(originalPostId, loggedInUser.id)
            }

            is PostValidationUseCase.ValidationResult.Invalid -> {
                return PostResult.error(validation.message)
            }
        }
    }

    suspend fun createQuotePost(content: String, originalPostId: String): PostResult {
        when (val validation = validationUseCase.validateQuotePost(content)) {
            is PostValidationUseCase.ValidationResult.Valid -> {
                val loggedInUser = userRepository.getLoggedInUser()
                    ?: return PostResult.error("User is not logged in")

                return postRepository.createQuotePost(content, originalPostId, loggedInUser.id)
            }

            is PostValidationUseCase.ValidationResult.Invalid -> {
                return PostResult.error(validation.message)
            }
        }
    }

    suspend fun canCreatePostToday(): Boolean = validationUseCase.canCreatePostToday()

    suspend fun getPostsCountToday(): Int = validationUseCase.getPostsCountToday()

    suspend fun getRemainingPostsToday(): Int = validationUseCase.getRemainingPostsToday()
}