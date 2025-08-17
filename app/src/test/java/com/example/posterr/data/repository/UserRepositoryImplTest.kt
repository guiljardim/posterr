package com.example.posterr.data.repository

import com.example.posterr.data.datasource.LocalUserDataSource
import com.example.posterr.domain.model.User
import com.example.posterr.domain.model.UserStats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class UserRepositoryImplTest {

    private lateinit var userRepository: UserRepositoryImpl
    private lateinit var localDataSource: LocalUserDataSource

    @Before
    fun setUp() {
        localDataSource = mockk()
        userRepository = UserRepositoryImpl(localDataSource)
    }

    @Test
    fun `getLoggedInUser should delegate to local data source and return user`() = runTest {
        // Given
        val expectedUser = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { localDataSource.getLoggedInUser() } returns expectedUser

        // When
        val result = userRepository.getLoggedInUser()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match expected user", expectedUser, result)
        assertEquals("User ID should match", "user1", result?.id)
        assertEquals("Username should match", "testuser", result?.username)
        assertEquals("Join date should match", 1234567890L, result?.joinDate)
        assertTrue("User should be logged in", result?.isLoggedIn == true)

        coVerify { localDataSource.getLoggedInUser() }
    }

    @Test
    fun `getLoggedInUser should delegate to local data source and return null`() = runTest {
        // Given
        coEvery { localDataSource.getLoggedInUser() } returns null

        // When
        val result = userRepository.getLoggedInUser()

        // Then
        assertNull("Result should be null", result)

        coVerify { localDataSource.getLoggedInUser() }
    }

    @Test
    fun `getUserById should delegate to local data source and return user`() = runTest {
        // Given
        val userId = "user1"
        val expectedUser = User(
            id = userId,
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = false
        )
        coEvery { localDataSource.getUserById(userId) } returns expectedUser

        // When
        val result = userRepository.getUserById(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match expected user", expectedUser, result)
        assertEquals("User ID should match", userId, result?.id)
        assertEquals("Username should match", "testuser", result?.username)
        assertEquals("Join date should match", 1234567890L, result?.joinDate)
        assertFalse("User should not be logged in", result?.isLoggedIn == true)

        coVerify { localDataSource.getUserById(userId) }
    }

    @Test
    fun `getUserById should delegate to local data source and return null`() = runTest {
        // Given
        val userId = "nonexistent"
        coEvery { localDataSource.getUserById(userId) } returns null

        // When
        val result = userRepository.getUserById(userId)

        // Then
        assertNull("Result should be null", result)

        coVerify { localDataSource.getUserById(userId) }
    }

    @Test
    fun `getUserStats should delegate to local data source and return user stats`() = runTest {
        // Given
        val userId = "user1"
        val expectedUserStats = UserStats(
            userId = userId,
            originalPostsCount = 5,
            repostCount = 3,
            quoteCount = 2
        )
        coEvery { localDataSource.getUserStats(userId) } returns expectedUserStats

        // When
        val result = userRepository.getUserStats(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User stats should match expected stats", expectedUserStats, result)
        assertEquals("User ID should match", userId, result.userId)
        assertEquals("Original posts count should match", 5, result.originalPostsCount)
        assertEquals("Repost count should match", 3, result.repostCount)
        assertEquals("Quote count should match", 2, result.quoteCount)
        assertEquals("Total posts should be calculated correctly", 10, result.totalPosts)

        coVerify { localDataSource.getUserStats(userId) }
    }

    @Test
    fun `getUserStats should delegate to local data source and return default stats`() = runTest {
        // Given
        val userId = "user1"
        val expectedUserStats = UserStats(userId = userId)
        coEvery { localDataSource.getUserStats(userId) } returns expectedUserStats

        // When
        val result = userRepository.getUserStats(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User stats should match expected stats", expectedUserStats, result)
        assertEquals("User ID should match", userId, result.userId)
        assertEquals("Original posts count should be default", 0, result.originalPostsCount)
        assertEquals("Repost count should be default", 0, result.repostCount)
        assertEquals("Quote count should be default", 0, result.quoteCount)
        assertEquals("Total posts should be 0", 0, result.totalPosts)

        coVerify { localDataSource.getUserStats(userId) }
    }

    @Test
    fun `getUserStats should handle edge case with zero counts`() = runTest {
        // Given
        val userId = "user1"
        val expectedUserStats = UserStats(
            userId = userId,
            originalPostsCount = 0,
            repostCount = 0,
            quoteCount = 0
        )
        coEvery { localDataSource.getUserStats(userId) } returns expectedUserStats

        // When
        val result = userRepository.getUserStats(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User stats should match expected stats", expectedUserStats, result)
        assertEquals("User ID should match", userId, result.userId)
        assertEquals("Original posts count should be 0", 0, result.originalPostsCount)
        assertEquals("Repost count should be 0", 0, result.repostCount)
        assertEquals("Quote count should be 0", 0, result.quoteCount)
        assertEquals("Total posts should be 0", 0, result.totalPosts)

        coVerify { localDataSource.getUserStats(userId) }
    }

    @Test
    fun `getUserStats should handle large counts correctly`() = runTest {
        // Given
        val userId = "user1"
        val expectedUserStats = UserStats(
            userId = userId,
            originalPostsCount = 1000,
            repostCount = 500,
            quoteCount = 250
        )
        coEvery { localDataSource.getUserStats(userId) } returns expectedUserStats

        // When
        val result = userRepository.getUserStats(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User stats should match expected stats", expectedUserStats, result)
        assertEquals("User ID should match", userId, result.userId)
        assertEquals("Original posts count should match", 1000, result.originalPostsCount)
        assertEquals("Repost count should match", 500, result.repostCount)
        assertEquals("Quote count should match", 250, result.quoteCount)
        assertEquals("Total posts should be calculated correctly", 1750, result.totalPosts)

        coVerify { localDataSource.getUserStats(userId) }
    }

    @Test
    fun `repository should implement UserRepository interface`() {
        // Then
        assertTrue("UserRepositoryImpl should implement UserRepository", 
                  userRepository is com.example.posterr.domain.repository.UserRepository)
    }
}
