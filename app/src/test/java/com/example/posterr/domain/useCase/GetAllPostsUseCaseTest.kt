package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.repository.PostRepository
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

    @Before
    fun setUp() {
        postRepository = mockk()
        getAllPostsUseCase = GetAllPostsUseCase(postRepository)
    }

    @Test
    fun `invoke should return posts from repository`() = runTest {
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
        coEvery { postRepository.getAllPosts() } returns expectedPosts

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 2, result.size)
        assertEquals("First post should match", expectedPosts[0], result[0])
        assertEquals("Second post should match", expectedPosts[1], result[1])

        coVerify { postRepository.getAllPosts() }
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
        coEvery { postRepository.getAllPosts() } returns expectedPosts

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 3, result.size)
        assertEquals("Original post type should match", PostType.ORIGINAL, result[0].type)
        assertEquals("Repost type should match", PostType.REPOST, result[1].type)
        assertEquals("Quote type should match", PostType.QUOTE, result[2].type)

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
        coEvery { postRepository.getAllPosts() } returns listOf(expectedPost)

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should be 1", 1, result.size)
        assertEquals("Post should match", expectedPost, result[0])

        coVerify { postRepository.getAllPosts() }
    }

    @Test
    fun `invoke should handle large number of posts`() = runTest {
        // Given
        val expectedPosts = (1..100).map { index ->
            Post(
                id = "post$index",
                content = "Post number $index",
                authorId = "user${index % 5}",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L + index
            )
        }
        coEvery { postRepository.getAllPosts() } returns expectedPosts

        // When
        val result = getAllPostsUseCase()

        // Then
        assertNotNull("Result should not be null", result)
        assertEquals("Posts count should match", 100, result.size)
        assertEquals("First post should match", expectedPosts[0], result[0])
        assertEquals("Last post should match", expectedPosts[99], result[99])

        coVerify { postRepository.getAllPosts() }
    }
}
