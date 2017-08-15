package com.mxt.anitrend;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.mxt.anitrend.base.custom.view.widget.emoji4j.Emoji;
import com.mxt.anitrend.base.custom.view.widget.emoji4j.EmojiManager;
import com.mxt.anitrend.base.custom.view.widget.emoji4j.EmojiUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by max on 2017/05/05.
 * Some of the tests will fail since I disabled some conversion features
 */
@RunWith(AndroidJUnit4.class)
public class EmojiTest {

    private Context mContext;

    public void getContext() {
        if(mContext == null)
            mContext = InstrumentationRegistry.getTargetContext();
        assertNotNull(mContext);
    }

    @Test
    public void startingPoint() {
        getContext();
        if(EmojiManager.data() == null)
            try {
                EmojiManager.initEmojiData(mContext);
            } catch (IOException e) {
                e.printStackTrace();
            }
        assertNotNull(EmojiManager.data());
    }

    @Test
    public void testEmojiByUnicode() {
        // existing emoji
        startingPoint();
        Emoji emoji = EmojiUtils.getEmoji("😃");
        assertNotNull(emoji);
        assertTrue(emoji.getAliases().contains("smiley"));

        emoji = EmojiUtils.getEmoji("\uD83D\uDC2D");
        assertNotNull(emoji);
        assertTrue(emoji.getHexHtml().equals("&#x1f42d;"));

        // not an emoji character
        emoji = EmojiUtils.getEmoji("అ");
        assertNull(emoji);
    }

    @Test
    public void testEmojiByShortCode() {
        // existing emoji
        startingPoint();
        Emoji emoji = EmojiUtils.getEmoji("blue_car");
        assertNotNull(emoji);
        assertEquals("🚙",emoji.getEmoji());

        // not an emoji character
        emoji = EmojiUtils.getEmoji("bluecar");
        assertNull(emoji);
    }

    @Test
    public void testEmojiByShortCodeWithColons() {
        // existing emoji
        startingPoint();
        Emoji emoji = EmojiUtils.getEmoji(":blue_car:");
        assertNotNull(emoji);
        assertEquals("🚙",emoji.getEmoji());

        // not an emoji character
        emoji = EmojiUtils.getEmoji(":bluecar:");
        assertNull(emoji);
    }

    @Test
    public void testEmojiByHexHtml() {
        // get by hexhtml
        startingPoint();
        Emoji emoji = EmojiUtils.getEmoji("&#x1f42d;");
        assertNotNull(emoji);
        assertTrue(emoji.getEmoji().equals("🐭"));

    }

    @Test
    public void testEmojiByDecimalHtml() {
        // get by decimal html
        startingPoint();
        Emoji emoji = EmojiUtils.getEmoji("&#128045;");
        assertNotNull(emoji);
        assertTrue(emoji.getEmoji().equals("🐭"));

    }

    @Test
    public void testIsEmoji() {
        startingPoint();
        assertTrue(EmojiUtils.isEmoji("&#128045;"));

        assertFalse(EmojiUtils.isEmoji("&#123;"));

        assertTrue(EmojiUtils.isEmoji("🐭"));

        assertTrue(EmojiUtils.isEmoji("smile"));

        assertTrue(EmojiUtils.isEmoji(":smiley:"));
    }

    @Test
    public void testEmojify1() {
        startingPoint();
        String text = "A :cat:, :dog: and a :mouse: became friends. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.";
        assertEquals("A 🐱, 🐶 and a 🐭 became friends. For 🐶's birthday party, they all had 🍔s, 🍟s, 🍪s and 🍰.", EmojiUtils.emojify(text));

        text = "A :cat:, :dog:, :coyote: and a :mouse: became friends. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.";
        assertEquals("A 🐱, 🐶, :coyote: and a 🐭 became friends. For 🐶's birthday party, they all had 🍔s, 🍟s, 🍪s and 🍰.", EmojiUtils.emojify(text));

    }

