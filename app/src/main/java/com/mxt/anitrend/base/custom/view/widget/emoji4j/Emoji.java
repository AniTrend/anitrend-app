package com.mxt.anitrend.base.custom.view.widget.emoji4j;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.code.regexp.Matcher;

import java.util.List;

/**
 * Emoji
 * @author Krishna Chaitanya Thota
 *
 */
public class Emoji extends AbstractEmoji implements Parcelable {

	private String emoji;

	private List<String> aliases;

	private List<String> emoticons;

	private List<String> tags;

	private String hexHtml;

	private String decimalHtml;
	
	private String decimalHtmlShort;
	
	private String hexHtmlShort;
	
	private String decimalSurrogateHtml;

	protected Emoji(Parcel in) {
		emoji = in.readString();
		aliases = in.createStringArrayList();
		emoticons = in.createStringArrayList();
		tags = in.createStringArrayList();
		hexHtml = in.readString();
		decimalHtml = in.readString();
		decimalHtmlShort = in.readString();
		hexHtmlShort = in.readString();
		decimalSurrogateHtml = in.readString();
	}

	public static final Creator<Emoji> CREATOR = new Creator<Emoji>() {
		@Override
		public Emoji createFromParcel(Parcel in) {
			return new Emoji(in);
		}

		@Override
		public Emoji[] newArray(int size) {
			return new Emoji[size];
		}
	};

	/**
	 * Gets the unicode emoji character
	 * @return Emoji String
	 */
	public String getEmoji() {
		return emoji;
	}

	public void setEmoji(String emoji) {
		setDecimalHtml(EmojiUtils.htmlifyHelper(emoji,false, false));
		setHexHtml(EmojiUtils.htmlifyHelper(emoji,true, false));
		
		setDecimalSurrogateHtml(EmojiUtils.htmlifyHelper(emoji,false, true));
		this.emoji = emoji;
	}

	/**
	 * Gets the list of all shortcodes for the emoji. shortcode is not enclosed in colons.
	 * @return List of all aliases
	 */
	public List<String> getAliases() {
		return aliases;
	}

	public List<String> getTags() {
		return tags;
	}

	/**
	 * Gets the hexadecimal html entity for the emoji
	 * @return the hexadecimal html string
	 */
	public String getHexHtml() {
		return hexHtml;
	}

	public void setHexHtml(String hexHtml) {
		this.hexHtml = hexHtml;
		Matcher matcher = htmlSurrogateEntityPattern.matcher(hexHtml);
		if(matcher.find()) {
			String signifiantHtmlEntity = matcher.group("H");
			this.setHexHtmlShort(signifiantHtmlEntity);
		} else {
			this.setHexHtmlShort(hexHtml);
		}
	}

	/**
	 * Gets the decimal html entity for the emoji
	 * @return the decimal html string
	 */
	public String getDecimalHtml() {
		return decimalHtml;
	}

	public void setDecimalHtml(String decimalHtml) {
		
		this.decimalHtml = decimalHtml;
		Matcher matcher = htmlSurrogateEntityPattern.matcher(decimalHtml);
		if(matcher.find()) {
			String signifiantHtmlEntity = matcher.group("H");
			this.setDecimalHtmlShort(signifiantHtmlEntity);
		} else {
			this.setDecimalHtmlShort(decimalHtml);
		}
	}

	/**
	 * Gets the list of emoticons associated with the emoji
	 * @return List of all emoticons associated with the emoji
	 */
	public List<String> getEmoticons() {
		return emoticons;
	}

	public void setEmoticons(List<String> emoticons) {
		//for(String emoticon:emoticons) {
			//EmojiManager.addEmoticon(emoticon);
		//}
		this.emoticons = emoticons;
	}

	public String getDecimalSurrogateHtml() {
		return decimalSurrogateHtml;
	}

	public void setDecimalSurrogateHtml(String decimalSurrogateHtml) {
		this.decimalSurrogateHtml = decimalSurrogateHtml;
	}

	public String getDecimalHtmlShort() {
		return decimalHtmlShort;
	}

	public void setDecimalHtmlShort(String decimalHtmlShort) {
		this.decimalHtmlShort = decimalHtmlShort;
	}

	public String getHexHtmlShort() {
		return hexHtmlShort;
	}

	public void setHexHtmlShort(String hexHtmlShort) {
		this.hexHtmlShort = hexHtmlShort;
	}


	/**
	 * Describe the kinds of special objects contained in this Parcelable
	 * instance's marshaled representation. For example, if the object will
	 * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
	 * the return value of this method must include the
	 * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
	 *
	 * @return a bitmask indicating the set of special object types marshaled
	 * by this Parcelable object instance.
	 * @see #CONTENTS_FILE_DESCRIPTOR
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Flatten this object in to a Parcel.
	 *
	 * @param dest  The Parcel in which the object should be written.
	 * @param flags Additional flags about how the object should be written.
	 *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(emoji);
		dest.writeStringList(aliases);
		dest.writeStringList(emoticons);
		dest.writeStringList(tags);
		dest.writeString(hexHtml);
		dest.writeString(decimalHtml);
		dest.writeString(decimalHtmlShort);
		dest.writeString(hexHtmlShort);
		dest.writeString(decimalSurrogateHtml);
	}
}
