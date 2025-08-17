package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.User
import com.example.posterr.domain.model.UserStats
import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import javax.inject.Inject

data class UserProfileData(
    val user: User,
    val stats: UserStats,
    val posts: List<Post>
)

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(userId: String): UserProfileData? {
        val user = userRepository.getUserById(userId) ?: return null
        val stats = userRepository.getUserStats(userId)
        val posts = postRepository.getPostsByUser(userId)

        return UserProfileData(user, stats, posts)
    }

    suspend fun getLoggedInUserProfile(): UserProfileData? {
        val user = userRepository.getLoggedInUser() ?: return null
        return invoke(user.id)
    }
}