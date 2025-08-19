package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.model.User
import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GetAllPostsUseCaseTest {

    private lateinit var getAllPostsUseCase: GetAllPostsUseCase
    private lateinit var postRepository: PostRepository
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        postRepository = mockk()
        userRepository = mockk()
        getAllPostsUseCase = GetAllPostsUseCase(postRepository, userRepository)
    }

    @Test
    fun `invoke should return posts with author usernames from repository`() = runTest {
        // Given
        val expectedPosts = listOf(
            Post(
                id = "post1",
                content = "First post",
                authorId = "user1",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            ),
            Post(
                id = "post2",
                content = "Second post",
                authorId = "user2",
                type = PostType.ORIGINAL,
                createdAt = 1234567891L
            )
        )
        val user1 = User(id = "user1", username = "testuser1", joinDate = 123L, isLoggedIn = false)
        val user2 = User(id = "user2", username = "testuser2", joinDate = 124L, isLoggedIn = false)
        
        coEvery { postRepository.getAllPosts() } returns expectedPosts
        coEvery { userRepository.getUserById("user1") } returns user1
        coEvery { userRepository.getUserById("user2") } returns user2

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 2, result.size)
        assertEquals("First post should match", expectedPosts[0], result[0].post)
        assertEquals("First author username should match", "testuser1", result[0].authorUsername)
        assertEquals("Second post should match", expectedPosts[1], result[1].post)
        assertEquals("Second author username should match", "testuser2", result[1].authorUsername)

        coVerify { postRepository.getAllPosts() }
        coVerify { userRepository.getUserById("user1") }
        coVerify { userRepository.getUserById("user2") }
    }

    @Test
    fun `invoke should return empty list when repository returns empty list`() = runTest {
        // Given
        coEvery { postRepository.getAllPosts() } returns emptyList()

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be empty", result.isEmpty())

        coVerify { postRepository.getAllPosts() }
    }

    @Test
    fun `invoke should return posts with different types`() = runTest {
        // Given
        val expectedPosts = listOf(
            Post(
                id = "post1",
                content = "Original post",
                authorId = "user1",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            ),
            Post(
                id = "repost1",
                content = "Reposted content",
                authorId = "user2",
                type = PostType.REPOST,
                createdAt = 1234567891L,
                originalPost = Post(
                    id = "post1",
                    content = "Original post",
                    authorId = "user1",
                    type = PostType.ORIGINAL,
                    createdAt = 1234567890L
                )
            ),
            Post(
                id = "quote1",
                content = "Quote content",
                authorId = "user3",
                type = PostType.QUOTE,
                createdAt = 1234567892L,
                originalPost = Post(
                    id = "post1",
                    content = "Original post",
                    authorId = "user1",
                    type = PostType.ORIGINAL,
                    createdAt = 1234567890L
                )
            )
        )
        val user1 = User(id = "user1", username = "user1name", joinDate = 123L, isLoggedIn = false)
        val user2 = User(id = "user2", username = "user2name", joinDate = 124L, isLoggedIn = false)
        val user3 = User(id = "user3", username = "user3name", joinDate = 125L, isLoggedIn = false)
        
        coEvery { postRepository.getAllPosts() } returns expectedPosts
        coEvery { userRepository.getUserById("user1") } returns user1
        coEvery { userRepository.getUserById("user2") } returns user2
        coEvery { userRepository.getUserById("user3") } returns user3

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 3, result.size)
        assertEquals("Original post type should match", PostType.ORIGINAL, result[0].post.type)
        assertEquals("Repost type should match", PostType.REPOST, result[1].post.type)
        assertEquals("Quote type should match", PostType.QUOTE, result[2].post.type)

        coVerify { postRepository.getAllPosts() }
    }

    @Test
    fun `invoke should handle single post correctly`() = runTest {
        // Given
        val expectedPost = Post(
            id = "post1",
            content = "Single post",
            authorId = "user1",
            type = PostType.ORIGINAL,
            createdAt = 1234567890L
        )
        val user1 = User(id = "user1", username = "testuser", joinDate = 123L, isLoggedIn = false)
        
        coEvery { postRepository.getAllPosts() } returns listOf(expectedPost)
        coEvery { userRepository.getUserById("user1") } returns user1

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should be 1", 1, result.size)
        assertEquals("Post should match", expectedPost, result[0].post)
        assertEquals("Username should match", "testuser", result[0].authorUsername)

        coVerify { postRepository.getAllPosts() }
        coVerify { userRepository.getUserById("user1") }
    }

    @Test
    fun `invoke should use authorId as fallback when user not found`() = runTest {
        // Given
        val expectedPost = Post(
            id = "post1",
            content = "Post with missing user",
            authorId = "missinguser",
            type = PostType.ORIGINAL,
            createdAt = 1234567890L
        )
        
        coEvery { postRepository.getAllPosts() } returns listOf(expectedPost)
        coEvery { userRepository.getUserById("missinguser") } returns null

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should be 1", 1, result.size)
        assertEquals("Post should match", expectedPost, result[0].post)
        assertEquals("Should fallback to authorId", "missinguser", result[0].authorUsername)

        coVerify { postRepository.getAllPosts() }
        coVerify { userRepository.getUserById("missinguser") }
    }
}
