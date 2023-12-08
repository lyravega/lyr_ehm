package lyravega.utilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class lyr_tooltipUtilities {
	private static final String limiter = "•";	// ALT+7
	private static final String separator = "◘";	// ALT+8
	private static final Pattern pattern = Pattern.compile(limiter+"(.{8})"+separator+"(.+?)"+limiter);
	private static Matcher matcher;

	public static class colour {
		public static final Color gray = Misc.getGrayColor();
		public static final Color normal = Misc.getTextColor();
		public static final Color highlight = Misc.getHighlightColor();
		public static final Color positive = Misc.getPositiveHighlightColor();
		public static final Color story = Misc.getStoryOptionColor();
		public static final Color storyDarkBright = Misc.getStoryDarkBrigherColor();
		public static final Color negative = Misc.getNegativeHighlightColor();
		public static final Color button = Misc.getButtonTextColor();

		/**
		 * Converts a colour to HEX string
		 * @param clr to convert
		 * @return
		 */
		public static final String convertColourToHexCode(Color clr) {
			return "0x"+Integer.toHexString(clr.getRGB()).substring(2);
		}
	}

	public static class colourizedText {
		private static final String grayedPattern = limiter+colour.convertColourToHexCode(colour.gray)+separator;
		private static final String normalPattern = limiter+colour.convertColourToHexCode(colour.normal)+separator;
		private static final String highlightPattern = limiter+colour.convertColourToHexCode(colour.highlight)+separator;
		private static final String positivePattern = limiter+colour.convertColourToHexCode(colour.positive)+separator;
		private static final String negativePattern = limiter+colour.convertColourToHexCode(colour.negative)+separator;
		private static final String storyPattern = limiter+colour.convertColourToHexCode(colour.story)+separator;
		private static final String storyDarkBrightPattern = limiter+colour.convertColourToHexCode(colour.storyDarkBright)+separator;
		private static final String buttonPattern = limiter+colour.convertColourToHexCode(colour.button)+separator;

		public static final String colouredText(Color c, String s) { return limiter+colour.convertColourToHexCode(c)+separator+s+limiter; }
		public static final String grayText(String s) { return grayedPattern+s+limiter; }
		public static final String normalText(String s) { return normalPattern+s+limiter; }
		public static final String highlightText(String s) { return highlightPattern+s+limiter; }
		public static final String positiveText(String s) { return positivePattern+s+limiter; }
		public static final String negativeText(String s) { return negativePattern+s+limiter; }
		public static final String storyText(String s) { return storyPattern+s+limiter; }
		public static final String storyDarkBrightText(String s) { return storyDarkBrightPattern+s+limiter; }
		public static final String buttonText(String s) { return buttonPattern+s+limiter; }
		public static final String positiveOrNegativeText(boolean b, String s) { return b ? positivePattern+s+limiter : negativePattern+s+limiter; }
	}

	/**
	 * Uses regular expression to automatically generate format, highlight colour array and
	 * highlight string array from a raw string input, and adds it to the passed tooltip as
	 * a para. Aims to streamline tooltip detailing process by feeding a single-liner to a
	 * method rather than having to individually adjust bits and pieces.
	 * <p> The used regex is {@code \((........)\|(.+?)\)} stored in {@link #pattern}. The
	 * whole match will be replaced with {@code %s}. The first match will be decoded using
	 * {@link Color#decode(String)} that expects a RGB in HEX format like {@code 0xFF0000}
	 * and added to the colour array. The second match will be added to the string array.
	 * <p> For example, when a string like {@code "Adding para with (0x00FFFF|colours) and
	 * (0xFF0000|stuff)"} is passed to this method, format with {@code "Some string with %s
	 * and %s"}, a colour array with decoded {@code 0:ColorObj, 1:ColorObj} colours, and a
	 * string array with {@code 0:"colours", 1:"stuff"} will be passed to the API.
	 * @param tooltip to add this para to
	 * @param rawText raw string without any format to be processed, must have no format
	 * @param pad
	 * @return the added para if any further processing is required
	 */
	public static final LabelAPI addColourizedPara(TooltipMakerAPI tooltip, String rawText, float pad) {
		ArrayList<String> replaceList = new ArrayList<String>();
		ArrayList<Color> colourList = new ArrayList<Color>();

		matcher = pattern.matcher(rawText);

		while(matcher.find()) {
			rawText = rawText.replace(matcher.group(0), "%s");
			replaceList.add(matcher.group(2));
			colourList.add(Color.decode(matcher.group(1)));
		}

		return tooltip.addPara(rawText, pad, colourList.toArray(new Color[]{}), replaceList.toArray(new String[]{}));
	}
}
