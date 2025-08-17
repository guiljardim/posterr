package com.example.posterr.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.useCase.CreatePostUseCase
import com.example.posterr.domain.useCase.GetAllPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val createPostUseCase: CreatePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
        loadUserPostCount()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val posts = getAllPostsUseCase()
                _uiState.value = _uiState.value.copy(
                    posts = posts,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading posts: ${e.toString()}",
                    isLoading = false
                )
                e.printStackTrace()
            }
        }
    }

    fun createOriginalPost(content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingPost = true)
            try {
                val result = createPostUseCase.createOriginalPost(content)
                when (result) {
                    is PostResult.Success -> {
                        loadPosts()
                        loadUserPostCount()
                        _uiState.value = _uiState.value.copy(
                            isCreatingPost = false,
                            error = null
                        )
                    }

                    is PostResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message,
                            isCreatingPost = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error creating post",
                    isCreatingPost = false
                )
            }
        }
    }

    fun createRepost(postId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingPost = true)
            try {
                val result = createPostUseCase.createRepost(postId)
                when (result) {
                    is PostResult.Success -> {
                        loadPosts()
                        loadUserPostCount()
                        _uiState.value = _uiState.value.copy(
                            isCreatingPost = false,
                            error = null
                        )
                    }

                    is PostResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message,
                            isCreatingPost = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error creating repost",
                    isCreatingPost = false
                )
            }
        }
    }

    fun createQuotePost(content: String, postId: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingPost = true)
            try {
                val result = createPostUseCase.createQuotePost(content, postId)
                when (result) {
                    is PostResult.Success -> {
                        loadPosts()
                        loadUserPostCount()
                        _uiState.value = _uiState.value.copy(
                            isCreatingPost = false,
                            error = null
                        )
                    }

                    is PostResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            error = result.message,
                            isCreatingPost = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error creating quote",
                    isCreatingPost = false
                )
            }
        }
    }

    private fun loadUserPostCount() {
        viewModelScope.launch {
            try {
                val postsCount = createPostUseCase.getPostsCountToday()
                val canCreate = createPostUseCase.canCreatePostToday()
                _uiState.value = _uiState.value.copy(
                    postsCountToday = postsCount,
                    canCreatePost = canCreate
                )
            } catch (e: Exception) {

            }
        }
    }


    fun setShowCreatePostDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreatePostDialog = show)
    }

    fun setShowQuoteDialog(show: Boolean, postId: String? = null) {
        _uiState.value = _uiState.value.copy(
            showQuoteDialog = show,
            selectedPostForQuote = postId
        )
    }
}

data class HomeUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val isCreatingPost: Boolean = false,
    val error: String? = null,
    val postsCountToday: Int = 0,
    val canCreatePost: Boolean = true,
    val showCreatePostDialog: Boolean = false,
    val showQuoteDialog: Boolean = false,
    val selectedPostForQuote: String? = null
)