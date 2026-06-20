package com.mxt.anitrend.ui.widget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun FavouriteButton(
    isFavourite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var animate by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animate) 1.3f else 1f,
        animationSpec = tween(200),
        label = "favourite_scale",
    )

    IconButton(
        onClick = {
            animate = true
            onToggle()
        },
        modifier = modifier,
    ) {
        Icon(
            imageVector = if (isFavourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (isFavourite) "Remove from favourites" else "Add to favourites",
            tint = if (isFavourite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp).scale(scale),
        )
    }
}
