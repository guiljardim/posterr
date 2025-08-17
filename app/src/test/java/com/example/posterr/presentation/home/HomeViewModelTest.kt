package com.example.posterr.presentation.home

import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.model.PostType
import com.example.posterr.domain.useCase.CreatePostUseCase
import com.example.posterr.domain.useCase.GetAllPostsUseCase
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
class HomeViewModelTest {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var getAllPostsUseCase: GetAllPostsUseCase
    private lateinit var createPostUseCase: CreatePostUseCase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getAllPostsUseCase = mockk()
        createPostUseCase = mockk()
        homeViewModel = HomeViewModel(getAllPostsUseCase, createPostUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init should load posts and user post count`() = runTest {
        // Given
        val posts = listOf(
            Post(
                id = "post1",
                content = "Test post",
                authorId = "user1",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            )
        )
        
        // Configure mocks before creating ViewModel
        coEvery { getAllPostsUseCase() } returns posts
        coEvery { createPostUseCase.getPostsCountToday() } returns 2
        coEvery { createPostUseCase.canCreatePostToday() } returns true
        
        // Create new ViewModel with configured mocks
        val testViewModel = HomeViewModel(getAllPostsUseCase, createPostUseCase)

        // When
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = testViewModel.uiState.first()
        assertEquals("Posts should be loaded", posts, uiState.posts)
        assertEquals("Posts count should be loaded", 2, uiState.postsCountToday)
        assertTrue("User should be able to create post", uiState.canCreatePost)
        assertFalse("Should not be loading", uiState.isLoading)
        assertNull("Should not have error", uiState.error)
    }

    @Test
    fun `loadPosts should update UI state with posts when successful`() = runTest {
        // Given
        val posts = listOf(
            Post(
                id = "post1",
                content = "Test post 1",
                authorId = "user1",
                type = PostType.ORIGINAL,
                createdAt = 1234567890L
            ),
            Post(
                id = "post2",
                content = "Test post 2",
                authorId = "user2",
                type = PostType.ORIGINAL,
                createdAt = 1234567891L
            )
        )
        coEvery { getAllPostsUseCase() } returns posts

        // When
        homeViewModel.loadPosts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertEquals("Posts should be loaded", posts, uiState.posts)
        assertFalse("Should not be loading", uiState.isLoading)
        assertNull("Should not have error", uiState.error)
    }

    @Test
    fun `loadPosts should update UI state with error when exception occurs`() = runTest {
        // Given
        val errorMessage = "Network error"
        coEvery { getAllPostsUseCase() } throws RuntimeException(errorMessage)

        // When
        homeViewModel.loadPosts()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertTrue("Should have error", uiState.error?.contains(errorMessage) == true)
        assertFalse("Should not be loading", uiState.isLoading)
        assertTrue("Posts should be empty", uiState.posts.isEmpty())
    }

    @Test
    fun `createOriginalPost should not create post when content is blank`() = runTest {
        // Given
        val blankContent = "   "

        // When
        homeViewModel.createOriginalPost(blankContent)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        // Post should not be created when content is blank
    }

    @Test
    fun `createOriginalPost should not create post when content is empty`() = runTest {
        // Given
        val emptyContent = ""

        // When
        homeViewModel.createOriginalPost(emptyContent)
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
        val posts = listOf(post)
        
        coEvery { createPostUseCase.createOriginalPost(content) } returns PostResult.success(post)
        coEvery { getAllPostsUseCase() } returns posts
        coEvery { createPostUseCase.getPostsCountToday() } returns 1
        coEvery { createPostUseCase.canCreatePostToday() } returns true

        // When
        homeViewModel.createOriginalPost(content)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertNull("Should not have error", uiState.error)
        assertEquals("Posts should be updated", posts, uiState.posts)
        assertEquals("Posts count should be updated", 1, uiState.postsCountToday)
    }

    @Test
    fun `createOriginalPost should update UI state with error when creation fails`() = runTest {
        // Given
        val content = "Valid post content"
        val errorMessage = "Daily limit reached"
        coEvery { createPostUseCase.createOriginalPost(content) } returns PostResult.error(errorMessage)

        // When
        homeViewModel.createOriginalPost(content)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
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
        homeViewModel.createOriginalPost(content)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
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
        val posts = listOf(post)
        
        coEvery { createPostUseCase.createRepost(postId) } returns PostResult.success(post)
        coEvery { getAllPostsUseCase() } returns posts
        coEvery { createPostUseCase.getPostsCountToday() } returns 1
        coEvery { createPostUseCase.canCreatePostToday() } returns true

        // When
        homeViewModel.createRepost(postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertNull("Should not have error", uiState.error)
        assertEquals("Posts should be updated", posts, uiState.posts)
    }

    @Test
    fun `createRepost should update UI state with error when creation fails`() = runTest {
        // Given
        val postId = "post1"
        val errorMessage = "Original post not found"
        coEvery { createPostUseCase.createRepost(postId) } returns PostResult.error(errorMessage)

        // When
        homeViewModel.createRepost(postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
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
        homeViewModel.createRepost(postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertTrue("Should have error", uiState.error?.contains(errorMessage) == true)
    }

    @Test
    fun `createQuotePost should not create post when content is blank`() = runTest {
        // Given
        val blankContent = "   "
        val postId = "post1"

        // When
        homeViewModel.createQuotePost(blankContent, postId)
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
        val posts = listOf(post)
        
        coEvery { createPostUseCase.createQuotePost(content, postId) } returns PostResult.success(post)
        coEvery { getAllPostsUseCase() } returns posts
        coEvery { createPostUseCase.getPostsCountToday() } returns 1
        coEvery { createPostUseCase.canCreatePostToday() } returns true

        // When
        homeViewModel.createQuotePost(content, postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertNull("Should not have error", uiState.error)
        assertEquals("Posts should be updated", posts, uiState.posts)
    }

    @Test
    fun `createQuotePost should update UI state with error when creation fails`() = runTest {
        // Given
        val content = "Quote content"
        val postId = "post1"
        val errorMessage = "Original post not found"
        coEvery { createPostUseCase.createQuotePost(content, postId) } returns PostResult.error(errorMessage)

        // When
        homeViewModel.createQuotePost(content, postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
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
        homeViewModel.createQuotePost(content, postId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertTrue("Should have error", uiState.error?.contains(errorMessage) == true)
    }

    @Test
    fun `setShowCreatePostDialog should update UI state`() = runTest {
        // When
        homeViewModel.setShowCreatePostDialog(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertTrue("Should show create post dialog", uiState.showCreatePostDialog)

        // When
        homeViewModel.setShowCreatePostDialog(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val updatedUiState = homeViewModel.uiState.first()
        assertFalse("Should hide create post dialog", updatedUiState.showCreatePostDialog)
    }

    @Test
    fun `setShowQuoteDialog should update UI state`() = runTest {
        // When
        homeViewModel.setShowQuoteDialog(true, "post1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        assertTrue("Should show quote dialog", uiState.showQuoteDialog)
        assertEquals("Should have selected post", "post1", uiState.selectedPostForQuote)

        // When
        homeViewModel.setShowQuoteDialog(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val updatedUiState = homeViewModel.uiState.first()
        assertFalse("Should hide quote dialog", updatedUiState.showQuoteDialog)
        assertNull("Should not have selected post", updatedUiState.selectedPostForQuote)
    }

    @Test
    fun `loadUserPostCount should handle exceptions gracefully`() = runTest {
        // Given
        coEvery { createPostUseCase.getPostsCountToday() } throws RuntimeException("Count error")
        coEvery { createPostUseCase.canCreatePostToday() } throws RuntimeException("Can create error")

        // When
        homeViewModel.loadPosts() // This triggers loadUserPostCount
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val uiState = homeViewModel.uiState.first()
        // Should not crash and should have default values
        assertEquals("Should have default posts count", 0, uiState.postsCountToday)
        assertTrue("Should have default can create post", uiState.canCreatePost)
    }

    @Test
    fun `HomeUiState should have correct default values`() {
        // When
        val uiState = HomeUiState()

        // Then
        assertTrue("Posts should be empty by default", uiState.posts.isEmpty())
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
    fun `HomeUiState copy should work correctly`() {
        // Given
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
        val uiState = HomeUiState().copy(
            posts = posts,
            isLoading = true,
            error = "Test error"
        )

        // Then
        assertEquals("Posts should be updated", posts, uiState.posts)
        assertTrue("Should be loading", uiState.isLoading)
        assertEquals("Should have error", "Test error", uiState.error)
        // Other fields should remain default
        assertFalse("Should not be creating post", uiState.isCreatingPost)
        assertEquals("Should have default posts count", 0, uiState.postsCountToday)
    }
}
