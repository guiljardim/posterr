package com.example.posterr.data.repository

import com.example.posterr.data.datasource.LocalPostDataSource
import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.repository.PostRepository
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PostRepositoryImpl @Inject constructor(
    private val localDataSource: LocalPostDataSource
) : PostRepository {

    override suspend fun createOriginalPost(content: String, authorId: String): PostResult {
        return when (val result = localDataSource.createOriginalPost(content, authorId)) {
            is com.example.posterr.domain.model.Result.Success -> PostResult.success(result.data)
            is com.example.posterr.domain.model.Result.Error -> PostResult.error(result.message)
        }
    }

    override suspend fun createRepost(originalPostId: String, authorId: String): PostResult {
        return when (val result = localDataSource.createRepost(originalPostId, authorId)) {
            is Result.Success -> PostResult.success(result.data)
            is Result.Error -> PostResult.error(result.message)
        }
    }

    override suspend fun createQuotePost(
        content: String,
        originalPostId: String,
        authorId: String
    ): PostResult {
        return when (val result =
            localDataSource.createQuotePost(content, originalPostId, authorId)) {
            is Result.Success -> PostResult.success(result.data)
            is Result.Error -> PostResult.error(result.message)
        }
    }
}