/*******************************************************************************
 * Copyright (c) 2010 Robert Schuster <r.schuster@tarent.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Robert Schuster <r.schuster@tarent.de> - initial API and implementation
 ******************************************************************************/
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
	/* TODO: Things to make this work better
	 * - BIRT has built-in support for objects persisting report generation runs. Use that and make
	 * this class not a bunch of static methods
	 * - intensely use the chapter retrieval commands in the report to find all the TOC entries in
	 * the first place (this reduces the amount of needed runs by one)
	 * - find a way to modify the TOC table's page entries after the TOC table has been generated and
	 * filled (If this is possible then the amount of neccessary runs is decreased by one again) 
	 */

	/** A page number counter that is being updated while the report is being generated.
	 * 
	 * The current value is stored along with a TOC entry.
	 */
	private static int pageNumber = 1;

	/** A counter for the amount of pages the TOC itself requires. This is the only possibility
	 * to correctly calculate the actual page for a TOC entry.
	 */
	private static int tocPageCount = 0;

	/**
	 * The TocHelpers state is preserved during runs of the report generation. It works
	 * slightly different depending on which run it is. This is e.g. the behavior at the end
	 * of each generation process:
	 * <ul>
	 * <li>First run: Collect TOC entries</li>
	 * <li>Second run: Find out TOC page count</li>
	 * <li>Third run: Reset everything</li>
	 * </ul>
	 */
	private static int resetCount = 0;

	/** When the TOC page count has been found out it is copied into this variable for usage
	 * in the final run.
	 */
	private static int finalTocPageCount = 0;

	/** When the TOC items have been found out they are copied into this list to be used in
	 * the final run.
	 */
	private static ArrayList<String> finalItems = new ArrayList<String>();

	/** When the TOC items' page numbers have been found out they are copied into this
	 * list to be used in the final run.
	 */
	private static ArrayList<Integer> finalPages = new ArrayList<Integer>();

	static {
		// Initialize the TOC item list with a helpful message (just in case someone
		// sees a report that was made out of the first run).
		finalItems.add("<em>TOC generation needs 2 more runs!</em>");
		finalPages.add(-1);
	}

	private static ArrayList<String> items = new ArrayList<String>();

	private static ArrayList<Integer> pages = new ArrayList<Integer>();

	/**
	 * Returns the amount of TOC items in the report.
	 * 
	 * Supposed to be correct only in the 2nd and 3rd run.
	 * 
	 * @return
	 */
	public static int getTocCount() {
		return finalItems.size();
	}

	/**
	 * Adds a TOC item to the dataset.
	 * 
	 * <p>The <code>depth</code> value can be used for indentation.</p>
	 * 
	 * @param item
	 * @param depth
	 */
	public static void addTocItem(String item, int depth) {
		StringBuilder sb = new StringBuilder(depth + item.length());
		for (int i = 0; i < depth; i++)
			sb.append("_");
		sb.append(item);

		items.add(sb.toString());
		pages.add(pageNumber);
	}

	/**
	 * Retrieves a TOC item with the specified index.
	 * 
	 * <p>Note: Proper values (= those that reflect the actual report content) is
	 * only supposed to be available in the 2nd and 3rd run.</p>
	 * 
	 * @param i
	 * @return
	 */
	public static String getTocItem(int i) {
		return finalItems.get(i);
	}

	/**
	 * Retrieves a TOC item's page number.
	 * 
	 * <p>Warning: The value is supposed to be correct <em>only</em> in the 3rd run.</p>
	 * 
	 * @param i
	 * @return
	 */
	public static Integer getTocItemPage(int i) {
		return (resetCount == 2) ? finalPages.get(i) + finalTocPageCount : -1;
	}

	/**
	 * Method to be called when one report generation run starts.
	 * 
	 * <p>It resets various internal counters.</p>
	 */
	public static void startRun() {
		pageNumber = 1;
		tocPageCount = 0;
	}

	/**
	 * Method to be called when one report generation run ends.
	 * 
	 * <p>Changes the {@link TocHelper}'s internal state.</p>
	 */
	public static void endRun() {
		++resetCount;
		switch (resetCount) {
		case 1:
			// End of first run:
			// Clear the example TOC items and use the collected ones instead.
			//
			// It is neccessary to provide the correct TOC entries because otherwise
			// the 2nd step could not determine the amount of pages the TOC would require
			// itself.
			finalItems.clear();
			finalPages.clear();
			
			finalItems.addAll(items);
			finalPages.addAll(pages);
			
			items.clear();
			pages.clear();

			break;
		case 2:
			// End of 2nd run:
			// Refresh the whole dataset
			// Note: Actually it would only be neccessary to refresh the page numbers. However
			// with random data being used for the report the actual amount of TOC entries could
			// differ between the 1st and 2nd run. 
			finalItems.clear();
			finalPages.clear();
			
			finalItems.addAll(items);
			finalPages.addAll(pages);

			finalTocPageCount = tocPageCount;

			break;
		case 3:
			// End of 3rd run:
			// Clear the dataset (and put example data into it).
			finalItems.clear();
			finalPages.clear();
			
			finalItems.add("<em>TOC generation needs 2 more runs!</em>");
			finalPages.add(-1);

			items.clear();
			pages.clear();

			// Makes the process start over.
			resetCount = 0;
			break;
		}

	}

	/**
	 * Increases the page count. This method should be called whenever a general page break
	 * happens. (E.g. override the document's "onPageBreak()" method.)
	 */
	public static void increasePageNumber() {
		pageNumber++;
	}

	/**
	 * Increases the TOC page count. This method should be called whenever a page break of the table
	 * that resembles the TOC happens. (E.g. override the table's "onPageBreak()" method.)
	 */
	public static void increaseTocPageCount() {
		tocPageCount++;
	}

}
