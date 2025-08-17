package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.model.User
import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CreatePostUseCaseTest {

    private lateinit var createPostUseCase: CreatePostUseCase
    private lateinit var postRepository: PostRepository
    private lateinit var userRepository: UserRepository
    private lateinit var validationUseCase: PostValidationUseCase

    @Before
    fun setUp() {
        postRepository = mockk()
        userRepository = mockk()
        validationUseCase = mockk()
        createPostUseCase = CreatePostUseCase(postRepository, userRepository, validationUseCase)
    }

    @Test
    fun `createOriginalPost should return success when validation passes and user is logged in`() = runTest {
        // Given
        val content = "Valid post content"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val post = Post(
            id = "post1",
            content = content,
            authorId = user.id,
            type = PostType.ORIGINAL,
            createdAt = 1234567890L
        )

        coEvery { validationUseCase.validateOriginalPost(content) } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.createOriginalPost(content, user.id) } returns PostResult.success(post)

        // When
        val result = createPostUseCase.createOriginalPost(content)

        // Then
        assertTrue("Result should be success", result is PostResult.Success)
        val successResult = result as PostResult.Success
        assertEquals("Post should match expected post", post, successResult.post)
    }

    @Test
    fun `createOriginalPost should return error when validation fails`() = runTest {
        // Given
        val content = "Invalid content"
        val errorMessage = "Content cannot be empty"

        coEvery { validationUseCase.validateOriginalPost(content) } returns PostValidationUseCase.ValidationResult.Invalid(errorMessage)

        // When
        val result = createPostUseCase.createOriginalPost(content)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", errorMessage, errorResult.message)
    }

    @Test
    fun `createOriginalPost should return error when user is not logged in`() = runTest {
        // Given
        val content = "Valid post content"

        coEvery { validationUseCase.validateOriginalPost(content) } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = createPostUseCase.createOriginalPost(content)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", "User is not logged in", errorResult.message)
    }

    @Test
    fun `createRepost should return success when validation passes and user is logged in`() = runTest {
        // Given
        val originalPostId = "originalPost1"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val post = Post(
            id = "repost1",
            content = "Reposted content",
            authorId = user.id,
            type = PostType.REPOST,
            createdAt = 1234567890L,
            originalPost = Post(
                id = originalPostId,
                content = "Original content",
                authorId = "originalAuthor",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )

        coEvery { validationUseCase.validateRepost() } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.createRepost(originalPostId, user.id) } returns PostResult.success(post)

        // When
        val result = createPostUseCase.createRepost(originalPostId)

        // Then
        assertTrue("Result should be success", result is PostResult.Success)
        val successResult = result as PostResult.Success
        assertEquals("Post should match expected post", post, successResult.post)
    }

    @Test
    fun `createRepost should return error when validation fails`() = runTest {
        // Given
        val originalPostId = "originalPost1"
        val errorMessage = "Daily limit of 5 posts reached"

        coEvery { validationUseCase.validateRepost() } returns PostValidationUseCase.ValidationResult.Invalid(errorMessage)

        // When
        val result = createPostUseCase.createRepost(originalPostId)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", errorMessage, errorResult.message)
    }

    @Test
    fun `createRepost should return error when user is not logged in`() = runTest {
        // Given
        val originalPostId = "originalPost1"

        coEvery { validationUseCase.validateRepost() } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = createPostUseCase.createRepost(originalPostId)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", "User is not logged in", errorResult.message)
    }

    @Test
    fun `createQuotePost should return success when validation passes and user is logged in`() = runTest {
        // Given
        val content = "Quote content"
        val originalPostId = "originalPost1"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val post = Post(
            id = "quote1",
            content = content,
            authorId = user.id,
            type = PostType.QUOTE,
            createdAt = 1234567890L,
            originalPost = Post(
                id = originalPostId,
                content = "Original content",
                authorId = "originalAuthor",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )

        coEvery { validationUseCase.validateQuotePost(content) } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.createQuotePost(content, originalPostId, user.id) } returns PostResult.success(post)

        // When
        val result = createPostUseCase.createQuotePost(content, originalPostId)

        // Then
        assertTrue("Result should be success", result is PostResult.Success)
        val successResult = result as PostResult.Success
        assertEquals("Post should match expected post", post, successResult.post)
    }

    @Test
    fun `createQuotePost should return error when validation fails`() = runTest {
        // Given
        val content = "Quote content"
        val originalPostId = "originalPost1"
        val errorMessage = "Content cannot exceed 777 characters"

        coEvery { validationUseCase.validateQuotePost(content) } returns PostValidationUseCase.ValidationResult.Invalid(errorMessage)

        // When
        val result = createPostUseCase.createQuotePost(content, originalPostId)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", errorMessage, errorResult.message)
    }

    @Test
    fun `createQuotePost should return error when user is not logged in`() = runTest {
        // Given
        val content = "Quote content"
        val originalPostId = "originalPost1"

        coEvery { validationUseCase.validateQuotePost(content) } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = createPostUseCase.createQuotePost(content, originalPostId)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", "User is not logged in", errorResult.message)
    }

    @Test
    fun `canCreatePostToday should delegate to validation use case`() = runTest {
        // Given
        coEvery { validationUseCase.canCreatePostToday() } returns true

        // When
        val result = createPostUseCase.canCreatePostToday()

        // Then
        assertTrue("Result should be true", result)
    }

    @Test
    fun `getPostsCountToday should delegate to validation use case`() = runTest {
        // Given
        coEvery { validationUseCase.getPostsCountToday() } returns 3

        // When
        val result = createPostUseCase.getPostsCountToday()

        // Then
        assertEquals("Posts count should be 3", 3, result)
    }

    @Test
    fun `getRemainingPostsToday should delegate to validation use case`() = runTest {
        // Given
        coEvery { validationUseCase.getRemainingPostsToday() } returns 2

        // When
        val result = createPostUseCase.getRemainingPostsToday()

        // Then
        assertEquals("Remaining posts should be 2", 2, result)
    }

    @Test
    fun `createOriginalPost should handle repository errors correctly`() = runTest {
        // Given
        val content = "Valid post content"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val errorMessage = "Database error"

        coEvery { validationUseCase.validateOriginalPost(content) } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.createOriginalPost(content, user.id) } returns PostResult.error(errorMessage)

        // When
        val result = createPostUseCase.createOriginalPost(content)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", errorMessage, errorResult.message)
    }

    @Test
    fun `createRepost should handle repository errors correctly`() = runTest {
        // Given
        val originalPostId = "originalPost1"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val errorMessage = "Original post not found"

        coEvery { validationUseCase.validateRepost() } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.createRepost(originalPostId, user.id) } returns PostResult.error(errorMessage)

        // When
        val result = createPostUseCase.createRepost(originalPostId)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", errorMessage, errorResult.message)
    }

    @Test
    fun `createQuotePost should handle repository errors correctly`() = runTest {
        // Given
        val content = "Quote content"
        val originalPostId = "originalPost1"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val errorMessage = "Original post not found"

        coEvery { validationUseCase.validateQuotePost(content) } returns PostValidationUseCase.ValidationResult.Valid
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.createQuotePost(content, originalPostId, user.id) } returns PostResult.error(errorMessage)

        // When
        val result = createPostUseCase.createQuotePost(content, originalPostId)

        // Then
        assertTrue("Result should be error", result is PostResult.Error)
        val errorResult = result as PostResult.Error
        assertEquals("Error message should match", errorMessage, errorResult.message)
    }
}
