package com.mxt.anitrend;

import org.commonmark.parser.Parser;

public class AnilistMarkdownExtension implements Parser.ParserExtension {
    public static AnilistMarkdownExtension create() { return new AnilistMarkdownExtension(); }


    @Override
    public void extend(Parser.Builder parserBuilder) {
        // Processes "youtube()" links
        parserBuilder.customDelimiterProcessor(new AnilistMarkdownYoutubeLinkProcessor());
        parserBuilder.postProcessor(new AnilistMarkdownPostProcessor());
    }
}
