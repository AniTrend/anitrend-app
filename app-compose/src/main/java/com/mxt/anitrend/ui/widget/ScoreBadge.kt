package com.mxt.anitrend.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScoreBadge(
    score: Int?,
    modifier: Modifier = Modifier,
) {
    if (score == null) return
    val (bgColor, textColor) = when {
        score >= 75 -> Color(0xFF4CAF50) to Color.White
        score >= 60 -> Color(0xFFFFC107) to Color.Black
        score >= 40 -> Color(0xFFFF9800) to Color.White
        else -> Color(0xFFF44336) to Color.White
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = "${score}%",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
