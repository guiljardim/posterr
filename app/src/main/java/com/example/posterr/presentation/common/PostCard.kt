package com.example.posterr.presentation.common

import androidx.compose.ui.res.stringResource
import com.example.posterr.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.posterr.domain.model.Post
import com.example.posterr.domain.model.PostType
import com.example.posterr.presentation.common.extensions.toPostDate

@Composable
fun PostCard(
    post: Post,
    onRepost: () -> Unit,
    onQuote: () -> Unit,
    authorUsername: String? = null,
    modifier: Modifier = Modifier
) {
    PostCardContent(
        post = post,
        onRepost = onRepost,
        onQuote = onQuote,
        authorUsername = authorUsername,
        modifier = modifier
    )
}

@Composable
fun PostCardContent(
    post: Post,
    onRepost: () -> Unit,
    onQuote: () -> Unit,
    authorUsername: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "@${authorUsername ?: post.authorId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = post.createdAt.toPostDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PostTypeBadge(post.type)

            Spacer(modifier = Modifier.height(8.dp))

            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (post.type != PostType.ORIGINAL && post.originalPost != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.original_post_from, authorUsername ?: post.originalPost.authorId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (post.originalPost.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = post.originalPost.content,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(
                    onClick = onRepost,
                    enabled = post.type == PostType.ORIGINAL
                ) {
                    Text(stringResource(id = R.string.repost))
                }

                TextButton(
                    onClick = onQuote
                ) {
                    Text(stringResource(id = R.string.quote))
                }
            }
        }
    }
}

@Composable
fun PostTypeBadge(postType: PostType) {
    val (text, color) = when (postType) {
        PostType.ORIGINAL -> stringResource(id = R.string.badge_original) to MaterialTheme.colorScheme.primary
        PostType.REPOST -> stringResource(id = R.string.badge_repost) to MaterialTheme.colorScheme.secondary
        PostType.QUOTE -> stringResource(id = R.string.badge_quote) to MaterialTheme.colorScheme.tertiary
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}