package com.example.posterr.data.datasource

import com.example.posterr.data.dao.DailyPostCountDao
import com.example.posterr.data.dao.PostDao
import com.example.posterr.data.entity.DailyPostCountEntity
import com.example.posterr.data.entity.PostEntity
import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.model.ResponseResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class LocalPostDataSourceTest {

    private lateinit var localPostDataSource: LocalPostDataSource
    private lateinit var postDao: PostDao
    private lateinit var dailyPostCountDao: DailyPostCountDao

    @Before
    fun setup() {
        postDao = mockk(relaxed = true)
        dailyPostCountDao = mockk(relaxed = true)
        localPostDataSource = LocalPostDataSource(postDao, dailyPostCountDao)
    }

    @Test
    fun `getAllPosts should return converted posts successfully`() = runTest {
        // Given
        val postEntities = listOf(
            PostEntity(
                id = "post1",
                content = "Test post 1",
                authorId = "user1",
                postType = PostType.ORIGINAL,
                createdAt = System.currentTimeMillis()
            ),
            PostEntity(
                id = "post2",
                content = "Test post 2",
                authorId = "user2",
                postType = PostType.ORIGINAL,
                createdAt = System.currentTimeMillis()
            )
        )
        
        coEvery { postDao.getAllPosts() } returns postEntities

        // When
        val result = localPostDataSource.getAllPosts()

        // Then
        assertEquals(2, result.size)
        assertEquals("post1", result[0].id)
        assertEquals("post2", result[1].id)
        coVerify { postDao.getAllPosts() }
    }

    @Test
    fun `getAllPosts should handle posts with originalPost references`() = runTest {
        // Given
        val postEntities = listOf(
            PostEntity(
                id = "repost1",
                content = "Reposted",
                authorId = "user1",
                postType = PostType.REPOST,
                createdAt = System.currentTimeMillis(),
                originalPostId = "post1"
            )
        )
        
        val originalPost = PostEntity(
            id = "post1",
            content = "Original post",
            authorId = "user2",
            postType = PostType.ORIGINAL,
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { postDao.getAllPosts() } returns postEntities
        coEvery { postDao.getPostById("post1") } returns originalPost

        // When
        val result = localPostDataSource.getAllPosts()

        // Then
        assertEquals(1, result.size)
        assertEquals("repost1", result[0].id)
        assertNotNull(result[0].originalPost)
        assertEquals("post1", result[0].originalPost?.id)
    }

    @Test
    fun `createOriginalPost should create post successfully when user can post`() = runTest {
        // Given
        val content = "Test post content"
        val authorId = "user1"
        val today = localPostDataSource.getTodayMillis().toString()
        
        coEvery { dailyPostCountDao.getPostCountForDate(authorId, today) } returns 2
        coEvery { postDao.insertPost(any()) } returns Unit
        coEvery { dailyPostCountDao.insertDailyPostCount(any()) } returns Unit

        // When
        val result = localPostDataSource.createOriginalPost(content, authorId)

        // Then
        assertTrue(result is ResponseResult.Success)
        val post = (result as ResponseResult.Success).data
        assertEquals(content, post.content)
        assertEquals(authorId, post.authorId)
        assertEquals(PostType.ORIGINAL, post.type)
        coVerify { postDao.insertPost(any()) }
        coVerify { dailyPostCountDao.insertDailyPostCount(any()) }
    }

    @Test
    fun `createOriginalPost should fail when daily limit reached`() = runTest {
        // Given
        val content = "Test post content"
        val authorId = "user1"
        val today = localPostDataSource.getTodayMillis().toString()
        
        // Mock the daily post count to return 5 (limit reached)
        coEvery { dailyPostCountDao.getPostCountForDateOrZero(authorId, today) } returns 5

        // When
        val result = localPostDataSource.createOriginalPost(content, authorId)

        // Then
        assertTrue("Result should be error", result is ResponseResult.Error)
        val errorResult = result as ResponseResult.Error
        assertEquals("Daily limit of 5 posts reached", errorResult.message)
        
        // Verify that no post was inserted
        coVerify(exactly = 0) { postDao.insertPost(any()) }
        coVerify(exactly = 0) { dailyPostCountDao.insertDailyPostCount(any()) }
    }

    @Test
    fun `createRepost should create repost successfully when user can post`() = runTest {
        // Given
        val originalPostId = "post1"
        val authorId = "user1"
        val today = localPostDataSource.getTodayMillis().toString()
        
        val originalPost = PostEntity(
            id = originalPostId,
            content = "Original post",
            authorId = "user2",
            postType = PostType.ORIGINAL,
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { postDao.getPostById(originalPostId) } returns originalPost
        coEvery { dailyPostCountDao.getPostCountForDate(authorId, today) } returns 2
        coEvery { postDao.insertPost(any()) } returns Unit
        coEvery { dailyPostCountDao.insertDailyPostCount(any()) } returns Unit

        // When
        val result = localPostDataSource.createRepost(originalPostId, authorId)

        // Then
        assertTrue(result is ResponseResult.Success)
        val repost = (result as ResponseResult.Success).data
        assertEquals(PostType.REPOST, repost.type)
        assertEquals(originalPostId, repost.originalPost?.id)
        coVerify { postDao.insertPost(any()) }
        coVerify { dailyPostCountDao.insertDailyPostCount(any()) }
    }

    @Test
    fun `createRepost should fail when original post not found`() = runTest {
        // Given
        val originalPostId = "nonexistent"
        val authorId = "user1"
        
        coEvery { postDao.getPostById(originalPostId) } returns null

        // When
        val result = localPostDataSource.createRepost(originalPostId, authorId)

        // Then
        assertTrue(result is ResponseResult.Error)
        assertEquals("Original post not found", (result as ResponseResult.Error).message)
        coVerify(exactly = 0) { postDao.insertPost(any()) }
    }

    @Test
    fun `createQuotePost should create quote post successfully when user can post`() = runTest {
        // Given
        val content = "Great post!"
        val originalPostId = "post1"
        val authorId = "user1"
        val today = localPostDataSource.getTodayMillis().toString()
        
        val originalPost = PostEntity(
            id = originalPostId,
            content = "Original post",
            authorId = "user2",
            postType = PostType.ORIGINAL,
            createdAt = System.currentTimeMillis()
        )
        
        coEvery { postDao.getPostById(originalPostId) } returns originalPost
        coEvery { dailyPostCountDao.getPostCountForDate(authorId, today) } returns 2
        coEvery { postDao.insertPost(any()) } returns Unit
        coEvery { dailyPostCountDao.insertDailyPostCount(any()) } returns Unit

        // When
        val result = localPostDataSource.createQuotePost(content, originalPostId, authorId)

        // Then
        assertTrue(result is ResponseResult.Success)
        val quotePost = (result as ResponseResult.Success).data
        assertEquals(content, quotePost.content)
        assertEquals(PostType.QUOTE, quotePost.type)
        assertEquals(originalPostId, quotePost.originalPost?.id)
        coVerify { postDao.insertPost(any()) }
        coVerify { dailyPostCountDao.insertDailyPostCount(any()) }
    }

    @Test
    fun `getPostsCountToday should return correct count`() = runTest {
        // Given
        val userId = "user1"
        val today = localPostDataSource.getTodayMillis().toString()
        coEvery { dailyPostCountDao.getPostCountForDateOrZero(userId, today) } returns 3

        // When
        val result = localPostDataSource.getPostsCountToday(userId)

        // Then
        assertEquals(3, result)
        coVerify { dailyPostCountDao.getPostCountForDateOrZero(userId, today) }
    }

    @Test
    fun `getPostsCountToday should return 0 when dao throws exception`() = runTest {
        // Given
        val userId = "user1"
        val today = localPostDataSource.getTodayMillis().toString()
        coEvery { dailyPostCountDao.getPostCountForDateOrZero(userId, today) } throws RuntimeException("Database error")

        // When
        val result = localPostDataSource.getPostsCountToday(userId)

        // Then
        assertEquals(0, result)
    }

    @Test
    fun `canCreatePostToday should return true when count is less than 5`() = runTest {
        // Given
        val userId = "user1"
        coEvery { localPostDataSource.getPostsCountToday(userId) } returns 3

        // When
        val result = localPostDataSource.canCreatePostToday(userId)

        // Then
        assertTrue(result)
    }

    @Test
    fun `canCreatePostToday should return false when count is 5 or more`() = runTest {
        // Given
        val userId = "user1"
        coEvery { localPostDataSource.getPostsCountToday(userId) } returns 5

        // When
        val result = localPostDataSource.canCreatePostToday(userId)

        // Then
        assertFalse(result)
    }

    @Test
    fun `getPostsByUser should return user posts successfully`() = runTest {
        // Given
        val userId = "user1"
        val userPosts = listOf(
            PostEntity(
                id = "post1",
                content = "User post 1",
                authorId = userId,
                postType = PostType.ORIGINAL,
                createdAt = System.currentTimeMillis()
            )
        )
        
        coEvery { postDao.getPostsByUser(userId) } returns userPosts

        // When
        val result = localPostDataSource.getPostsByUser(userId)

        // Then
        assertEquals(1, result.size)
        assertEquals(userId, result[0].authorId)
        coVerify { postDao.getPostsByUser(userId) }
    }

    @Test
    fun `getTodayMillis should return start of today`() {
        // When
        val result = localPostDataSource.getTodayMillis()
        
        // Then
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val expected = calendar.timeInMillis
        
        // Allow for small time differences (within 1 second)
        assertTrue(kotlin.math.abs(result - expected) < 1000)
    }

    @Test
    fun `incrementPostsToday should create new record when none exists`() = runTest {
        // Given
        val userId = "user1"
        coEvery { dailyPostCountDao.getPostCountForDate(userId, any()) } returns null
        coEvery { dailyPostCountDao.insertDailyPostCount(any()) } returns Unit

        // When
        localPostDataSource.createOriginalPost("Test post", userId)

        // Then
        coVerify { dailyPostCountDao.insertDailyPostCount(any()) }
    }

    @Test
    fun `incrementPostsToday should increment existing record`() = runTest {
        // Given
        val userId = "user1"
        val today = localPostDataSource.getTodayMillis().toString()
        coEvery { dailyPostCountDao.getPostCountForDate(userId, today) } returns 2
        coEvery { dailyPostCountDao.insertDailyPostCount(any()) } returns Unit
        coEvery { postDao.insertPost(any()) } returns Unit

        // When
        localPostDataSource.createOriginalPost("Test post", userId)

        // Then
        coVerify { dailyPostCountDao.insertDailyPostCount(any()) }
    }
}
