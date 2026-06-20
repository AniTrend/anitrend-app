package com.mxt.anitrend.ui.widget

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FollowButton(
    isFollowing: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isFollowing) {
        OutlinedButton(onClick = onToggle, modifier = modifier) {
            Text("Following")
        }
    } else {
        Button(onClick = onToggle, modifier = modifier) {
            Text("Follow")
        }
    }
}
