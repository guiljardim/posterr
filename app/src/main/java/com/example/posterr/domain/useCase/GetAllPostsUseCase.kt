package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.repository.PostRepository
import javax.inject.Inject

class GetAllPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(): List<Post> = postRepository.getAllPosts()
}