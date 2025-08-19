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
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match", user, result?.user)
        assertEquals("Posts should match", posts, result?.posts)
        
        // Stats should be calculated from posts
        assertEquals("Original posts count should be 2", 2, result?.stats?.originalPostsCount)
        assertEquals("Repost count should be 0", 0, result?.stats?.repostCount)
        assertEquals("Quote count should be 0", 0, result?.stats?.quoteCount)

        coVerify { userRepository.getUserById(userId) }
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
        val posts = emptyList<Post>()

        coEvery { userRepository.getUserById(userId) } returns user
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match", user, result?.user)
        assertTrue("Posts should be empty", result?.posts?.isEmpty() == true)
        
        // Stats should be calculated from empty posts
        assertEquals("Original posts count should be 0", 0, result?.stats?.originalPostsCount)
        assertEquals("Repost count should be 0", 0, result?.stats?.repostCount)
        assertEquals("Quote count should be 0", 0, result?.stats?.quoteCount)

        coVerify { userRepository.getUserById(userId) }
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
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match", user, result?.user)
        assertEquals("User stats should be calculated from posts", 1, result?.stats?.originalPostsCount)
        assertEquals("User stats should be calculated from posts", 0, result?.stats?.repostCount)
        assertEquals("User stats should be calculated from posts", 0, result?.stats?.quoteCount)
        assertEquals("Posts should match", posts, result?.posts)

        coVerify { userRepository.getUserById(userId) }
        coVerify { postRepository.getPostsByUser(userId) }
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
        coVerify(exactly = 0) { postRepository.getPostsByUser(any()) }
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
        coEvery { postRepository.getPostsByUser(user.id) } returns posts

        // When
        val result = getUserProfileUseCase.getLoggedInUserProfile()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("User should match", user, result?.user)
        assertEquals("User stats should be calculated from posts", 1, result?.stats?.originalPostsCount)
        assertEquals("User stats should be calculated from posts", 0, result?.stats?.repostCount)
        assertEquals("User stats should be calculated from posts", 0, result?.stats?.quoteCount)
        assertEquals("Posts should match", posts, result?.posts)

        coVerify { userRepository.getLoggedInUser() }
        coVerify { postRepository.getPostsByUser(user.id) }
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
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 3, result?.posts?.size)
        assertEquals("Original post type should match", PostType.ORIGINAL, result?.posts?.get(0)?.type)
        assertEquals("Repost type should match", PostType.REPOST, result?.posts?.get(1)?.type)
        assertEquals("Quote type should match", PostType.QUOTE, result?.posts?.get(2)?.type)
        
        // Stats should be calculated from posts
        assertEquals("Original posts count should be 1", 1, result?.stats?.originalPostsCount)
        assertEquals("Repost count should be 1", 1, result?.stats?.repostCount)
        assertEquals("Quote count should be 1", 1, result?.stats?.quoteCount)

        coVerify { userRepository.getUserById(userId) }
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
        coEvery { postRepository.getPostsByUser(userId) } returns posts

        // When
        val result = getUserProfileUseCase(userId)

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 100, result?.posts?.size)
        assertEquals("First post should match", posts[0], result?.posts?.get(0))
        assertEquals("Last post should match", posts[99], result?.posts?.get(99))
        
        // Stats should be calculated from posts (100 posts, ~33 of each type)
        val totalPosts = result?.stats?.originalPostsCount?.plus(result?.stats?.repostCount ?: 0)?.plus(result?.stats?.quoteCount ?: 0) ?: 0
        assertEquals("Total posts count should be 100", 100, totalPosts)
        assertTrue("Original posts count should be reasonable", (result?.stats?.originalPostsCount ?: 0) in 30..40)
        assertTrue("Repost count should be reasonable", (result?.stats?.repostCount ?: 0) in 30..40)
        assertTrue("Quote count should be reasonable", (result?.stats?.quoteCount ?: 0) in 30..40)

        coVerify { userRepository.getUserById(userId) }
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
