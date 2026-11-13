package com.mxt.anitrend.ui.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mxt.anitrend.R
import kotlin.math.roundToInt

/** Loads nonblank Compose-owned image models through Coil with the app's shared error drawable. */
@Composable
internal fun AniTrendImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    crossfadeDurationMillis: Int = 350,
    errorResource: Int? = R.drawable.ic_emoji_sweat,
    painterContentShape: Shape? = null,
) {
    val imageModel =
        when (model) {
            is String -> model.takeIf(String::isNotBlank)
            else -> model
        }

    if (imageModel != null) {
        val context = LocalContext.current
        val request =
            remember(context, imageModel, crossfadeDurationMillis) {
                ImageRequest.Builder(context)
                    .data(imageModel)
                    .crossfade(crossfadeDurationMillis)
                    .build()
            }
        val errorPainter = errorResource?.let { painterResource(it) }
        val contentShape = painterContentShape

        if (contentShape == null) {
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                error = errorPainter,
            )
        } else {
            val contentScaleState = rememberUpdatedState(contentScale)
            val sourceSize = remember(imageModel) { mutableStateOf(Size.Unspecified) }
            val updateSourceSize: (Painter?) -> Unit =
                remember(sourceSize) {
                    { painter -> sourceSize.value = painter?.intrinsicSize ?: Size.Unspecified }
                }
            val onLoading: (AsyncImagePainter.State.Loading) -> Unit =
                remember(updateSourceSize) {
                    { state -> updateSourceSize(state.painter) }
                }
            val onSuccess: (AsyncImagePainter.State.Success) -> Unit =
                remember(updateSourceSize) {
                    { state -> updateSourceSize(state.painter) }
                }
            val onError: (AsyncImagePainter.State.Error) -> Unit =
                remember(updateSourceSize) {
                    { state -> updateSourceSize(state.painter) }
                }
            // Inside can letterbox the painter within AsyncImage's full-size layout.
            val clippedModifier =
                modifier.drawWithCache {
                    val imageSize = sourceSize.value
                    if (!imageSize.isSpecified || imageSize.isEmpty() || size.isEmpty()) {
                        onDrawWithContent { drawContent() }
                    } else {
                        val scaleFactor = contentScaleState.value.computeScaleFactor(imageSize, size)
                        val scaledSize =
                            Size(
                                imageSize.width * scaleFactor.scaleX,
                                imageSize.height * scaleFactor.scaleY,
                            )
                        val imageOffset =
                            Alignment.Center.align(
                                size = IntSize(scaledSize.width.roundToInt(), scaledSize.height.roundToInt()),
                                space = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                                layoutDirection = layoutDirection,
                            )
                        val outline = contentShape.createOutline(scaledSize, layoutDirection, this)
                        val path =
                            Path().apply {
                                when (outline) {
                                    is Outline.Rectangle -> addRect(Rect(Offset.Zero, scaledSize))
                                    is Outline.Rounded -> addRoundRect(outline.roundRect)
                                    is Outline.Generic -> addPath(outline.path)
                                }
                                translate(Offset(imageOffset.x.toFloat(), imageOffset.y.toFloat()))
                            }
                        onDrawWithContent {
                            val contentDrawScope = this
                            clipPath(path) {
                                contentDrawScope.drawContent()
                            }
                        }
                    }
                }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = clippedModifier,
                contentScale = contentScale,
                error = errorPainter,
                onLoading = onLoading,
                onSuccess = onSuccess,
                onError = onError,
                alignment = Alignment.Center,
            )
        }
    }
}

/** Displays an avatar with the existing placeholder, error drawable, and circular crop. */
@Composable
internal fun AniTrendAvatarImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val avatarModel = imageUrl?.takeIf(String::isNotBlank)
    val avatarModifier =
        modifier
            .clip(CircleShape)
            .background(colorResource(R.color.colorPrimaryDark), CircleShape)

    Box(
        modifier =
        avatarModifier.semantics(mergeDescendants = true) {
            if (contentDescription != null) this.contentDescription = contentDescription
        },
    ) {
        if (avatarModel == null) {
            Image(
                painter = painterResource(R.drawable.ic_emoji_sweat),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (!LocalInspectionMode.current) {
            AniTrendImage(
                model = avatarModel,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                crossfadeDurationMillis = 150,
            )
        }
    }
}
