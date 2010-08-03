package sernet.verinice.report.service.impl;

import java.util.ArrayList;

/**
 * The {@link TocHelper} class simplifies the generation of printable table of
 * contents from BIRT reports.
 * 
 * <p>
 * The class provides simple public and static methods and work around the fact
 * that BIRT (up until 2.6 at least) has no built-in printable TOC feature.
 * </p>
 * 
 * @author Robert Schuster <r.schuster@tarent.de>
 * 
 */
public class TocHelper {

	private static int pageNumber = 1;

	private static int tocPageCount = 0;

	private static int resetCount = 0;

	private static int finalTocPageCount = 0;

	private static ArrayList<String> finalItems = new ArrayList<String>();

	private static ArrayList<Integer> finalPages = new ArrayList<Integer>();

	static {
		finalItems.add("<em>TOC generation needs 2 more runs!</em>");
		finalPages.add(-1);
	}

	private static ArrayList<String> items = new ArrayList<String>();

	private static ArrayList<Integer> pages = new ArrayList<Integer>();

	public static int getTocCount() {
		return finalItems.size();
	}

	public static void addTocItem(String item, int depth) {
		StringBuilder sb = new StringBuilder(depth + item.length());
		for (int i = 0; i < depth; i++)
			sb.append("_");
		sb.append(item);

		items.add(sb.toString());
		pages.add(pageNumber);
	}

	public static String getTocItem(int i) {
		return finalItems.get(i);
	}

	public static Integer getTocPage(int i) {
		return (resetCount == 2) ? finalPages.get(i) + finalTocPageCount : -1;
	}

	public static void resetPages() {
		pageNumber = 1;
		tocPageCount = 0;
	}

	public static void resetToc() {
		++resetCount;
		switch (resetCount) {
		case 1:
			finalItems.clear();
			finalPages.clear();
			
			finalItems.addAll(items);
			finalPages.addAll(pages);
			
			items.clear();
			pages.clear();

			break;
		case 2:
			finalItems.clear();
			finalPages.clear();
			
			finalItems.addAll(items);
			finalPages.addAll(pages);

			finalTocPageCount = tocPageCount;

			break;
		case 3:
			finalItems.clear();
			finalPages.clear();
			
			finalItems.add("<em>TOC generation needs 2 more runs!</em>");
			finalPages.add(-1);

			items.clear();
			pages.clear();

			resetCount = 0;
			break;
		}

	}

	public static void increasePageNumber() {
		pageNumber++;
	}

	public static void increaseTocPageCount() {
		tocPageCount++;
	}

}
