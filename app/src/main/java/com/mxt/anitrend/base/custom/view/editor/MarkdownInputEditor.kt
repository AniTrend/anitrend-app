package com.mxt.anitrend.base.custom.view.editor

import android.content.Context
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.TextInputEditText
import android.support.v13.view.inputmethod.EditorInfoCompat
import android.support.v13.view.inputmethod.InputConnectionCompat
import android.support.v13.view.inputmethod.InputContentInfoCompat
import android.support.v4.content.ContextCompat
import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection

import com.mxt.anitrend.R
import com.mxt.anitrend.base.interfaces.view.CustomView
import com.mxt.anitrend.util.CompatUtil
import com.mxt.anitrend.util.KeyUtil
import com.mxt.anitrend.util.MarkDownUtil

import io.wax911.emojify.parser.EmojiParser

import com.mxt.anitrend.util.KeyUtil.MD_BOLD
import com.mxt.anitrend.util.KeyUtil.MD_BULLET
import com.mxt.anitrend.util.KeyUtil.MD_CENTER_ALIGN
import com.mxt.anitrend.util.KeyUtil.MD_CODE
import com.mxt.anitrend.util.KeyUtil.MD_HEADING
import com.mxt.anitrend.util.KeyUtil.MD_ITALIC
import com.mxt.anitrend.util.KeyUtil.MD_NUMBER
import com.mxt.anitrend.util.KeyUtil.MD_QUOTE
import com.mxt.anitrend.util.KeyUtil.MD_STRIKE

/**
 * Created by max on 2017/08/14.
 * Markdown input editor
 */

class MarkdownInputEditor : TextInputEditText, CustomView, ActionMode.Callback, InputConnectionCompat.OnCommitContentListener {

