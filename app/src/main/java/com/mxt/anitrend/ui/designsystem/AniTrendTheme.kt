package com.mxt.anitrend.ui.designsystem

import android.content.Context
import android.content.res.TypedArray
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R

private val aniTrendColorAttributes =
    intArrayOf(
        R.attr.colorPrimary,
        R.attr.colorSecondaryContainer,
        R.attr.colorOnSecondaryContainer,
        R.attr.colorSurface,
        R.attr.colorOnSurface,
        R.attr.colorOnSurfaceVariant,
        R.attr.colorOutlineVariant,
        R.attr.colorSurfaceContainer,
    )

internal val aniTrendCondensedFontFamily =
    FontFamily(
        Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Normal),
        Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Medium),
        Font(DeviceFontFamilyName("sans-serif"), FontWeight.Normal),
        Font(DeviceFontFamilyName("sans-serif"), FontWeight.Medium),
    )
internal val aniTrendCondensedBoldFontFamily =
    FontFamily(
        Font(DeviceFontFamilyName("sans-serif-condensed"), FontWeight.Bold),
    )

internal val aniTrendPlatformTextStyle = PlatformTextStyle(includeFontPadding = true)

private val defaultTypography = Typography()
private val aniTrendTypography =
    defaultTypography.copy(
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = aniTrendCondensedFontFamily),
        titleMedium =
        defaultTypography.titleMedium.copy(
            fontFamily = aniTrendCondensedFontFamily,
            platformStyle = aniTrendPlatformTextStyle,
        ),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = aniTrendCondensedFontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = aniTrendCondensedFontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = aniTrendCondensedFontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = aniTrendCondensedFontFamily),
        labelMedium =
        defaultTypography.labelMedium.copy(
            fontFamily = aniTrendCondensedFontFamily,
            platformStyle = aniTrendPlatformTextStyle,
        ),
    )

private val aniTrendShapes =
    Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
    )

/**
 * Provides Compose with the currently selected XML theme roles during incremental adoption.
 * Resolving the active theme keeps the existing light, dark, and black schemes as the source of truth.
 */
@Composable
internal fun AniTrendTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val colorScheme = remember(context, configuration) { context.aniTrendColorScheme() }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = aniTrendTypography,
        shapes = aniTrendShapes,
        content = content,
    )
}

private fun Context.aniTrendColorScheme(): ColorScheme {
    val themeColors = obtainStyledAttributes(aniTrendColorAttributes)
    return try {
        for (index in aniTrendColorAttributes.indices) {
            check(themeColors.hasValue(index)) {
                "AniTrend XML theme is missing required Compose color attribute $index"
            }
        }

        val primary = themeColors.colorAt(0)
        val secondaryContainer = themeColors.colorAt(1)
        val onSecondaryContainer = themeColors.colorAt(2)
        val surface = themeColors.colorAt(3)
        val onSurface = themeColors.colorAt(4)
        val onSurfaceVariant = themeColors.colorAt(5)
        val outlineVariant = themeColors.colorAt(6)
        val surfaceContainer = themeColors.colorAt(7)

        if (surface.luminance() > 0.5f) {
            lightColorScheme(
                primary = primary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                surface = surface,
                onSurface = onSurface,
                onSurfaceVariant = onSurfaceVariant,
                outlineVariant = outlineVariant,
                surfaceContainer = surfaceContainer,
            )
        } else {
            darkColorScheme(
                primary = primary,
                secondaryContainer = secondaryContainer,
                onSecondaryContainer = onSecondaryContainer,
                surface = surface,
                onSurface = onSurface,
                onSurfaceVariant = onSurfaceVariant,
                outlineVariant = outlineVariant,
                surfaceContainer = surfaceContainer,
            )
        }
    } finally {
        themeColors.recycle()
    }
}

private fun TypedArray.colorAt(index: Int): Color = Color(getColor(index, 0))
