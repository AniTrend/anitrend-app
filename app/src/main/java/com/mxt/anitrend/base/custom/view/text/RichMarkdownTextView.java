package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.StringRes;
import android.support.v4.text.util.LinkifyCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.widget.TextView;
import android.widget.Toast;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.MarkDownUtil;
import com.mxt.anitrend.util.RegexUtil;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.il.AsyncDrawableLoader;

public class RichMarkdownTextView extends AppCompatTextView implements CustomView {

    public RichMarkdownTextView(Context context) {
        super(context);
        onInit();
    }

    public RichMarkdownTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        onInit();
    }

    public RichMarkdownTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInit();
    }

    /**
     * Optionally included when constructing custom views
     */
    @Override
    public void onInit() {
        setFocusable(false);
        LinkifyCompat.addLinks(this, Linkify.WEB_URLS);
        setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    @BindingAdapter("markDown")
    public static void markDown(RichMarkdownTextView richMarkdownTextView, String markdown) {
        String strippedText = RegexUtil.removeTags(markdown);
        Spanned markdownSpan = MarkDownUtil.convert(strippedText, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);
    }

    @BindingAdapter("textHtml")
    public static void htmlText(RichMarkdownTextView richMarkdownTextView, String html) {
        Spanned markdownSpan = MarkDownUtil.convert(html, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);
    }

    @BindingAdapter("basicHtml")
    public static void basicText(RichMarkdownTextView richMarkdownTextView, String html) {
        Spanned htmlSpan = Html.fromHtml(html);
        richMarkdownTextView.setText(htmlSpan);
    }

    @BindingAdapter("textHtml")
    public static void htmlText(RichMarkdownTextView richMarkdownTextView, @StringRes int resId) {
        String text = richMarkdownTextView.getContext().getString(resId);
        Spanned markdownSpan = MarkDownUtil.convert(text, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);
    }

    @BindingAdapter("richMarkDown")
    public static void richMarkDown(RichMarkdownTextView richMarkdownTextView, String markdown) {
        final Context context = richMarkdownTextView.getContext();

        final SpannableConfiguration configuration = SpannableConfiguration.builder(context)
                .asyncDrawableLoader(AsyncDrawableLoader.create())
                .htmlAllowNonClosedTags(false)
                .softBreakAddsNewLine(false)
                .build();

        Markwon.setMarkdown(richMarkdownTextView, configuration, RegexUtil.convertToStandardMarkdown(markdown));
    }

    public void setMarkDownText(String markDownText) {
        String strippedText = RegexUtil.removeTags(markDownText);
        Spanned markdownSpan = MarkDownUtil.convert(strippedText, getContext(), this);
        setText(markdownSpan, TextView.BufferType.SPANNABLE);
    }
}
