package com.mxt.anitrend.base.custom.view.editor;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v13.view.inputmethod.EditorInfoCompat;
import android.support.v13.view.inputmethod.InputConnectionCompat;
import android.support.v13.view.inputmethod.InputContentInfoCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.mxt.anitrend.R;
import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.CompatUtil;
import com.mxt.anitrend.util.KeyUtil;
import com.mxt.anitrend.util.MarkDownUtil;

import static com.mxt.anitrend.util.KeyUtil.MD_BOLD;
import static com.mxt.anitrend.util.KeyUtil.MD_BULLET;
import static com.mxt.anitrend.util.KeyUtil.MD_CENTER_ALIGN;
import static com.mxt.anitrend.util.KeyUtil.MD_CODE;
import static com.mxt.anitrend.util.KeyUtil.MD_HEADING;
import static com.mxt.anitrend.util.KeyUtil.MD_ITALIC;
import static com.mxt.anitrend.util.KeyUtil.MD_NUMBER;
import static com.mxt.anitrend.util.KeyUtil.MD_QUOTE;
import static com.mxt.anitrend.util.KeyUtil.MD_STRIKE;

/**
 * Created by max on 2017/08/14.
 * Markdown input editor
 */

public class MarkdownInputEditor extends TextInputEditText implements CustomView, ActionMode.Callback,
        InputConnectionCompat.OnCommitContentListener{

    public MarkdownInputEditor(Context context) {
        super(context);
        onInit();
    }

    public MarkdownInputEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public MarkdownInputEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    private InputFilter EMOJI_FILTER = (source, start, end, dest, dstart, dend) -> {
        for (int index = start; index < end; index++) {
            int type = Character.getType(source.charAt(index));
            if (type == Character.SURROGATE)
                return "";
        }
        return null;
    };

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        setFilters(new InputFilter[] {EMOJI_FILTER});
        setVerticalScrollBarEnabled(true);
        setCustomSelectionActionModeCallback(this);
        setMaxHeight(CompatUtil.dipToPx(KeyUtil.PEEK_HEIGHT));
        setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection ic = super.onCreateInputConnection(outAttrs);
        EditorInfoCompat.setContentMimeTypes(outAttrs, new String [] {"image/png", "image/gif"});
        return InputConnectionCompat.createWrapper(ic, outAttrs, this);
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
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.editor_menu, menu);
        return true;
    }

    /**
     * Called to refresh an action mode's action menu whenever it is invalidated.
     *
     * @param mode ActionMode being prepared
     * @param menu Menu used to populate action buttons
     * @return true if the menu or action mode was updated, false otherwise.
     */
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    /**
     * Used to get the correct selection end range depending on the text size
     * of the preferred mark_down styling.
     *
     * @param selection The menu item from the selection mode
     * @return end selection size with applied offset
     */
    private int getSelectionEnd(@IdRes int selection) {
        int end = getSelectionEnd();
        final int init_end = getSelectionEnd();
        switch (selection) {
            case R.id.menu_bold:
                end += MD_BOLD.length();
                break;
            case R.id.menu_italic:
                end += MD_ITALIC.length();
                break;
            case R.id.menu_strike:
                end += MD_STRIKE.length();
                break;
            case R.id.menu_list:
                end += MD_NUMBER.length();
                break;
            case R.id.menu_bullet:
                end += MD_BULLET.length();
                break;
            case R.id.menu_heading:
                end += MD_HEADING.length();
                break;
            case R.id.menu_center:
                end += MD_CENTER_ALIGN.length();
                break;
            case R.id.menu_quote:
                end += MD_QUOTE.length();
                break;
            case R.id.menu_code:
                end += MD_CODE.length();
                break;
        }
        // Rare case but if it ever happens reduce end by 1
        final int text_length = getText().length();
        if(end > text_length + (end - init_end))
            end -= (end - init_end) - 1;
        return end;
    }

    /**
     * Called to report a user click on an action button.
     *
     * @param mode The current ActionMode
     * @param item The item that was clicked
     * @return true if this callback handled the event, false if the standard MenuItem
     * invocation should continue.
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int start = getSelectionStart();
        int end = getSelectionEnd(item.getItemId());
        switch (item.getItemId()) {
            case R.id.menu_bold:
                getText().insert(start, MD_BOLD);
                getText().insert(end, MD_BOLD, 0, MD_BOLD.length());
                mode.finish();
                return true;
            case R.id.menu_italic:
                getText().insert(start, MD_ITALIC);
                getText().insert(end, MD_ITALIC, 0, MD_ITALIC.length());
                mode.finish();
                return true;
            case R.id.menu_strike:
                getText().insert(start, MD_STRIKE);
                getText().insert(end, MD_STRIKE, 0, MD_STRIKE.length());
                mode.finish();
                return true;
            case R.id.menu_list:
                getText().insert(start, MD_NUMBER);
                mode.finish();
                return true;
            case R.id.menu_bullet:
                getText().insert(start, MD_BULLET);
                mode.finish();
                return true;
            case R.id.menu_heading:
                getText().insert(start, MD_HEADING);
                mode.finish();
                return true;
            case R.id.menu_center:
                getText().insert(start, MD_CENTER_ALIGN);
                getText().insert(end, MD_CENTER_ALIGN, 0, MD_CENTER_ALIGN.length());
                mode.finish();
                return true;
            case R.id.menu_quote:
                getText().insert(start, MD_QUOTE);
                mode.finish();
                return true;
            case R.id.menu_code:
                getText().insert(start, MD_CODE);
                getText().insert(end, MD_CODE, 0, MD_CODE.length());
                mode.finish();
                return true;
        }
        return false;
    }

    /**
     * @return composed text as hex html entities
     */
    public String getFormattedText() {
        String content = getText().toString();
        // disabled until anilist introduces emojis
        //return EmojiUtils.hexHtmlify(content);
        return content;
    }

    /**
     * Called when an action mode is about to be exited and destroyed.
     *
     * @param mode The current ActionMode being destroyed
     */
    @Override
    public void onDestroyActionMode(ActionMode mode) {

    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(getText());
    }

    /**
     * Intercepts InputConnection#commitContent API calls.
     *
     * @param inputContentInfo content to be committed
     * @param flags            {@code 0} or {@link InputConnection#INPUT_CONTENT_GRANT_READ_URI_PERMISSION}
     * @param opts             optional bundle data. This can be {@code null}
     * @return {@code true} if this request is accepted by the application, no matter if the
     * request is already handled or still being handled in background. {@code false} to use the
     * default implementation
     */
    @Override
    public boolean onCommitContent(InputContentInfoCompat inputContentInfo, int flags, @Nullable Bundle opts) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && (flags & InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION) != 0)
                inputContentInfo.requestPermission();
            Uri linkUri = inputContentInfo.getLinkUri();
            if(linkUri != null) {
                String link = MarkDownUtil.convertImage(linkUri.toString());
                getText().insert(getSelectionStart(), link);
                inputContentInfo.releasePermission();
                return true;
            }
            inputContentInfo.releasePermission();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;  // return true if succeeded
    }
}
