package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import javax.inject.Inject

data class PostWithAuthor(
    val post: Post,
    val authorUsername: String
)

class GetAllPostsUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): List<PostWithAuthor> {
        val posts = postRepository.getAllPosts()
        return posts.map { post ->
            val author = userRepository.getUserById(post.authorId)
            PostWithAuthor(
                post = post,
                authorUsername = author?.username ?: post.authorId
            )
        }
    }
}