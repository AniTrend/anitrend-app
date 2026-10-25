/*
 * Copyright (C) 2026 AniTrend
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mxt.anitrend.ui.designsystem

import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.Settings

/*
 * Compose color schemes are the authoritative brand tokens. The values mirror
 * the light/dark/AMOLED palettes in values/m3_color.xml so Compose surfaces and
 * remaining XML surfaces stay visually aligned without reading live theme
 * attributes at composition time.
 */
private val aniTrendLightColorScheme: ColorScheme =
    lightColorScheme(
        primary = Color(0xFF0A5CAA),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD6E4FF),
        onPrimaryContainer = Color(0xFF001B3E),
        inversePrimary = Color(0xFFAAC7FF),
        secondary = Color(0xFF555F71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD9E3F8),
        onSecondaryContainer = Color(0xFF121C2B),
        tertiary = Color(0xFF835345),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDBCE),
        onTertiaryContainer = Color(0xFF331107),
        background = Color(0xFFF9F8FC),
        onBackground = Color(0xFF1B1B1F),
        surface = Color(0xFFF9F8FC),
        surfaceDim = Color(0xFFD9D8DD),
        surfaceBright = Color(0xFFF9F8FC),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF3F2F6),
        surfaceContainer = Color(0xFFEDECF1),
        surfaceContainerHigh = Color(0xFFE8E6EB),
        surfaceContainerHighest = Color(0xFFE2E1E6),
        onSurface = Color(0xFF1B1B1F),
        surfaceVariant = Color(0xFFE4E1E6),
        onSurfaceVariant = Color(0xFF49454F),
        inverseSurface = Color(0xFF313033),
        inverseOnSurface = Color(0xFFF4EFF4),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFCAC4D0),
    )

private val aniTrendDarkColorScheme: ColorScheme =
    darkColorScheme(
        primary = Color(0xFFAAC7FF),
        onPrimary = Color(0xFF002F65),
        primaryContainer = Color(0xFF00458E),
        onPrimaryContainer = Color(0xFFD6E4FF),
        inversePrimary = Color(0xFF0A5CAA),
        secondary = Color(0xFFBDC7DC),
        onSecondary = Color(0xFF273141),
        secondaryContainer = Color(0xFF3D4758),
        onSecondaryContainer = Color(0xFFD9E3F8),
        tertiary = Color(0xFFF4B9A7),
        onTertiary = Color(0xFF4E281C),
        tertiaryContainer = Color(0xFF673C2F),
        onTertiaryContainer = Color(0xFFFFDBCE),
        background = Color(0xFF131318),
        onBackground = Color(0xFFE4E1E6),
        surface = Color(0xFF131318),
        surfaceDim = Color(0xFF131318),
        surfaceBright = Color(0xFF39393E),
        surfaceContainerLowest = Color(0xFF0E0E13),
        surfaceContainerLow = Color(0xFF1B1B1F),
        surfaceContainer = Color(0xFF1F1F23),
        surfaceContainerHigh = Color(0xFF2A2A2F),
        surfaceContainerHighest = Color(0xFF353539),
        onSurface = Color(0xFFE4E1E6),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        inverseSurface = Color(0xFFE4E1E6),
        inverseOnSurface = Color(0xFF313033),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        outline = Color(0xFF938F99),
        outlineVariant = Color(0xFF49454F),
    )

/** AMOLED black: the dark roles with every surface/bottom layer forced black. */
private val aniTrendBlackColorScheme: ColorScheme =
    aniTrendDarkColorScheme.copy(
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        surfaceDim = Color(0xFF000000),
        surfaceBright = Color(0xFF000000),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF000000),
        surfaceContainer = Color(0xFF000000),
        surfaceContainerHigh = Color(0xFF000000),
        surfaceContainerHighest = Color(0xFF000000),
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
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(16.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

/**
 * Resolves the persisted application theme (`light`, `dark` or `black`) for
 * legacy View hosts that embed Compose surfaces before the Navigation 3 host
 * cutover. Compose roots pass the observed Settings theme directly instead.
 */
internal val Context.aniTrendThemeName: String
    get() = Settings(this).theme

/**
 * Observed Settings theme for legacy View hosts embedding Compose surfaces.
 * Compose roots pass the theme value directly to [AniTrendTheme].
 */
@Composable
internal fun currentAniTrendTheme(): String = LocalContext.current.aniTrendThemeName

/**
 * The application theme wrapper. [theme] is the persisted Settings value:
 * [KeyUtil.THEME_LIGHT], [KeyUtil.THEME_DARK] or [KeyUtil.THEME_BLACK].
 */
@Composable
internal fun AniTrendTheme(theme: String, content: @Composable () -> Unit) {
    val colorScheme =
        when (theme) {
            KeyUtil.THEME_DARK -> aniTrendDarkColorScheme
            KeyUtil.THEME_BLACK -> aniTrendBlackColorScheme
            else -> aniTrendLightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = aniTrendTypography,
        shapes = aniTrendShapes,
        content = content,
    )
}
