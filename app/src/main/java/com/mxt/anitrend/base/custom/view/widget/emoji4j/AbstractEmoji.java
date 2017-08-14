package com.mxt.anitrend.base.custom.view.widget.emoji4j;

import com.google.code.regexp.Pattern;

/**
 * 
 * @author chait
 *
 */
public abstract class AbstractEmoji {

	protected static final Pattern shortCodePattern = Pattern.compile(":(\\w+):");
	
	protected static final Pattern htmlEntityPattern = Pattern.compile("&#\\w+;");
	
	protected static final Pattern htmlSurrogateEntityPattern = Pattern.compile("(?<H>&#\\w+;)(?<L>&#\\w+;)");
	
	protected static final Pattern htmlSurrogateEntityPattern2 = Pattern.compile("&#\\w+;&#\\w+;&#\\w+;&#\\w+;");
	
	protected static final Pattern shortCodeOrHtmlEntityPattern = Pattern.compile(":\\w+:|(?<H1>&#\\w+;)(?<H2>&#\\w+;)(?<L1>&#\\w+;)(?<L2>&#\\w+;)|(?<H>&#\\w+;)(?<L>&#\\w+;)|&#\\w+;");
	
	/**
	 * Helper to convert emoji characters to html entities in a string
	 * 
	 * @param text String to htmlify
	 * @param isHex isHex
	 * @return htmlified string
	 */
	protected static String htmlifyHelper(String text, boolean isHex, boolean isSurrogate) {

		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < text.length(); i++) {
			int ch = text.codePointAt(i);

			if (ch <= 128) {
				sb.appendCodePoint(ch);
			} else if (ch > 128 && (ch < 159 || (ch >= 55296 && ch <= 57343))) {
				// don't write illegal html characters
				// refer
				// http://en.wikipedia.org/wiki/Character_encodings_in_HTML
				// Illegal characters section
				continue;
			} else {
				if (isHex) {
					sb.append("&#x" + Integer.toHexString(ch) + ";");
				} else {
					if(isSurrogate) {
						double H = Math.floor((ch - 0x10000) / 0x400) + 0xD800;
						double L = ((ch - 0x10000) % 0x400) + 0xDC00;
						sb.append("&#"+String.format("%.0f", H)+";&#"+String.format("%.0f", L)+";");
					} else {
						sb.append("&#" + ch + ";");
					}
				}
			}

		}

		return sb.toString();
	}
}