    @Test
    public void testEmojify2() {
        startingPoint();
        String text = "A &#128049;, &#x1f436; and a :mouse: became friends. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.";
        assertEquals("A 🐱, 🐶 and a 🐭 became friends. For 🐶's birthday party, they all had 🍔s, 🍟s, 🍪s and 🍰.", EmojiUtils.emojify(text));

        text = "A &#128049;, &#x1f436;, &nbsp; and a :mouse: became friends. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.";
        assertEquals("A 🐱, 🐶, &nbsp; and a 🐭 became friends. For 🐶's birthday party, they all had 🍔s, 🍟s, 🍪s and 🍰.", EmojiUtils.emojify(text));
    }

    @Test
    public void testCountEmojis() {
        startingPoint();
        String text = "A &#128049;, &#x1f436;,&nbsp;:coyote: and a :mouse: became friends. For :dog:'s birthday party, they all had 🍔s, :fries:s, :cookie:s and :cake:.";
        assertTrue(EmojiUtils.countEmojis(text) == 8);
    }

    @Test
    public void testHtmlify() {
        startingPoint();
        String text = "A :cat:, :dog: and a :mouse: became friends. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.";
        String htmlifiedText = EmojiUtils.htmlify(text);

        assertEquals("A &#128049;, &#128054; and a &#128045; became friends. For &#128054;'s birthday party, they all had &#127828;s, &#127839;s, &#127850;s and &#127856;.", htmlifiedText);

        // also verify by emojifying htmlified text

        assertEquals("A 🐱, 🐶 and a 🐭 became friends. For 🐶's birthday party, they all had 🍔s, 🍟s, 🍪s and 🍰.", EmojiUtils.emojify(htmlifiedText));
    }

    @Test
    public void testHexHtmlify() {
        startingPoint();
        String text = "A :cat:, :dog: and a :mouse: became friends. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.";
        String htmlifiedText = EmojiUtils.hexHtmlify(text);

        assertEquals("A &#x1f431;, &#x1f436; and a &#x1f42d; became friends. For &#x1f436;'s birthday party, they all had &#x1f354;s, &#x1f35f;s, &#x1f36a;s and &#x1f370;.", htmlifiedText);
        // also verify by emojifying htmlified text
        assertEquals("A 🐱, 🐶 and a 🐭 became friends. For 🐶's birthday party, they all had 🍔s, 🍟s, 🍪s and 🍰.", EmojiUtils.emojify(htmlifiedText));
    }

    @Test
    public void testShortCodifyFromEmojis() {
        startingPoint();
        String text = "A 🐱, 🐶 and a 🐭 became friends❤️. For 🐶's birthday party, they all had 🍔s, 🍟s, 🍪s and 🍰.";
        assertEquals("A :cat:, :dog: and a :mouse: became friends:heart:. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.", EmojiUtils.shortCodify(text));

    }

    @Test
    public void testShortCodifyFromEmoticons() {
        startingPoint();
        String text = ":):-),:-):-]:-xP=*:*<3:P:p,=-)";
        String actual = EmojiUtils.shortCodify(text);
        assertEquals(":smiley::smiley::sweat_smile::smiley::no_mouth::stuck_out_tongue_closed_eyes::kissing::kissing::heart::stuck_out_tongue::stuck_out_tongue::sweat_smile:", actual);

        assertEquals("😃😃😅😃😶😝😗😗❤️😛😛😅", EmojiUtils.emojify(actual));
    }

    @Test
    public void testShortCodifyFromHtmlEntities() {
        startingPoint();
        String text = "A &#128049;, &#128054; and a &#128045; became friends. For &#128054;'s birthday party, they all had &#127828;s, &#127839;s, &#127850;s and &#127856;.";
        assertEquals("A :cat:, :dog: and a :mouse: became friends. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.", EmojiUtils.shortCodify(text));

        text = "A &#x1f431;, &#x1f436; and a &#x1f42d; became friends. For &#x1f436;'s birthday party, they all had &#x1f354;s, &#x1f35f;s, &#x1f36a;s and &#x1f370;.";
        assertEquals("A :cat:, :dog: and a :mouse: became friends. For :dog:'s birthday party, they all had :hamburger:s, :fries:s, :cookie:s and :cake:.", EmojiUtils.shortCodify(text));

    }

