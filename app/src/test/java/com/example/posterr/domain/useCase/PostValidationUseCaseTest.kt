package com.example.posterr.domain.useCase

import com.example.posterr.domain.model.User
import com.example.posterr.domain.repository.PostRepository
import com.example.posterr.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PostValidationUseCaseTest {

    private lateinit var postValidationUseCase: PostValidationUseCase
    private lateinit var postRepository: PostRepository
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        postRepository = mockk()
        userRepository = mockk()
        postValidationUseCase = PostValidationUseCase(postRepository, userRepository)
    }

    @Test
    fun `validateOriginalPost should return Valid when all conditions are met`() = runTest {
        // Given
        val content = "Valid post content"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns true

        // When
        val result = postValidationUseCase.validateOriginalPost(content)

        // Then
        assertTrue("Result should be Valid", result is PostValidationUseCase.ValidationResult.Valid)
    }

    @Test
    fun `validateOriginalPost should return Invalid when user is not logged in`() = runTest {
        // Given
        val content = "Valid post content"
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = postValidationUseCase.validateOriginalPost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "User is not logged in", invalidResult.message)
    }

    @Test
    fun `validateOriginalPost should return Invalid when content is blank`() = runTest {
        // Given
        val content = "   "
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user

        // When
        val result = postValidationUseCase.validateOriginalPost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "Content cannot be empty", invalidResult.message)
    }

    @Test
    fun `validateOriginalPost should return Invalid when content is empty`() = runTest {
        // Given
        val content = ""
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user

        // When
        val result = postValidationUseCase.validateOriginalPost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "Content cannot be empty", invalidResult.message)
    }

    @Test
    fun `validateOriginalPost should return Invalid when content exceeds 777 characters`() = runTest {
        // Given
        val content = "a".repeat(778)
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user

        // When
        val result = postValidationUseCase.validateOriginalPost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "Content cannot exceed 777 characters", invalidResult.message)
    }

    @Test
    fun `validateOriginalPost should return Invalid when content is exactly 777 characters`() = runTest {
        // Given
        val content = "a".repeat(777)
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns true

        // When
        val result = postValidationUseCase.validateOriginalPost(content)

        // Then
        assertTrue("Result should be Valid", result is PostValidationUseCase.ValidationResult.Valid)
    }

    @Test
    fun `validateOriginalPost should return Invalid when daily limit is reached`() = runTest {
        // Given
        val content = "Valid post content"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns false

        // When
        val result = postValidationUseCase.validateOriginalPost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "Daily limit of 5 posts reached", invalidResult.message)
    }

    @Test
    fun `validateRepost should return Valid when all conditions are met`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns true

        // When
        val result = postValidationUseCase.validateRepost()

        // Then
        assertTrue("Result should be Valid", result is PostValidationUseCase.ValidationResult.Valid)
    }

    @Test
    fun `validateRepost should return Invalid when user is not logged in`() = runTest {
        // Given
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = postValidationUseCase.validateRepost()

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "User is not logged in", invalidResult.message)
    }

    @Test
    fun `validateRepost should return Invalid when daily limit is reached`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns false

        // When
        val result = postValidationUseCase.validateRepost()

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "Daily limit of 5 posts reached", invalidResult.message)
    }

    @Test
    fun `validateQuotePost should return Valid when all conditions are met`() = runTest {
        // Given
        val content = "Valid quote content"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns true

        // When
        val result = postValidationUseCase.validateQuotePost(content)

        // Then
        assertTrue("Result should be Valid", result is PostValidationUseCase.ValidationResult.Valid)
    }

    @Test
    fun `validateQuotePost should return Invalid when user is not logged in`() = runTest {
        // Given
        val content = "Valid quote content"
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = postValidationUseCase.validateQuotePost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "User is not logged in", invalidResult.message)
    }

    @Test
    fun `validateQuotePost should return Invalid when content is blank`() = runTest {
        // Given
        val content = "   "
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user

        // When
        val result = postValidationUseCase.validateQuotePost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "Content cannot be empty", invalidResult.message)
    }

    @Test
    fun `validateQuotePost should return Invalid when content exceeds 777 characters`() = runTest {
        // Given
        val content = "a".repeat(778)
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user

        // When
        val result = postValidationUseCase.validateQuotePost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "Content cannot exceed 777 characters", invalidResult.message)
    }

    @Test
    fun `validateQuotePost should return Invalid when daily limit is reached`() = runTest {
        // Given
        val content = "Valid quote content"
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns false

        // When
        val result = postValidationUseCase.validateQuotePost(content)

        // Then
        assertTrue("Result should be Invalid", result is PostValidationUseCase.ValidationResult.Invalid)
        val invalidResult = result as PostValidationUseCase.ValidationResult.Invalid
        assertEquals("Error message should match", "Daily limit of 5 posts reached", invalidResult.message)
    }

    @Test
    fun `getRemainingPostsToday should return correct remaining posts`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.getPostsCountToday(user.id) } returns 3

        // When
        val result = postValidationUseCase.getRemainingPostsToday()

        // Then
        assertEquals("Remaining posts should be 2", 2, result)
    }

    @Test
    fun `getRemainingPostsToday should return 0 when user has 5 posts`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.getPostsCountToday(user.id) } returns 5

        // When
        val result = postValidationUseCase.getRemainingPostsToday()

        // Then
        assertEquals("Remaining posts should be 0", 0, result)
    }

    @Test
    fun `getRemainingPostsToday should return 0 when user has more than 5 posts`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.getPostsCountToday(user.id) } returns 7

        // When
        val result = postValidationUseCase.getRemainingPostsToday()

        // Then
        assertEquals("Remaining posts should be 0", 0, result)
    }

    @Test
    fun `getRemainingPostsToday should return 0 when user is not logged in`() = runTest {
        // Given
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = postValidationUseCase.getRemainingPostsToday()

        // Then
        assertEquals("Remaining posts should be 0", 0, result)
    }

    @Test
    fun `getPostsCountToday should return correct count`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.getPostsCountToday(user.id) } returns 4

        // When
        val result = postValidationUseCase.getPostsCountToday()

        // Then
        assertEquals("Posts count should be 4", 4, result)
    }

    @Test
    fun `getPostsCountToday should return 0 when user is not logged in`() = runTest {
        // Given
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = postValidationUseCase.getPostsCountToday()

        // Then
        assertEquals("Posts count should be 0", 0, result)
    }

    @Test
    fun `canCreatePostToday should return true when user can post`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns true

        // When
        val result = postValidationUseCase.canCreatePostToday()

        // Then
        assertTrue("User should be able to create post", result)
    }

    @Test
    fun `canCreatePostToday should return false when user cannot post`() = runTest {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        coEvery { userRepository.getLoggedInUser() } returns user
        coEvery { postRepository.canCreatePostToday(user.id) } returns false

        // When
        val result = postValidationUseCase.canCreatePostToday()

        // Then
        assertFalse("User should not be able to create post", result)
    }

    @Test
    fun `canCreatePostToday should return false when user is not logged in`() = runTest {
        // Given
        coEvery { userRepository.getLoggedInUser() } returns null

        // When
        val result = postValidationUseCase.canCreatePostToday()

        // Then
        assertFalse("User should not be able to create post", result)
    }
}
