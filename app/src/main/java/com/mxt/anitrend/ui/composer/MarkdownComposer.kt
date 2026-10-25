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
package com.mxt.anitrend.ui.composer

import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.content.TransferableContent
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mxt.anitrend.R
import com.mxt.anitrend.domain.feed.interactor.SaveFeedRequest
import com.mxt.anitrend.domain.model.FeedItemUiModel
import com.mxt.anitrend.extension.koinOf
import com.mxt.anitrend.navigation.model.FeedComposerScreenParam
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.markdown.MarkDownUtil
import com.mxt.anitrend.util.markdown.RegexUtil
import io.wax911.emojify.EmojiManager
import io.wax911.emojify.parser.parseToHtmlHexadecimal
import io.wax911.emojify.parser.parseToUnicode

private val supportedImageTypes = setOf("image/png", "image/gif")

/*
 * The shared Compose markdown composer for posts, messages, replies, shared
 * content and media-list notes. One text-editing state owns text, selection and
 * IME composition; formatting inserts KeyUtil.MD_* tokens at the selection using
 * the existing MarkDownUtil/RegexUtil builders, and IME image content becomes
 * markdown images at the cursor.
 */

/**
 * Remembers the composer draft across configuration and process recreation. The
 * seed passes through [EmojiManager.parseToUnicode] once, so server-encoded
 * emoji render as unicode emoji in the editor.
 */
@Composable
internal fun rememberComposerDraft(seed: String? = null): TextFieldValue {
    var draft by rememberSaveable(seed, stateSaver = TextFieldValue.Saver) {
        val emojified = seed?.let { koinOf<EmojiManager>().parseToUnicode(it) }.orEmpty()
        mutableStateOf(TextFieldValue(emojified))
    }
    return draft
}

/** Formats a composer draft for submission: emoji become HTML hex entities. */
internal fun formatComposerSubmit(emojiManager: EmojiManager, text: String): String = emojiManager.parseToHtmlHexadecimal(text)

/**
 * Maps a composer mode, its identity/draft parameter and the editor text to the
 * domain save request handled by [com.mxt.anitrend.domain.feed.interactor.SaveFeedInteractor].
 * This is the single conversion point for composer saves.
 *
 * @param requestType composer mode (text or message feed)
 * @param composerParam identity and draft state, or null when absent
 * @param text the editor text being submitted
 * @return the domain save request, or null when the mode is not a composer mode
 */
internal fun buildComposerSaveRequest(
    @KeyUtil.RequestType requestType: Int,
    composerParam: FeedComposerScreenParam?,
    text: String,
): SaveFeedRequest? = when (requestType) {
    KeyUtil.MUT_SAVE_TEXT_FEED ->
        SaveFeedRequest.Text(
            id = composerParam?.feedId,
            text = text,
        )

    KeyUtil.MUT_SAVE_MESSAGE_FEED ->
        SaveFeedRequest.Message(
            id = composerParam?.feedId,
            message = text,
            recipientId = composerParam?.recipientId ?: 0L,
        )

    else -> null
}

/**
 * Extracts the edit identity from an immutable [FeedItemUiModel] into the typed
 * [FeedComposerScreenParam], preserving any recipient identity already carried
 * by [current]. Only the stable feed id and draft text are copied.
 */
internal fun FeedItemUiModel.toComposerParam(current: FeedComposerScreenParam = FeedComposerScreenParam()): FeedComposerScreenParam = current.copy(
    feedId = id,
    draftText = feedText,
)

