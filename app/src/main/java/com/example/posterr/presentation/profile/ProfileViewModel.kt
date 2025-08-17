package com.example.posterr.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.posterr.domain.model.PostResult
import com.example.posterr.domain.useCase.CreatePostUseCase
import com.example.posterr.domain.useCase.GetUserProfileUseCase
import com.example.posterr.domain.useCase.UserProfileData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val createPostUseCase: CreatePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserPostCount()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val profileData = getUserProfileUseCase.getLoggedInUserProfile()
                if (profileData != null) {
                    _uiState.value = _uiState.value.copy(
                        profileData = profileData,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Profile not found",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Error loading profile",
                    isLoading = false
                )
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
                        loadProfile()
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
                        loadProfile()
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
                        loadProfile()
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
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

data class ProfileUiState(
    val profileData: UserProfileData? = null,
    val isLoading: Boolean = false,
    val isCreatingPost: Boolean = false,
    val error: String? = null,
    val postsCountToday: Int = 0,
    val canCreatePost: Boolean = true,
    val showCreatePostDialog: Boolean = false,
    val showQuoteDialog: Boolean = false,
    val selectedPostForQuote: String? = null
)