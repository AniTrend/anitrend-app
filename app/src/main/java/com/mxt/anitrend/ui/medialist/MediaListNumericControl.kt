package com.mxt.anitrend.ui.medialist

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.ui.designsystem.AniTrendTheme
import com.mxt.anitrend.ui.designsystem.aniTrendCondensedFontFamily
import com.mxt.anitrend.ui.designsystem.aniTrendPlatformTextStyle

@Composable
internal fun MediaListNumericControl(
    value: TextFieldValue,
    maximumLabel: String?,
    keyboardType: KeyboardType,
    onValueChange: (TextFieldValue) -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val controlTextStyle =
        MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = aniTrendCondensedFontFamily,
            platformStyle = aniTrendPlatformTextStyle,
            textAlign = TextAlign.Center,
        )
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            contentDescription = stringResource(R.string.action_decrement),
            iconResource = R.drawable.ic_stepper_remove_24dp,
            onClick = onDecrement,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .widthIn(min = 72.dp)
                .height(45.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                .padding(horizontal = 12.dp),
            singleLine = true,
            textStyle = controlTextStyle,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    innerTextField()
                }
            },
        )
        if (maximumLabel != null) {
            Text(
                text = maximumLabel,
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.lg_margin)),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = aniTrendCondensedFontFamily,
                    platformStyle = aniTrendPlatformTextStyle,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                softWrap = false,
            )
        }
        StepperButton(
            contentDescription = stringResource(R.string.action_increment),
            iconResource = R.drawable.ic_stepper_add_24dp,
            onClick = onIncrement,
        )
    }
}

@Composable
private fun StepperButton(
    contentDescription: String,
    iconResource: Int,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
    ) {
        Icon(
            painter = painterResource(iconResource),
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview
@Composable
private fun MediaListNumericControlPreview() {
    AniTrendTheme {
        MediaListNumericControl(
            value = TextFieldValue("5"),
            maximumLabel = "/ 12",
            keyboardType = KeyboardType.Number,
            onValueChange = {},
            onDecrement = {},
            onIncrement = {},
        )
    }
}
