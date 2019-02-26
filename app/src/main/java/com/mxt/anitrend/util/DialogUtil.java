package com.mxt.anitrend.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.text.InputType;
import android.text.SpannedString;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.mxt.anitrend.BuildConfig;
import com.mxt.anitrend.R;
import com.mxt.anitrend.base.custom.view.text.RichMarkdownTextView;
import com.mxt.anitrend.base.custom.view.text.SingleLineTextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Created by max on 2017/09/16.
 * Creates different dialog types
 */

public class DialogUtil {

    public static void createDialogAttachMedia(@IdRes int action, final EditText editor, Context context) {

        MaterialDialog.Builder builder = createDefaultDialog(context).positiveText(R.string.Ok)
                .negativeText(R.string.Cancel).autoDismiss(false)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .input(context.getString(R.string.text_enter_text), null, (dialog, input) -> {
                    // on input
                });

        switch (action) {
            case R.id.insert_link:
                builder.title(R.string.attach_link_title)
                        .content(R.string.attach_link_text)
                        .onAny((dialog, which) -> {
                            switch (which) {
                                case POSITIVE:
                                    EditText editText = dialog.getInputEditText();
                                    if(editText != null) {
                                        if(!TextUtils.isEmpty(editText.getText())) {
                                            int start = editor.getSelectionStart();
                                            editor.getEditableText().insert(start, MarkDownUtil.convertLink(editText.getText()));
                                            dialog.dismiss();
                                        } else {
                                            NotifyUtil.makeText(context, R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    break;
                                case NEUTRAL:
                                    break;
                                case NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        });
                break;
            case R.id.insert_image:
                builder.title(R.string.attach_image_title)
                        .content(R.string.attach_image_text)
                        .onAny((dialog, which) -> {
                            switch (which) {
                                case POSITIVE:
                                    EditText editText = dialog.getInputEditText();
                                    if(editText != null) {
                                        if(!TextUtils.isEmpty(editText.getText())) {
                                            int start = editor.getSelectionStart();
                                            editor.getEditableText().insert(start, MarkDownUtil.convertImage(editText.getText()));
                                            dialog.dismiss();
                                        } else {
                                            NotifyUtil.makeText(context, R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    break;
                                case NEUTRAL:
                                    break;
                                case NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        });
                break;
            case R.id.insert_youtube:
                builder.title(R.string.attach_youtube_title)
                        .content(R.string.attach_youtube_text)
                        .onAny((dialog, which) -> {
                            switch (which) {
                                case POSITIVE:
                                    EditText editText = dialog.getInputEditText();
                                    if(editText != null) {
                                        if(!TextUtils.isEmpty(editText.getText())) {
                                            int start = editor.getSelectionStart();
                                            editor.getEditableText().insert(start, MarkDownUtil.convertYoutube(editText.getText()));
                                            dialog.dismiss();
                                        } else {
                                            NotifyUtil.makeText(context, R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    break;
                                case NEUTRAL:
                                    break;
                                case NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        });
                break;
            case R.id.insert_webm:
                builder.title(R.string.attach_webm_title)
                        .content(R.string.attach_webm_text)
                        .onAny((dialog, which) -> {
                            switch (which) {
                                case POSITIVE:
                                    EditText editText = dialog.getInputEditText();
                                    if(editText != null) {
                                        if(!TextUtils.isEmpty(editText.getText())) {
                                            int start = editor.getSelectionStart();
                                            editor.getEditableText().insert(start, MarkDownUtil.convertVideo(editText.getText()));
                                            dialog.dismiss();
                                        } else {
                                            NotifyUtil.makeText(context, R.string.input_empty_warning, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    break;
                                case NEUTRAL:
                                    break;
                                case NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        });
                break;
        }
        builder.show();
    }

    public static <T> void createSelection(Context context, @StringRes int title, int selectedIndex, Collection<T> selectableItems, MaterialDialog.SingleButtonCallback singleButtonCallback) {
        createDefaultDialog(context).title(title)
                .items(selectableItems)
                .positiveText(R.string.Ok)
                .negativeText(R.string.Cancel)
                .itemsCallbackSingleChoice(selectedIndex, (materialDialog, view, i, charSequence) -> false)
                .autoDismiss(true).onAny(singleButtonCallback).show();
    }

    public static void createMessage(Context context, @StringRes int title, @StringRes int content, MaterialDialog.SingleButtonCallback singleButtonCallback) {
        createDefaultDialog(context).title(title)
                .positiveText(R.string.Ok)
                .negativeText(R.string.Cancel)
                .icon(CompatUtil.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp))
                .content(new SpannedString(context.getString(content)))
                .autoDismiss(true).onAny(singleButtonCallback).show();
    }

    public static void createMessage(Context context, String title, String content) {
        createDefaultDialog(context).title(title)
                .positiveText(R.string.Close)
                .icon(CompatUtil.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp))
                .content(MarkDownUtil.convert(content))
                .autoDismiss(true).show();
    }

    public static void createMessage(Context context, String title, String content, @StringRes int positive, @StringRes int negative, @StringRes int neutral, MaterialDialog.SingleButtonCallback singleButtonCallback) {
        createDefaultDialog(context).title(title)
                .positiveText(positive)
                .negativeText(negative)
                .neutralText(neutral)
                .icon(CompatUtil.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp))
                .content(MarkDownUtil.convert(content))
                .autoDismiss(true).onAny(singleButtonCallback).show();
    }

    public static void createMessage(Context context, String title, String content, @StringRes int positive, @StringRes int negative, MaterialDialog.SingleButtonCallback singleButtonCallback) {
        createDefaultDialog(context).title(title)
                .positiveText(positive)
                .negativeText(negative)
                .icon(CompatUtil.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp))
                .content(MarkDownUtil.convert(content))
                .autoDismiss(true).onAny(singleButtonCallback).show();
    }

    public static void createTagMessage(Context context, String title, String content, Boolean isSpoiler, @StringRes int positive, @StringRes int negative, MaterialDialog.SingleButtonCallback singleButtonCallback) {
        MaterialDialog.Builder builder = createDefaultDialog(context).title(title)
                .positiveText(positive)
                .negativeText(negative)
                .icon(CompatUtil.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp))
                .content(MarkDownUtil.convert(content))
                .autoDismiss(true).onAny(singleButtonCallback);

        if (isSpoiler) builder.icon(CompatUtil.getDrawable(context, R.drawable.ic_spoiler_tag));
        else builder.icon(CompatUtil.getDrawable(context, R.drawable.ic_loyalty_white_24dp));

        builder.show();
    }

    public static <T> void createCheckList(Context context, @StringRes int title, Collection<T> selectableItems, Integer[] selectedIndices, MaterialDialog.ListCallbackMultiChoice listCallbackMultiChoice, MaterialDialog.SingleButtonCallback singleButtonCallback) {
        createDefaultDialog(context).title(title)
                .items(selectableItems)
                .positiveText(R.string.Ok)
                .negativeText(R.string.Reset)
                .neutralText(R.string.Cancel)
                .icon(CompatUtil.getTintedDrawable(context, R.drawable.ic_new_releases_white_24dp))
                .itemsCallbackMultiChoice(selectedIndices,listCallbackMultiChoice)
                .autoDismiss(true).onAny(singleButtonCallback).show();
    }

    public static void createChangeLog(Context context) {
        try {
            MaterialDialog materialDialog = createDefaultDialog(context)
                    .customView(R.layout.dialog_changelog, true)
                    .build();

            SingleLineTextView singleLineTextView = (SingleLineTextView) materialDialog.findViewById(R.id.changelog_version);
            singleLineTextView.setText(String.format("v%s", BuildConfig.VERSION_NAME));

            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("changelog.md");
            StringBuilder stringBuilder = new StringBuilder();
            int buffer;
            while ((buffer = inputStream.read()) != -1)
                stringBuilder.append((char)buffer);
            RichMarkdownTextView richMarkdownTextView = (RichMarkdownTextView) materialDialog.findViewById(R.id.changelog_information);
            RichMarkdownTextView.richMarkDown(richMarkdownTextView, stringBuilder.toString());

            materialDialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds themed material dialog builder for basic configuration
     * <br/>
     *
     * @param context from a fragment activity derived class
     * @see android.support.v4.app.FragmentActivity
     */
    static MaterialDialog.Builder createDefaultDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .typeface(Typeface.SANS_SERIF,Typeface.SANS_SERIF)
                .buttonRippleColorRes(R.color.colorAccentDark)
                .positiveColorRes(R.color.colorStateGreen)
                .negativeColorRes(R.color.colorStateOrange)
                .neutralColorRes(R.color.colorStateBlue)
                .theme(CompatUtil.isLightTheme(context)?Theme.LIGHT:Theme.DARK);
    }
}