/**
 * The markdown composer: a formatting toolbar above the field with Bold, Italic,
 * Link and Attach visible and Strike/List/Heading/Center/Quote/Code plus
 * YouTube/WebM/GIF in overflow. Image URIs received from the keyboard insert
 * markdown images at the cursor; other content passes through untouched.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MarkdownComposer(
    draft: TextFieldValue,
    onDraftChange: (TextFieldValue) -> Unit,
    enabled: Boolean,
    placeholder: String,
    onAttach: () -> Unit,
    onGiphy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var overflowOpen by remember { mutableStateOf(false) }

    val boldLabel = stringResource(R.string.format_bold)
    val italicLabel = stringResource(R.string.format_italic)
    val linkLabel = stringResource(R.string.format_link)
    val overflowLabel = stringResource(R.string.More)
    val strikeLabel = stringResource(R.string.format_strike)
    val listLabel = stringResource(R.string.format_list)
    val numberedLabel = stringResource(R.string.format_numbered)
    val headingLabel = stringResource(R.string.format_heading)
    val centerLabel = stringResource(R.string.format_center)
    val quoteLabel = stringResource(R.string.format_quote)
    val codeLabel = stringResource(R.string.format_code)
    val youtubeLabel = stringResource(R.string.format_youtube)
    val webMLabel = stringResource(R.string.format_webm)
    val gifLabel = stringResource(R.string.format_gif)

    fun insertAtSelection(builder: (String) -> String) {
        val start = draft.selection.min
        val end = draft.selection.max
        val selected = draft.text.substring(start, end)
        val inserted = builder(selected)
        onDraftChange(
            TextFieldValue(
                text = draft.text.substring(0, start) + inserted + draft.text.substring(end),
                selection = TextRange(start + inserted.length),
            ),
        )
    }

    fun wrapSelection(token: String) = insertAtSelection { selected -> "$token$selected$token" }

    fun prefixLine(token: String) {
        val start = draft.selection.min
        val lineStart =
            if (start == 0 || draft.text[start - 1] == '\n') {
                start
            } else {
                draft.text.lastIndexOf('\n', start - 1).let { if (it == -1) 0 else it + 1 }
            }
        onDraftChange(
            TextFieldValue(
                text = draft.text.substring(0, lineStart) + token + draft.text.substring(lineStart),
                selection = TextRange(start + token.length),
            ),
        )
    }

    fun insertLink() = insertAtSelection { selected ->
        if (selected.startsWith("http")) {
            RegexUtil.createLinkStandard(selected)
        } else {
            "[${selected.ifBlank { linkLabel }}](https://)"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        IconButton(onClick = { wrapSelection(KeyUtil.MD_BOLD) }, enabled = enabled) {
            Icon(painter = painterResource(R.drawable.ic_format_bold_grey_600_24dp), contentDescription = boldLabel)
        }
        IconButton(onClick = { wrapSelection(KeyUtil.MD_ITALIC) }, enabled = enabled) {
            Icon(painter = painterResource(R.drawable.ic_format_italic_grey_600_24dp), contentDescription = italicLabel)
        }
        IconButton(onClick = { insertLink() }, enabled = enabled) {
            Icon(painter = painterResource(R.drawable.ic_link_white_24dp), contentDescription = linkLabel)
        }
        IconButton(onClick = onAttach, enabled = enabled) {
            Icon(painter = painterResource(R.drawable.ic_attach_file_white_24dp), contentDescription = stringResource(R.string.action_attach))
        }
        Box {
            IconButton(onClick = { overflowOpen = true }, enabled = enabled) {
                Icon(painter = painterResource(R.drawable.ic_code_grey_600_24dp), contentDescription = overflowLabel)
            }
            DropdownMenu(expanded = overflowOpen, onDismissRequest = { overflowOpen = false }) {
                DropdownMenuItem(
                    text = { Text(strikeLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_strikethrough_s_grey_600_24dp), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        wrapSelection(KeyUtil.MD_STRIKE)
                    },
                )
                DropdownMenuItem(
                    text = { Text(listLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_format_list_bulleted_grey_600_24dp), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        prefixLine(KeyUtil.MD_BULLET)
                    },
                )
                DropdownMenuItem(
                    text = { Text(numberedLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_format_list_numbered_grey_600_24dp), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        prefixLine(KeyUtil.MD_NUMBER)
                    },
                )
                DropdownMenuItem(
                    text = { Text(headingLabel) },
                    onClick = {
                        overflowOpen = false
                        prefixLine(KeyUtil.MD_HEADING)
                    },
                )
                DropdownMenuItem(
                    text = { Text(centerLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_format_align_center_grey_600_24dp), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        wrapSelection(KeyUtil.MD_CENTER_ALIGN)
                    },
                )
                DropdownMenuItem(
                    text = { Text(quoteLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_format_quote_grey_600_24dp), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        prefixLine(KeyUtil.MD_QUOTE)
                    },
                )
                DropdownMenuItem(
                    text = { Text(codeLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_code_grey_600_24dp), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        wrapSelection(KeyUtil.MD_CODE)
                    },
                )
                DropdownMenuItem(
                    text = { Text(youtubeLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_youtube), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        insertAtSelection { selected ->
                            RegexUtil.createYoutubeStandard(selected.ifBlank { "https://www.youtube.com/" })
                        }
                    },
                )
                DropdownMenuItem(
                    text = { Text(webMLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_movie_grey_600_24dp), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        insertAtSelection { selected ->
                            RegexUtil.createWebMStandard(selected.ifBlank { "https://" })
                        }
                    },
                )
                DropdownMenuItem(
                    text = { Text(gifLabel) },
                    leadingIcon = { Icon(painter = painterResource(R.drawable.ic_gif_white_24dp), contentDescription = null) },
                    onClick = {
                        overflowOpen = false
                        onGiphy()
                    },
                )
            }
        }
    }
    OutlinedTextField(
        value = draft,
        onValueChange = onDraftChange,
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .contentReceiver(
                object : ReceiveContentListener {
                    override fun onReceive(transferableContent: TransferableContent): TransferableContent {
                        val description = transferableContent.clipMetadata.clipDescription
                        val supported =
                            (0 until description.mimeTypeCount).any { index ->
                                description.getMimeType(index) in supportedImageTypes
                            }
                        if (!supported) return transferableContent
                        val uri = transferableContent.clipEntry.clipData.getItemAt(0).uri
                            ?: return transferableContent
                        insertAtSelection { MarkDownUtil.convertImage(uri.toString()) }
                        return transferableContent
                    }
                },
            ),
        enabled = enabled,
        placeholder = { Text(placeholder) },
        keyboardOptions =
        KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
        ),
        minLines = 1,
        maxLines = 6,
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}
