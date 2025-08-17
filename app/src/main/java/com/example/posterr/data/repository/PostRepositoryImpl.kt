package com.example.posterr.data.repository

import com.example.posterr.data.datasource.LocalPostDataSource
import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.model.ResponseResult
import com.example.posterr.domain.repository.PostRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PostRepositoryImpl @Inject constructor(
    private val localDataSource: LocalPostDataSource
) : PostRepository {

    override suspend fun getAllPosts(): List<Post> = localDataSource.getAllPosts()

    override suspend fun createOriginalPost(content: String, authorId: String): PostResult {
        return when (val result = localDataSource.createOriginalPost(content, authorId)) {
            is ResponseResult.Success -> PostResult.success(result.data)
            is ResponseResult.Error -> PostResult.error(result.message)
        }
    }

    override suspend fun createRepost(originalPostId: String, authorId: String): PostResult {
        return when (val result = localDataSource.createRepost(originalPostId, authorId)) {
            is ResponseResult.Success -> PostResult.success(result.data)
            is ResponseResult.Error -> PostResult.error(result.message)
        }
    }

    override suspend fun createQuotePost(
        content: String,
        originalPostId: String,
        authorId: String
    ): PostResult {
        return when (val result =
            localDataSource.createQuotePost(content, originalPostId, authorId)) {
            is ResponseResult.Success -> PostResult.success(result.data)
            is ResponseResult.Error -> PostResult.error(result.message)
        }
    }

    override suspend fun canCreatePostToday(userId: String): Boolean = localDataSource.canCreatePostToday(userId)

    override suspend fun getPostsCountToday(userId: String): Int = localDataSource.getPostsCountToday(userId)

    override suspend fun getPostsByUser(userId: String): List<Post> = localDataSource.getPostsByUser(userId)


}