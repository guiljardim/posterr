package com.example.posterr.data.repository

import com.example.posterr.data.datasource.LocalPostDataSource
import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.model.ResponseResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PostRepositoryImplTest {

    private lateinit var postRepository: PostRepositoryImpl
    private lateinit var localDataSource: LocalPostDataSource

    @Before
    fun setup() {
        localDataSource = mockk(relaxed = true)
        postRepository = PostRepositoryImpl(localDataSource)
    }

    @Test
    fun `getAllPosts should return posts from local data source`() = runTest {
        // Given
        val posts = listOf(
            Post(
                id = "post1",
                content = "Test post 1",
                authorId = "user1",
                type = PostType.ORIGINAL,
                createdAt = System.currentTimeMillis()
            )
        )
        coEvery { localDataSource.getAllPosts() } returns posts

        // When
        val result = postRepository.getAllPosts()

        // Then
        assertEquals(posts, result)
        coVerify { localDataSource.getAllPosts() }
    }

    @Test
    fun `createOriginalPost should return success when local data source succeeds`() = runTest {
        // Given
        val content = "Test post content"
        val authorId = "user1"
        val post = Post(
            id = "post1",
            content = content,
            authorId = authorId,
            type = PostType.ORIGINAL,
            createdAt = System.currentTimeMillis()
        )
        coEvery { localDataSource.createOriginalPost(content, authorId) } returns ResponseResult.Success(post)

        // When
        val result = postRepository.createOriginalPost(content, authorId)

        // Then
        assertTrue(result is PostResult.Success)
        val successResult = result as PostResult.Success
        assertEquals(post, successResult.post)
        coVerify { localDataSource.createOriginalPost(content, authorId) }
    }

    @Test
    fun `createOriginalPost should return error when local data source fails`() = runTest {
        // Given
        val content = "Test post content"
        val authorId = "user1"
        val errorMessage = "Daily limit reached"
        coEvery { localDataSource.createOriginalPost(content, authorId) } returns ResponseResult.Error(errorMessage)

        // When
        val result = postRepository.createOriginalPost(content, authorId)

        // Then
        assertTrue(result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals(errorMessage, errorResult.message)
        coVerify { localDataSource.createOriginalPost(content, authorId) }
    }

    @Test
    fun `createRepost should return success when local data source succeeds`() = runTest {
        // Given
        val originalPostId = "post1"
        val authorId = "user1"
        val post = Post(
            id = "repost1",
            content = "",
            authorId = authorId,
            type = PostType.REPOST,
            createdAt = System.currentTimeMillis(),
            originalPost = Post(
                id = originalPostId,
                content = "Original post",
                authorId = "user2",
                type = PostType.ORIGINAL,
                createdAt = System.currentTimeMillis()
            )
        )
        coEvery { localDataSource.createRepost(originalPostId, authorId) } returns ResponseResult.Success(post)

        // When
        val result = postRepository.createRepost(originalPostId, authorId)

        // Then
        assertTrue(result is PostResult.Success)
        val successResult = result as PostResult.Success
        assertEquals(post, successResult.post)
        coVerify { localDataSource.createRepost(originalPostId, authorId) }
    }

    @Test
    fun `createRepost should return error when local data source fails`() = runTest {
        // Given
        val originalPostId = "post1"
        val authorId = "user1"
        val errorMessage = "Original post not found"
        coEvery { localDataSource.createRepost(originalPostId, authorId) } returns ResponseResult.Error(errorMessage)

        // When
        val result = postRepository.createRepost(originalPostId, authorId)

        // Then
        assertTrue(result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals(errorMessage, errorResult.message)
        coVerify { localDataSource.createRepost(originalPostId, authorId) }
    }

    @Test
    fun `createQuotePost should return success when local data source succeeds`() = runTest {
        // Given
        val content = "Great post!"
        val originalPostId = "post1"
        val authorId = "user1"
        val post = Post(
            id = "quote1",
            content = content,
            authorId = authorId,
            type = PostType.QUOTE,
            createdAt = System.currentTimeMillis(),
            originalPost = Post(
                id = originalPostId,
                content = "Original post",
                authorId = "user2",
                type = PostType.ORIGINAL,
                createdAt = System.currentTimeMillis()
            )
        )
        coEvery { localDataSource.createQuotePost(content, originalPostId, authorId) } returns ResponseResult.Success(post)

        // When
        val result = postRepository.createQuotePost(content, originalPostId, authorId)

        // Then
        assertTrue(result is PostResult.Success)
        val successResult = result as PostResult.Success
        assertEquals(post, successResult.post)
        coVerify { localDataSource.createQuotePost(content, originalPostId, authorId) }
    }

    @Test
    fun `createQuotePost should return error when local data source fails`() = runTest {
        // Given
        val content = "Great post!"
        val originalPostId = "post1"
        val authorId = "user1"
        val errorMessage = "Daily limit reached"
        coEvery { localDataSource.createQuotePost(content, originalPostId, authorId) } returns ResponseResult.Error(errorMessage)

        // When
        val result = postRepository.createQuotePost(content, originalPostId, authorId)

        // Then
        assertTrue(result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals(errorMessage, errorResult.message)
        coVerify { localDataSource.createQuotePost(content, originalPostId, authorId) }
    }

    @Test
    fun `canCreatePostToday should return result from local data source`() = runTest {
        // Given
        val userId = "user1"
        coEvery { localDataSource.canCreatePostToday(userId) } returns true

        // When
        val result = postRepository.canCreatePostToday(userId)

        // Then
        assertTrue(result)
        coVerify { localDataSource.canCreatePostToday(userId) }
    }

    @Test
    fun `getPostsCountToday should return result from local data source`() = runTest {
        // Given
        val userId = "user1"
        coEvery { localDataSource.getPostsCountToday(userId) } returns 3

        // When
        val result = postRepository.getPostsCountToday(userId)

        // Then
        assertEquals(3, result)
        coVerify { localDataSource.getPostsCountToday(userId) }
    }

    @Test
    fun `getPostsByUser should return result from local data source`() = runTest {
        // Given
        val userId = "user1"
        val posts = listOf(
            Post(
                id = "post1",
                content = "User post",
                authorId = userId,
                type = PostType.ORIGINAL,
                createdAt = System.currentTimeMillis()
            )
        )
        coEvery { localDataSource.getPostsByUser(userId) } returns posts

        // When
        val result = postRepository.getPostsByUser(userId)

        // Then
        assertEquals(posts, result)
        coVerify { localDataSource.getPostsByUser(userId) }
    }
}
