package com.example.posterr.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.title_posterr)) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.Person, contentDescription = stringResource(id = R.string.cd_profile))
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.canCreatePost) {
                FloatingActionButton(
                    onClick = { viewModel.setShowCreatePostDialog(true) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.cd_create_post))
                }
            }
        }
    ) { paddingValues ->
        Box(
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
            } else if (uiState.posts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.home_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column {
                    if (uiState.postsCountToday > 0) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(id = R.string.posts_today, uiState.postsCountToday),
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.posts) { post ->
                            PostCard(
                                post = post,
                                onRepost = { viewModel.createRepost(post.id) },
                                onQuote = { viewModel.setShowQuoteDialog(true, post.id) }
                            )
                        }
                    }
                }
            }

            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    
                }
            }

            if (uiState.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    Text(
                        text = stringResource(id = R.string.error_prefix, uiState.error!!),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
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