    private val emojiInputFilter = object: InputFilter {
        /**
         * This method is called when the buffer is going to replace the
         * range `dstart  dend` of `dest`
         * with the new text from the range `start  end`
         * of `source`.  Return the CharSequence that you would
         * like to have placed there instead, including an empty string
         * if appropriate, or `null` to accept the original
         * replacement.  Be careful to not to reject 0-length replacements,
         * as this is what happens when you delete text.  Also beware that
         * you should not attempt to make any changes to `dest`
         * from this method; you may only examine it for context.
         *
         * Note: If <var>source</var> is an instance of [Spanned] or
         * [Spannable], the span objects in the <var>source</var> should be
         * copied into the filtered result (i.e. the non-null return value).
         * [TextUtils.copySpansFrom] can be used for convenience if the
         * span boundary indices would be remaining identical relative to the source.
         */
        override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned?, dstart: Int, dend: Int): CharSequence {
            source?.also {
                for (index in start until end) {
                    val type = Character.getType(it[index])
                    if (type == Character.SURROGATE.toInt())
                        return ""
                }
            }
            return ""
        }

    }

    /**
     * @return composed text as hex html entities
     */
    val formattedText: String?
        get() {
            val content = text.toString()
            return EmojiParser.parseToHtmlHexadecimal(content)
        }

    val isEmpty: Boolean
        get() = text.isNullOrBlank()

    constructor(context: Context) : super(context) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        onInit()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        onInit()
    }

    /**
     * Optionally included when constructing custom views
     */
    override fun onInit() {
        // filters = arrayOf<InputFilter>(emojiInputFilter)
        isVerticalScrollBarEnabled = true
        customSelectionActionModeCallback = this
        maxHeight = CompatUtil.dipToPx(KeyUtil.PEEK_HEIGHT)
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        typeface = Typeface.create("sans-serif-condensed", Typeface.NORMAL)
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val ic = super.onCreateInputConnection(outAttrs)
        EditorInfoCompat.setContentMimeTypes(outAttrs, arrayOf("image/png", "image/gif"))
        return InputConnectionCompat.createWrapper(ic, outAttrs, this)
    }


    /**
     * Called when action mode is first created. The menu supplied will be used to
     * generate action buttons for the action mode.
     *
     * @param mode ActionMode being created
     * @param menu Menu used to populate action buttons
     * @return true if the action mode should be created, false if entering this
     * mode should be aborted.
     */
    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        val inflater = mode.menuInflater
        inflater.inflate(R.menu.editor_menu, menu)
        return true
    }

    /**
     * Called to refresh an action mode's action menu whenever it is invalidated.
     *
     * @param mode ActionMode being prepared
     * @param menu Menu used to populate action buttons
     * @return true if the menu or action mode was updated, false otherwise.
     */
    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
        return false
    }

    /**
     * Used to get the correct selection end range depending on the text size
     * of the preferred mark_down styling.
     *
     * @param selection The menu item from the selection mode
     * @return end selection size with applied offset
     */
    private fun getSelectionEnd(@IdRes selection: Int): Int {
        var end = selectionEnd
        val init_end = selectionEnd
        when (selection) {
            R.id.menu_bold -> end += MD_BOLD.length
            R.id.menu_italic -> end += MD_ITALIC.length
            R.id.menu_strike -> end += MD_STRIKE.length
            R.id.menu_list -> end += MD_NUMBER.length
            R.id.menu_bullet -> end += MD_BULLET.length
            R.id.menu_heading -> end += MD_HEADING.length
            R.id.menu_center -> end += MD_CENTER_ALIGN.length
            R.id.menu_quote -> end += MD_QUOTE.length
            R.id.menu_code -> end += MD_CODE.length
        }
        // Rare case but if it ever happens reduce end by 1
        val text_length = text.length
        if (end > text_length + (end - init_end))
            end -= end - init_end - 1
        return end
    }

    /**
     * Called to report a user click on an action button.
     *
     * @param mode The current ActionMode
     * @param item The item that was clicked
     * @return true if this callback handled the event, false if the standard MenuItem
     * invocation should continue.
     */
    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        val start = selectionStart
        val end = getSelectionEnd(item.itemId)
        when (item.itemId) {
            R.id.menu_bold -> {
                text.insert(start, MD_BOLD)
                text.insert(end, MD_BOLD, 0, MD_BOLD.length)
                mode.finish()
                return true
            }
            R.id.menu_italic -> {
                text.insert(start, MD_ITALIC)
                text.insert(end, MD_ITALIC, 0, MD_ITALIC.length)
                mode.finish()
                return true
            }
            R.id.menu_strike -> {
                text.insert(start, MD_STRIKE)
                text.insert(end, MD_STRIKE, 0, MD_STRIKE.length)
                mode.finish()
                return true
            }
            R.id.menu_list -> {
                text.insert(start, MD_NUMBER)
                mode.finish()
                return true
            }
            R.id.menu_bullet -> {
                text.insert(start, MD_BULLET)
                mode.finish()
                return true
            }
            R.id.menu_heading -> {
                text.insert(start, MD_HEADING)
                mode.finish()
                return true
            }
            R.id.menu_center -> {
                text.insert(start, MD_CENTER_ALIGN)
                text.insert(end, MD_CENTER_ALIGN, 0, MD_CENTER_ALIGN.length)
                mode.finish()
                return true
            }
            R.id.menu_quote -> {
                text.insert(start, MD_QUOTE)
                mode.finish()
                return true
            }
            R.id.menu_code -> {
                text.insert(start, MD_CODE)
                text.insert(end, MD_CODE, 0, MD_CODE.length)
                mode.finish()
                return true
            }
        }
        return false
    }

    /**
     * Called when an action mode is about to be exited and destroyed.
     *
     * @param mode The current ActionMode being destroyed
     */
    override fun onDestroyActionMode(mode: ActionMode) {

    }

    /**
     * Clean up any resources that won't be needed
     */
    override fun onViewRecycled() {

    }

    /**
     * Intercepts InputConnection#commitContent API calls.
     *
     * @param inputContentInfo content to be committed
     * @param flags            `0` or [InputConnection.INPUT_CONTENT_GRANT_READ_URI_PERMISSION]
     * @param opts             optional bundle data. This can be `null`
     * @return `true` if this request is accepted by the application, no matter if the
     * request is already handled or still being handled in background. `false` to use the
     * default implementation
     */
    override fun onCommitContent(inputContentInfo: InputContentInfoCompat, flags: Int, opts: Bundle?): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && flags and InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0)
                inputContentInfo.requestPermission()
            val linkUri = inputContentInfo.linkUri
            if (linkUri != null) {
                val link = MarkDownUtil.convertImage(linkUri.toString())
                text.insert(selectionStart, link)
                inputContentInfo.releasePermission()
                return true
            }
            inputContentInfo.releasePermission()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return false  // return true if succeeded
    }
}
