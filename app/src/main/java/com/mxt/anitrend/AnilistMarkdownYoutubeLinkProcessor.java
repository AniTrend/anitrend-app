package com.mxt.anitrend;

import org.commonmark.node.Text;
import org.commonmark.parser.delimiter.DelimiterProcessor;
import org.commonmark.parser.delimiter.DelimiterRun;

public class AnilistMarkdownYoutubeLinkProcessor implements DelimiterProcessor {
    @Override
    public char getOpeningCharacter() { return 'y'; }

    @Override
    public char getClosingCharacter() { return ')'; }

    @Override
    public int getMinLength() { return 9; }

    @Override
    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        return 0;
    }

    @Override
    public void process(Text opener, Text closer, int delimiterUse) {

    }
}
