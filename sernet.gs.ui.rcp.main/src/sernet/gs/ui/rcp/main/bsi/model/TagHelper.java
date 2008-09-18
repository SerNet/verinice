package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TagHelper {

	public static Collection<String> getTags(String simpleValue) {
		String[] split = simpleValue.split("[, ]+");
		return removeEmptyTags(Arrays.asList(split));
	}

	private static Collection<String> removeEmptyTags(List<String> tags) {
		for (String tag : tags) {
			if (tag.length() < 1 || tag.equals(" "))
				tags.remove(tag);
		}
		return tags;
	}

}
