package com.example.posterr.data.datasource

import com.example.posterr.data.dao.UserDao
import com.example.posterr.data.dao.UserStatsDao
import com.example.posterr.data.entity.UserEntity
import com.example.posterr.data.entity.UserStatsEntity
import com.example.posterr.domain.model.User
import com.example.posterr.domain.model.UserStats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class LocalUserDataSourceTest {

    private lateinit var localUserDataSource: LocalUserDataSource
    private lateinit var userDao: UserDao
    private lateinit var userStatsDao: UserStatsDao

    @Before
    fun setUp() {
        userDao = mockk()
        userStatsDao = mockk()
        localUserDataSource = LocalUserDataSource(userDao, userStatsDao)
    }

    @Test
    fun `getLoggedInUser should return user when user exists`() = runTest {
        // Given
        val userEntity = UserEntity(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userDao.getLoggedInUser() } returns userEntity

        // When
        val result = localUserDataSource.getLoggedInUser()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User ID should match", "user1", result?.id)
        assertEquals("Username should match", "testuser", result?.username)
        assertEquals("Join date should match", 1234567890L, result?.joinDate)
        assertTrue("User should be logged in", result?.isLoggedIn == true)

        coVerify { userDao.getLoggedInUser() }
    }

    @Test
    fun `getLoggedInUser should return null when no user exists`() = runTest {
        // Given
        coEvery { userDao.getLoggedInUser() } returns null

        // When
        val result = localUserDataSource.getLoggedInUser()

        // Then
        assertNull("Result should be null", result)

        coVerify { userDao.getLoggedInUser() }
    }

    @Test
    fun `getUserById should return user when user exists`() = runTest {
        // Given
        val userId = "user1"
        val userEntity = UserEntity(
            id = userId,
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = false
        )
        coEvery { userDao.getUserById(userId) } returns userEntity

        // When
        val result = localUserDataSource.getUserById(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User ID should match", userId, result?.id)
        assertEquals("Username should match", "testuser", result?.username)
        assertEquals("Join date should match", 1234567890L, result?.joinDate)
        assertFalse("User should not be logged in", result?.isLoggedIn == true)

        coVerify { userDao.getUserById(userId) }
    }

    @Test
    fun `getUserById should return null when user does not exist`() = runTest {
        // Given
        val userId = "nonexistent"
        coEvery { userDao.getUserById(userId) } returns null

        // When
        val result = localUserDataSource.getUserById(userId)

        // Then
        assertNull("Result should be null", result)

        coVerify { userDao.getUserById(userId) }
    }

    @Test
    fun `getUserStats should return user stats when stats exist`() = runTest {
        // Given
        val userId = "user1"
        val userStatsEntity = UserStatsEntity(
            userId = userId,
            originalPostsCount = 5,
            repostCount = 3,
            quoteCount = 2
        )
        coEvery { userStatsDao.getUserStats(userId) } returns userStatsEntity

        // When
        val result = localUserDataSource.getUserStats(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User ID should match", userId, result.userId)
        assertEquals("Original posts count should match", 5, result.originalPostsCount)
        assertEquals("Repost count should match", 3, result.repostCount)
        assertEquals("Quote count should match", 2, result.quoteCount)
        assertEquals("Total posts should be calculated correctly", 10, result.totalPosts)

        coVerify { userStatsDao.getUserStats(userId) }
    }

    @Test
    fun `getUserStats should return default stats when no stats exist`() = runTest {
        // Given
        val userId = "user1"
        coEvery { userStatsDao.getUserStats(userId) } returns null

        // When
        val result = localUserDataSource.getUserStats(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User ID should match", userId, result.userId)
        assertEquals("Original posts count should be default", 0, result.originalPostsCount)
        assertEquals("Repost count should be default", 0, result.repostCount)
        assertEquals("Quote count should be default", 0, result.quoteCount)
        assertEquals("Total posts should be 0", 0, result.totalPosts)

        coVerify { userStatsDao.getUserStats(userId) }
    }

    @Test
    fun `getUserStats should handle edge case with zero counts`() = runTest {
        // Given
        val userId = "user1"
        val userStatsEntity = UserStatsEntity(
            userId = userId,
            originalPostsCount = 0,
            repostCount = 0,
            quoteCount = 0
        )
        coEvery { userStatsDao.getUserStats(userId) } returns userStatsEntity

        // When
        val result = localUserDataSource.getUserStats(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User ID should match", userId, result.userId)
        assertEquals("Original posts count should be 0", 0, result.originalPostsCount)
        assertEquals("Repost count should be 0", 0, result.repostCount)
        assertEquals("Quote count should be 0", 0, result.quoteCount)
        assertEquals("Total posts should be 0", 0, result.totalPosts)

        coVerify { userStatsDao.getUserStats(userId) }
    }

    @Test
    fun `getUserStats should handle large counts correctly`() = runTest {
        // Given
        val userId = "user1"
        val userStatsEntity = UserStatsEntity(
            userId = userId,
            originalPostsCount = 1000,
            repostCount = 500,
            quoteCount = 250
        )
        coEvery { userStatsDao.getUserStats(userId) } returns userStatsEntity

        // When
        val result = localUserDataSource.getUserStats(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User ID should match", userId, result.userId)
        assertEquals("Original posts count should match", 1000, result.originalPostsCount)
        assertEquals("Repost count should match", 500, result.repostCount)
        assertEquals("Quote count should match", 250, result.quoteCount)
        assertEquals("Total posts should be calculated correctly", 1750, result.totalPosts)

        coVerify { userStatsDao.getUserStats(userId) }
    }
}
