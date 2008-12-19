package sernet.hui.swt.widgets.URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class URLUtil {
	
	private static Pattern pattern = Pattern.compile("<a href=\"(.*)\">(.*)</a>");


	public static String getHref(String url) {
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);

		}
		return "";
	}
	
	public static String getName(String url) {
		Matcher matcher = pattern.matcher(url);
		if (matcher.find()) {
			return matcher.group(2);

		}
		return "";
	}
	
	
}
