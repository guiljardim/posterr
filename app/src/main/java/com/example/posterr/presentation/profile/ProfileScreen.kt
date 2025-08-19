package com.example.posterr.presentation.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.posterr.presentation.common.CreatePostDialog
import com.example.posterr.presentation.common.PostCard
import com.example.posterr.presentation.common.QuotePostDialog
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import com.example.posterr.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_profile)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else if (uiState.profileData?.posts?.isEmpty() == true) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                uiState.profileData?.let { profileData ->
                    ProfileHeader(
                        user = profileData.user,
                        stats = profileData.stats,
                        postsCountToday = uiState.postsCountToday,
                        canCreatePost = uiState.canCreatePost,
                        onNavigateBack = onNavigateBack,
                        onCreatePost = { viewModel.setShowCreatePostDialog(true) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(profileData.posts) { post ->
                            PostCard(
                                post = post,
                                onRepost = { viewModel.createRepost(post.id) },
                                onQuote = { viewModel.setShowQuoteDialog(true, post.id) }
                            )
                        }
                    }
                }
            }

            if (uiState.showCreatePostDialog) {
                CreatePostDialog(
                    onDismiss = { viewModel.setShowCreatePostDialog(false) },
                    onConfirm = { content ->
                        viewModel.createOriginalPost(content)
                        viewModel.setShowCreatePostDialog(false)
                    },
                    postsCountToday = uiState.postsCountToday,
                    canCreatePost = uiState.canCreatePost
                )
            }

            if (uiState.showQuoteDialog) {
                QuotePostDialog(
                    onDismiss = { viewModel.setShowQuoteDialog(false, null) },
                    onConfirm = { content ->
                        uiState.selectedPostForQuote?.let { postId ->
                            viewModel.createQuotePost(content, postId)
                        }
                        viewModel.setShowQuoteDialog(false, null)
                    },
                    postsCountToday = uiState.postsCountToday,
                    canCreatePost = uiState.canCreatePost
                )
            }
        }
    }
}