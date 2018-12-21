package com.mxt.anitrend;

import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.parser.Parser;

import java.util.Arrays;

import ru.noties.markwon.Markwon;
import ru.noties.markwon.tasklist.TaskListExtension;

public class AnilistMarkdown {

    public static Parser createParser() {
        return new Parser.Builder()
                .extensions(Arrays.asList(
                        AnilistMarkdownExtension.create(),
                        StrikethroughExtension.create(),
                        TablesExtension.create(),
                        TaskListExtension.create()
                ))
                .build();
    }



    private AnilistMarkdown() {

    }
}
