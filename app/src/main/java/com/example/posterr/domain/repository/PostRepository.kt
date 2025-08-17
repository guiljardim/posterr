package com.example.posterr.domain.repository

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostResult

interface PostRepository {
    suspend fun getAllPosts(): List<Post>
    suspend fun createOriginalPost(content: String, authorId: String): PostResult
    suspend fun createRepost(originalPostId: String, authorId: String): PostResult
    suspend fun createQuotePost(
        content: String,
        originalPostId: String,
        authorId: String
    ): PostResult

    suspend fun canCreatePostToday(userId: String): Boolean
    suspend fun getPostsCountToday(userId: String): Int
    suspend fun getPostsByUser(userId: String): List<Post>
}