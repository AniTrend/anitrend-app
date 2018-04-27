package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.StringRes;
import android.support.v4.text.util.LinkifyCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mxt.anitrend.base.interfaces.view.CustomView;
import com.mxt.anitrend.util.MarkDown;
import com.mxt.anitrend.util.PatternMatcher;
import com.zzhoujay.richtext.RichText;
import com.zzhoujay.richtext.RichType;
import com.zzhoujay.richtext.ig.DefaultImageGetter;

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
        String strippedText = PatternMatcher.removeTags(markdown);
        Spanned markdownSpan = MarkDown.convert(strippedText, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);
    }

    @BindingAdapter("textHtml")
    public static void htmlText(RichMarkdownTextView richMarkdownTextView, String html) {
        Spanned markdownSpan = MarkDown.convert(html, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);
    }

    @BindingAdapter("textHtml")
    public static void htmlText(RichMarkdownTextView richMarkdownTextView, @StringRes int resId) {
        String text = richMarkdownTextView.getContext().getString(resId);
        Spanned markdownSpan = MarkDown.convert(text, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);
    }

    @BindingAdapter("richMarkDown")
    public static void richMarkDown(RichMarkdownTextView richMarkdownTextView, String markdown) {
        RichText.from(PatternMatcher.convertToStandardMarkdown(markdown))
                .imageGetter(new DefaultImageGetter())
                .bind(richMarkdownTextView.getContext())
                .type(RichType.markdown).into(richMarkdownTextView);
    }

    public void setMarkDownText(String markDownText) {
        String strippedText = PatternMatcher.removeTags(markDownText);
        Spanned markdownSpan = MarkDown.convert(strippedText, getContext(), this);
        setText(markdownSpan, TextView.BufferType.SPANNABLE);
    }
}
