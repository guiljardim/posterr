package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.model.User
import com.example.posterr.domain.model.UserStats
import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetUserProfileUseCaseTest {

    private lateinit var getUserProfileUseCase: GetUserProfileUseCase
    private lateinit var userRepository: UserRepository
    private lateinit var postRepository: PostRepository

    @Before
    fun setUp() {
        userRepository = mockk()
        postRepository = mockk()
        getUserProfileUseCase = GetUserProfileUseCase(userRepository, postRepository)
    }

    @Test
    fun `invoke should return UserProfileData when user exists`() = runTest {
        // Given
        val userId = "user1"
        val user = User(
            id = userId,
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(
            userId = userId,
            originalPostsCount = 5,
            repostCount = 3,
            quoteCount = 2
        )
        val posts = listOf(
            Post(
                id = "post1",
                content = "First post",
                authorId = userId,
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            ),
            Post(
                id = "post2",
                content = "Second post",
                authorId = userId,
                type = PostType.ORIGINAL,
                createdAt = 1234567891L
            )
        )

        coEvery { userRepository.getUserById(userId) } returns user
        coEvery { userRepository.getUserStats(userId) } returns userStats
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match", user, result?.user)
        assertEquals("User stats should match", userStats, result?.stats)
        assertEquals("Posts should match", posts, result?.posts)

        coVerify { userRepository.getUserById(userId) }
        coVerify { userRepository.getUserStats(userId) }
        coVerify { postRepository.getPostsByUser(userId) }
    }

    @Test
    fun `invoke should return null when user does not exist`() = runTest {
        // Given
        val userId = "nonexistent"
        coEvery { userRepository.getUserById(userId) } returns null

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNull("Result should be null", result)

        coVerify { userRepository.getUserById(userId) }
        coVerify(exactly = 0) { userRepository.getUserStats(any()) }
        coVerify(exactly = 0) { postRepository.getPostsByUser(any()) }
    }

    @Test
    fun `invoke should return UserProfileData with empty posts when user has no posts`() = runTest {
        // Given
        val userId = "user1"
        val user = User(
            id = userId,
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(userId = userId)
        val posts = emptyList<Post>()

        coEvery { userRepository.getUserById(userId) } returns user
        coEvery { userRepository.getUserStats(userId) } returns userStats
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match", user, result?.user)
        assertEquals("User stats should match", userStats, result?.stats)
        assertTrue("Posts should be empty", result?.posts?.isEmpty() == true)

        coVerify { userRepository.getUserById(userId) }
        coVerify { userRepository.getUserStats(userId) }
        coVerify { postRepository.getPostsByUser(userId) }
    }

    @Test
    fun `invoke should return UserProfileData with default stats when user has no stats`() = runTest {
        // Given
        val userId = "user1"
        val user = User(
            id = userId,
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(userId = userId)
        val posts = listOf(
            Post(
                id = "post1",
                content = "First post",
                authorId = userId,
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )

        coEvery { userRepository.getUserById(userId) } returns user
        coEvery { userRepository.getUserStats(userId) } returns userStats
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match", user, result?.user)
        assertEquals("User stats should have default values", 0, result?.stats?.originalPostsCount)
        assertEquals("User stats should have default values", 0, result?.stats?.repostCount)
        assertEquals("User stats should have default values", 0, result?.stats?.quoteCount)
        assertEquals("Posts should match", posts, result?.posts)

        coVerify { userRepository.getUserById(userId) }
        coVerify { userRepository.getUserStats(userId) }
        coVerify { postRepository.getPostsByUser(userId) }
    }

    @Test
    fun `getLoggedInUserProfile should return UserProfileData when user is logged in`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(
            userId = user.id,
            originalPostsCount = 3,
            repostCount = 1,
            quoteCount = 1
        )
        val posts = listOf(
            Post(
                id = "post1",
                content = "Logged in user post",
                authorId = user.id,
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )

        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { userRepository.getUserById(user.id) } returns user
        coEvery { userRepository.getUserStats(user.id) } returns userStats
        coEvery { postRepository.getPostsByUser(user.id) } returns posts

        // When
        val result = getUserProfileUseCase.getLoggedInUserProfile()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match", user, result?.user)
        assertEquals("User stats should match", userStats, result?.stats)
        assertEquals("Posts should match", posts, result?.posts)

        coVerify { userRepository.getLoggedInUser() }
        coVerify { userRepository.getUserStats(user.id) }
        coVerify { postRepository.getPostsByUser(user.id) }
    }

    @Test
    fun `getLoggedInUserProfile should return null when no user is logged in`() = runTest {
        // Given
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = getUserProfileUseCase.getLoggedInUserProfile()

        // Then
        assertNull("Result should be null", result)

        coVerify { userRepository.getLoggedInUser() }
        coVerify(exactly = 0) { userRepository.getUserStats(any()) }
        coVerify(exactly = 0) { postRepository.getPostsByUser(any()) }
    }

    @Test
    fun `invoke should handle posts with different types correctly`() = runTest {
        // Given
        val userId = "user1"
        val user = User(
            id = userId,
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(
            userId = userId,
            originalPostsCount = 2,
            repostCount = 1,
            quoteCount = 1
        )
        val posts = listOf(
            Post(
                id = "post1",
                content = "Original post",
                authorId = userId,
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            ),
            Post(
                id = "repost1",
                content = "Reposted content",
                authorId = userId,
                type = PostType.REPOST,
                createdAt = 1234567891L,
                originalPost = Post(
                    id = "post1",
                    content = "Original post",
                    authorId = userId,
                    type = PostType.ORIGINAL,
                    createdAt = 1234567890L
                )
            ),
            Post(
                id = "quote1",
                content = "Quote content",
                authorId = userId,
                type = PostType.QUOTE,
                createdAt = 1234567892L,
                originalPost = Post(
                    id = "post1",
                    content = "Original post",
                    authorId = userId,
                    type = PostType.ORIGINAL,
                    createdAt = 1234567890L
                )
            )
        )

        coEvery { userRepository.getUserById(userId) } returns user
        coEvery { userRepository.getUserStats(userId) } returns userStats
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 3, result?.posts?.size)
        assertEquals("Original post type should match", PostType.ORIGINAL, result?.posts?.get(0)?.type)
        assertEquals("Repost type should match", PostType.REPOST, result?.posts?.get(1)?.type)
        assertEquals("Quote type should match", PostType.QUOTE, result?.posts?.get(2)?.type)

        coVerify { userRepository.getUserById(userId) }
        coVerify { userRepository.getUserStats(userId) }
        coVerify { postRepository.getPostsByUser(userId) }
    }

    @Test
    fun `invoke should handle large number of posts correctly`() = runTest {
        // Given
        val userId = "user1"
        val user = User(
            id = userId,
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(
            userId = userId,
            originalPostsCount = 50,
            repostCount = 25,
            quoteCount = 25
        )
        val posts = (1..100).map { index ->
            when (index % 3) {
                0 -> Post(
                    id = "post$index",
                    content = "Post number $index",
                    authorId = userId,
                    type = PostType.ORIGINAL,
                    createdAt = 1234567890L + index
                )
                1 -> Post(
                    id = "post$index",
                    content = "Reposted content",
                    authorId = userId,
                    type = PostType.REPOST,
                    createdAt = 1234567890L + index,
                    originalPost = Post(
                        id = "original$index",
                        content = "Original content",
                        authorId = "originalAuthor",
                        type = PostType.ORIGINAL,
                        createdAt = 1234567890L
                    )
                )
                else -> Post(
                    id = "post$index",
                    content = "Quote content",
                    authorId = userId,
                    type = PostType.QUOTE,
                    createdAt = 1234567890L + index,
                    originalPost = Post(
                        id = "original$index",
                        content = "Original content",
                        authorId = "originalAuthor",
                        type = PostType.ORIGINAL,
                        createdAt = 1234567890L
                    )
                )
            }
        }

        coEvery { userRepository.getUserById(userId) } returns user
        coEvery { userRepository.getUserStats(userId) } returns userStats
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 100, result?.posts?.size)
        assertEquals("First post should match", posts[0], result?.posts?.get(0))
        assertEquals("Last post should match", posts[99], result?.posts?.get(99))

        coVerify { userRepository.getUserById(userId) }
        coVerify { userRepository.getUserStats(userId) }
        coVerify { postRepository.getPostsByUser(userId) }
    }

    @Test
    fun `UserProfileData should have correct structure`() {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(
            userId = "user1",
            originalPostsCount = 5,
            repostCount = 3,
            quoteCount = 2
        )
        val posts = emptyList<Post>()

        // When
        val userProfileData = UserProfileData(user, userStats, posts)

        // Then
        assertEquals("User should match", user, userProfileData.user)
        assertEquals("User stats should match", userStats, userProfileData.stats)
        assertEquals("Posts should match", posts, userProfileData.posts)
    }
}
