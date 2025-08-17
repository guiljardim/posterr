package com.example.posterr.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun QuotePostDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    postsCountToday: Int,
    canCreatePost: Boolean
) {
    var content by remember { mutableStateOf("") }
    val remainingChars = 777 - content.length

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Quote") },
        text = {
            Column {
                if (!canCreatePost) {
                    Text(
                        text = "Daily limit of 5 posts reached!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = "Posts today: $postsCountToday/5",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 777) content = it },
                    label = { Text("Add your comment") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    enabled = canCreatePost
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$remainingChars characters remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (remainingChars < 50) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (remainingChars < 0) {
                        Text(
                            text = "Maximum 777 characters!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(content) },
                enabled = content.isNotBlank() && canCreatePost && remainingChars >= 0
            ) {
                Text("Quote")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}