    @Test
    public void removeAllEmojisTest() {
        startingPoint();
        String emojiText = "A 🐱, 🐱 and a 🐭 became friends❤️. For 🐶's birthday party, they all had 🍔s, 🍟s, 🍪s and 🍰.";
        assertEquals("A ,  and a  became friends. For 's birthday party, they all had s, s, s and .", EmojiUtils.removeAllEmojis(emojiText));
    }

    @Test
    public void surrogateDecimalToEmojiTest() {
        startingPoint();
        String emojiText = "A &#55357;&#56369;, &#x1f436;&#55357;&#56369; and a &#55357;&#56365; became friends. They had &#junk;&#55356;&#57172;&#junk; :-)";
        assertEquals("A 🐱, 🐶🐱 and a 🐭 became friends. They had &#junk;🍔&#junk; 😃", EmojiUtils.emojify(emojiText));

        emojiText = "&#10084;&#65039;&#junk;&#55357;&#56374;";
        assertEquals("❤️&#junk;🐶", EmojiUtils.emojify(emojiText));

        emojiText = "&#55357;&#56833;";
        assertEquals("😁", EmojiUtils.emojify(emojiText));
    }

    @Test
    public void toSurrogateDecimalAndBackTest() {
        startingPoint();
        String text = "😃😃😅😃😶😝😗😗❤️😛😛😅❤️😛";
        String htmlifiedText = EmojiUtils.htmlify(text, true);
        assertEquals("&#55357;&#56835;&#55357;&#56835;&#55357;&#56837;&#55357;&#56835;&#55357;&#56886;&#55357;&#56861;&#55357;&#56855;&#55357;&#56855;&#55242;&#56164;&#55296;&#55823;&#55357;&#56859;&#55357;&#56859;&#55357;&#56837;&#55242;&#56164;&#55296;&#55823;&#55357;&#56859;", htmlifiedText);

        assertEquals(text, EmojiUtils.emojify(htmlifiedText));
    }

    @Test
    public void surrogateToHTMLTest() {
        startingPoint();
        String surrogateText = "&#55357;&#56835;&#55357;&#56835;&#55357;&#56837;&#55357;&#56835;&#55357;&#56886;&#55357;&#56861;&#55357;&#56855;&#55357;&#56855;&#55242;&#56164;&#55296;&#55823;&#55357;&#56859;&#55357;&#56859;&#55357;&#56837;&#55242;&#56164;&#55296;&#55823;&#55357;&#56859;";

        String emojiText = "😃😃😅😃😶😝😗😗❤️😛😛😅❤️😛";
        String decHtmlString = EmojiUtils.htmlify(surrogateText);

        String hexHtmlString = EmojiUtils.hexHtmlify(surrogateText);

        assertEquals("&#128515;&#128515;&#128517;&#128515;&#128566;&#128541;&#128535;&#128535;&#10084;&#65039;&#128539;&#128539;&#128517;&#10084;&#65039;&#128539;", decHtmlString);

        assertEquals("&#x1f603;&#x1f603;&#x1f605;&#x1f603;&#x1f636;&#x1f61d;&#x1f617;&#x1f617;&#x2764;&#xfe0f;&#x1f61b;&#x1f61b;&#x1f605;&#x2764;&#xfe0f;&#x1f61b;", hexHtmlString);

        assertEquals(emojiText, EmojiUtils.emojify(decHtmlString));

        assertEquals(emojiText, EmojiUtils.emojify(hexHtmlString));
    }
}
