package com.mxt.anitrend.ui.composer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gif
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposerSheet(
    onDismiss: () -> Unit,
    onPost: (String) -> Unit,
    onNavigateToGiphy: () -> Unit = {},
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Post") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = {
                    textFieldValue = wrapWithFormat(textFieldValue, "**", "**")
                }) { Text("B", fontWeight = FontWeight.Bold) }
                TextButton(onClick = {
                    textFieldValue = wrapWithFormat(textFieldValue, "*", "*")
                }) { Text("I", fontStyle = FontStyle.Italic) }
                TextButton(onClick = {
                    textFieldValue = wrapWithFormat(textFieldValue, "||", "||")
                }) { Text("S", color = MaterialTheme.colorScheme.error) }
            }
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                placeholder = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 8,
                textStyle = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onNavigateToGiphy) {
                    Icon(Icons.Filled.Gif, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("GIF")
                }
                Spacer(modifier = Modifier.weight(1f))
                FilledTonalButton(
                    onClick = { onPost(textFieldValue.text) },
                    enabled = textFieldValue.text.isNotBlank(),
                ) {
                    Text("Post")
                }
            }
        }
    }
}

private fun wrapWithFormat(
    value: TextFieldValue,
    prefix: String,
    suffix: String,
): TextFieldValue {
    val sel = value.selection
    if (sel.length > 0) {
        val inner = value.text.substring(sel.min, sel.max)
        val withFmt = "$prefix$inner$suffix"
        val newText = value.text.replaceRange(sel.min, sel.max, withFmt)
        val newCursor = sel.min + withFmt.length
        return value.copy(text = newText, selection = TextRange(newCursor))
    }
    val cursor = sel.start
    val newText = value.text.substring(0, cursor) + prefix + suffix + value.text.substring(cursor)
    val newCursor = cursor + prefix.length
    return value.copy(text = newText, selection = TextRange(newCursor))
}
