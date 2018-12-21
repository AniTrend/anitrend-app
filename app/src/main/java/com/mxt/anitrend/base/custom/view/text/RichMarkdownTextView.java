package com.mxt.anitrend.base.custom.view.text;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.StringRes;
import android.support.v4.text.util.LinkifyCompat;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;

import com.mxt.anitrend.AnilistMarkdown;
import com.mxt.anitrend.base.interfaces.view.CustomView;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.SpannableFactoryDef;
import ru.noties.markwon.il.AsyncDrawableLoader;
import ru.noties.markwon.renderer.SpannableRenderer;

public class RichMarkdownTextView extends AppCompatTextView implements CustomView {

    private SpannableConfiguration configuration;
    private final Parser parser = AnilistMarkdown.createParser();
    private final SpannableRenderer renderer = new SpannableRenderer();

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
        configuration = SpannableConfiguration.builder(getContext())
                .factory(new SpannableFactoryDef())
                .asyncDrawableLoader(AsyncDrawableLoader.create())
                .htmlAllowNonClosedTags(true)
                .softBreakAddsNewLine(true)
                .build();
    }

    /**
     * Clean up any resources that won't be needed
     */
    @Override
    public void onViewRecycled() {

    }

    private void renderMarkdown(String content) {

        Node node;

        if(TextUtils.isEmpty(content))
            node = parser.parse("<b>No content available</b>");
        else
            node = parser.parse(content);

        final CharSequence text = renderer.render(configuration, node);
        Markwon.scheduleDrawables(this);
        Markwon.setText(this, text);
    }

    @BindingAdapter("markDown")
    public static void markDown(RichMarkdownTextView richMarkdownTextView, String markdown) {
        richMarkdownTextView.renderMarkdown(markdown);
        /*
        String strippedText = RegexUtil.removeTags(markdown);
        Spanned markdownSpan = MarkDownUtil.convert(strippedText, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);*/
    }

    @BindingAdapter("textHtml")
    public static void htmlText(RichMarkdownTextView richMarkdownTextView, String html) {
        richMarkdownTextView.renderMarkdown(html);
        /*
        Spanned markdownSpan = MarkDownUtil.convert(html, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);*/
    }

    @BindingAdapter("basicHtml")
    public static void basicText(RichMarkdownTextView richMarkdownTextView, String html) {
        richMarkdownTextView.renderMarkdown(html);
        /*Spanned htmlSpan = Html.fromHtml(html);
        richMarkdownTextView.setText(htmlSpan);*/
    }

    @BindingAdapter("textHtml")
    public static void htmlText(RichMarkdownTextView richMarkdownTextView, @StringRes int resId) {
        String text = richMarkdownTextView.getContext().getString(resId);
        richMarkdownTextView.renderMarkdown(text);
        /*
        Spanned markdownSpan = MarkDownUtil.convert(text, richMarkdownTextView.getContext(), richMarkdownTextView);
        richMarkdownTextView.setText(markdownSpan, TextView.BufferType.SPANNABLE);*/
    }

    @BindingAdapter("richMarkDown")
    public static void richMarkDown(RichMarkdownTextView richMarkdownTextView, String markdown) {
        richMarkdownTextView.renderMarkdown(markdown);
    }

    public void setMarkDownText(String markDownText) {
        renderMarkdown(markDownText);
        /*
        String strippedText = RegexUtil.removeTags(markDownText);
        Spanned markdownSpan = MarkDownUtil.convert(strippedText, getContext(), this);
        setText(markdownSpan, TextView.BufferType.SPANNABLE);*/
    }
}

