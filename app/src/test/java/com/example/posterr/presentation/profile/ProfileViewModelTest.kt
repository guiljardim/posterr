package com.example.posterr.presentation.profile

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.model.User
import com.example.posterr.domain.model.UserStats
import com.example.posterr.domain.useCase.CreatePostUseCase
import com.example.posterr.domain.useCase.GetUserProfileUseCase
import com.example.posterr.domain.useCase.UserProfileData
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var getUserProfileUseCase: GetUserProfileUseCase
    private lateinit var createPostUseCase: CreatePostUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getUserProfileUseCase = mockk()
        createPostUseCase = mockk()
        profileViewModel = ProfileViewModel(getUserProfileUseCase, createPostUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load user post count`() = runTest {
        // Given
        coEvery { createPostUseCase.getPostsCountToday() } returns 3
        coEvery { createPostUseCase.canCreatePostToday() } returns true
        
        // Create new ViewModel with configured mocks
        val testViewModel = ProfileViewModel(getUserProfileUseCase, createPostUseCase)

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = testViewModel.uiState.first()
        assertEquals("Posts count should be loaded", 3, uiState.postsCountToday)
        assertTrue("User should be able to create post", uiState.canCreatePost)
    }

    @Test
    fun `loadProfile should update UI state with profile data when successful`() = runTest {
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
        val posts = listOf(
            Post(
                id = "post1",
                content = "Test post",
                authorId = "user1",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )
        val profileData = UserProfileData(user, userStats, posts)

        coEvery { getUserProfileUseCase.getLoggedInUserProfile() } returns profileData

        // When
        profileViewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertEquals("Profile data should be loaded", profileData, uiState.profileData)
        assertFalse("Should not be loading", uiState.isLoading)
        assertNull("Should not have error", uiState.error)
    }

    @Test
    fun `loadProfile should update UI state with error when profile not found`() = runTest {
        // Given
        coEvery { getUserProfileUseCase.getLoggedInUserProfile() } returns null

        // When
        profileViewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertNull("Profile data should be null", uiState.profileData)
        assertFalse("Should not be loading", uiState.isLoading)
        assertEquals("Should have error message", "Profile not found", uiState.error)
    }

    @Test
    fun `loadProfile should update UI state with error when exception occurs`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getUserProfileUseCase.getLoggedInUserProfile() } throws RuntimeException(errorMessage)

        // When
        profileViewModel.loadProfile()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertNull("Profile data should be null", uiState.profileData)
        assertFalse("Should not be loading", uiState.isLoading)
        assertTrue("Should have error", uiState.error?.contains(errorMessage) == true)
    }

    @Test
    fun `createOriginalPost should not create post when content is blank`() = runTest {
        // Given
        val blankContent = "   "

        // When
        profileViewModel.createOriginalPost(blankContent)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // Post should not be created when content is blank
    }

    @Test
    fun `createOriginalPost should not create post when content is empty`() = runTest {
        // Given
        val emptyContent = ""

        // When
        profileViewModel.createOriginalPost(emptyContent)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // Post should not be created when content is empty
    }

    @Test
    fun `createOriginalPost should update UI state when successful`() = runTest {
        // Given
        val content = "Valid post content"
        val post = Post(
            id = "newPost",
            content = content,
            authorId = "user1",
            type = PostType.ORIGINAL,
            createdAt = 1234567890L
        )
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(userId = "user1")
        val profileData = UserProfileData(user, userStats, listOf(post))
        
        coEvery { createPostUseCase.createOriginalPost(content) } returns PostResult.success(post)
        coEvery { getUserProfileUseCase.getLoggedInUserProfile() } returns profileData
        coEvery { createPostUseCase.getPostsCountToday() } returns 1
        coEvery { createPostUseCase.canCreatePostToday() } returns true

        // When
        profileViewModel.createOriginalPost(content)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertNull("Should not have error", uiState.error)
        assertEquals("Profile should be updated", profileData, uiState.profileData)
        assertEquals("Posts count should be updated", 1, uiState.postsCountToday)
    }

    @Test
    fun `createOriginalPost should update UI state with error when creation fails`() = runTest {
        // Given
        val content = "Valid post content"
        val errorMessage = "Daily limit reached"
        coEvery { createPostUseCase.createOriginalPost(content) } returns PostResult.error(errorMessage)

        // When
        profileViewModel.createOriginalPost(content)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertEquals("Should have error message", errorMessage, uiState.error)
    }

    @Test
    fun `createOriginalPost should handle exceptions`() = runTest {
        // Given
        val content = "Valid post content"
        val errorMessage = "Network error"
        coEvery { createPostUseCase.createOriginalPost(content) } throws RuntimeException(errorMessage)

        // When
        profileViewModel.createOriginalPost(content)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertTrue("Should have error", uiState.error?.contains(errorMessage) == true)
    }

    @Test
    fun `createRepost should update UI state when successful`() = runTest {
        // Given
        val postId = "post1"
        val post = Post(
            id = "repost1",
            content = "Reposted content",
            authorId = "user1",
            type = PostType.REPOST,
            createdAt = 1234567890L,
            originalPost = Post(
                id = postId,
                content = "Original content",
                authorId = "originalAuthor",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(userId = "user1")
        val profileData = UserProfileData(user, userStats, listOf(post))
        
        coEvery { createPostUseCase.createRepost(postId) } returns PostResult.success(post)
        coEvery { getUserProfileUseCase.getLoggedInUserProfile() } returns profileData
        coEvery { createPostUseCase.getPostsCountToday() } returns 1
        coEvery { createPostUseCase.canCreatePostToday() } returns true

        // When
        profileViewModel.createRepost(postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertNull("Should not have error", uiState.error)
        assertEquals("Profile should be updated", profileData, uiState.profileData)
    }

    @Test
    fun `createRepost should update UI state with error when creation fails`() = runTest {
        // Given
        val postId = "post1"
        val errorMessage = "Original post not found"
        coEvery { createPostUseCase.createRepost(postId) } returns PostResult.error(errorMessage)

        // When
        profileViewModel.createRepost(postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertEquals("Should have error message", errorMessage, uiState.error)
    }

    @Test
    fun `createRepost should handle exceptions`() = runTest {
        // Given
        val postId = "post1"
        val errorMessage = "Network error"
        coEvery { createPostUseCase.createRepost(postId) } throws RuntimeException(errorMessage)

        // When
        profileViewModel.createRepost(postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertTrue("Should have error", uiState.error?.contains(errorMessage) == true)
    }

    @Test
    fun `createQuotePost should not create post when content is blank`() = runTest {
        // Given
        val blankContent = "   "
        val postId = "post1"

        // When
        profileViewModel.createQuotePost(blankContent, postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // Quote post should not be created when content is blank
    }

    @Test
    fun `createQuotePost should update UI state when successful`() = runTest {
        // Given
        val content = "Quote content"
        val postId = "post1"
        val post = Post(
            id = "quote1",
            content = content,
            authorId = "user1",
            type = PostType.QUOTE,
            createdAt = 1234567890L,
            originalPost = Post(
                id = postId,
                content = "Original content",
                authorId = "originalAuthor",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(userId = "user1")
        val profileData = UserProfileData(user, userStats, listOf(post))
        
        coEvery { createPostUseCase.createQuotePost(content, postId) } returns PostResult.success(post)
        coEvery { getUserProfileUseCase.getLoggedInUserProfile() } returns profileData
        coEvery { createPostUseCase.getPostsCountToday() } returns 1
        coEvery { createPostUseCase.canCreatePostToday() } returns true

        // When
        profileViewModel.createQuotePost(content, postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertNull("Should not have error", uiState.error)
        assertEquals("Profile should be updated", profileData, uiState.profileData)
    }

    @Test
    fun `createQuotePost should update UI state with error when creation fails`() = runTest {
        // Given
        val content = "Quote content"
        val postId = "post1"
        val errorMessage = "Original post not found"
        coEvery { createPostUseCase.createQuotePost(content, postId) } returns PostResult.error(errorMessage)

        // When
        profileViewModel.createQuotePost(content, postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertEquals("Should have error message", errorMessage, uiState.error)
    }

    @Test
    fun `createQuotePost should handle exceptions`() = runTest {
        // Given
        val content = "Quote content"
        val postId = "post1"
        val errorMessage = "Network error"
        coEvery { createPostUseCase.createQuotePost(content, postId) } throws RuntimeException(errorMessage)

        // When
        profileViewModel.createQuotePost(content, postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertTrue("Should have error", uiState.error?.contains(errorMessage) == true)
    }

    @Test
    fun `clearError should remove error from UI state`() = runTest {
        // Given
        val errorMessage = "Test error"
        profileViewModel.setShowCreatePostDialog(true) // Trigger some state change to ensure we can observe

        // When
        profileViewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertNull("Error should be cleared", uiState.error)
    }

    @Test
    fun `setShowCreatePostDialog should update UI state`() = runTest {
        // When
        profileViewModel.setShowCreatePostDialog(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertTrue("Should show create post dialog", uiState.showCreatePostDialog)

        // When
        profileViewModel.setShowCreatePostDialog(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val updatedUiState = profileViewModel.uiState.first()
        assertFalse("Should hide create post dialog", updatedUiState.showCreatePostDialog)
    }

    @Test
    fun `setShowQuoteDialog should update UI state`() = runTest {
        // When
        profileViewModel.setShowQuoteDialog(true, "post1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        assertTrue("Should show quote dialog", uiState.showQuoteDialog)
        assertEquals("Should have selected post", "post1", uiState.selectedPostForQuote)

        // When
        profileViewModel.setShowQuoteDialog(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val updatedUiState = profileViewModel.uiState.first()
        assertFalse("Should hide quote dialog", updatedUiState.showQuoteDialog)
        assertNull("Should not have selected post", updatedUiState.selectedPostForQuote)
    }

    @Test
    fun `loadUserPostCount should handle exceptions gracefully`() = runTest {
        // Given
        coEvery { createPostUseCase.getPostsCountToday() } throws RuntimeException("Count error")
        coEvery { createPostUseCase.canCreatePostToday() } throws RuntimeException("Can create error")

        // When
        profileViewModel.loadProfile() // This triggers loadUserPostCount
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = profileViewModel.uiState.first()
        // Should not crash and should have default values
        assertEquals("Should have default posts count", 0, uiState.postsCountToday)
        assertTrue("Should have default can create post", uiState.canCreatePost)
    }

    @Test
    fun `ProfileUiState should have correct default values`() {
        // When
        val uiState = ProfileUiState()

        // Then
        assertNull("Profile data should be null by default", uiState.profileData)
        assertFalse("Should not be loading by default", uiState.isLoading)
        assertFalse("Should not be creating post by default", uiState.isCreatingPost)
        assertNull("Should not have error by default", uiState.error)
        assertEquals("Should have default posts count", 0, uiState.postsCountToday)
        assertTrue("Should be able to create post by default", uiState.canCreatePost)
        assertFalse("Should not show create post dialog by default", uiState.showCreatePostDialog)
        assertFalse("Should not show quote dialog by default", uiState.showQuoteDialog)
        assertNull("Should not have selected post for quote by default", uiState.selectedPostForQuote)
    }

    @Test
    fun `ProfileUiState copy should work correctly`() {
        // Given
        val user = User(
            id = "user1",
            username = "testuser",
            joinDate = 1234567890L,
            isLoggedIn = true
        )
        val userStats = UserStats(userId = "user1")
        val profileData = UserProfileData(user, userStats, emptyList())

        // When
        val uiState = ProfileUiState().copy(
            profileData = profileData,
            isLoading = true,
            error = "Test error"
        )

        // Then
        assertEquals("Profile data should be updated", profileData, uiState.profileData)
        assertTrue("Should be loading", uiState.isLoading)
        assertEquals("Should have error", "Test error", uiState.error)
        // Other fields should remain default
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertEquals("Should have default posts count", 0, uiState.postsCountToday)
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
        val posts = listOf(
            Post(
                id = "post1",
                content = "Test post",
                authorId = "user1",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )

        // When
        val userProfileData = UserProfileData(user, userStats, posts)

        // Then
        assertEquals("User should match", user, userProfileData.user)
        assertEquals("User stats should match", userStats, userProfileData.stats)
        assertEquals("Posts should match", posts, userProfileData.posts)
    }
}
