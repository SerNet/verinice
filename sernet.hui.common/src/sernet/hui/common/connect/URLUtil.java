/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.hui.common.connect;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to handle HTML URLs (links).
 *
 * @author Alexander Koderman <ak[at]sernet[dot]de>
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class URLUtil {
	
	private static Pattern pattern = Pattern.compile("<a href=\"(.*)\">(.*)</a>");
	
	/**
	 * Do not instantiate this class use public static methods.
	 */
	private URLUtil() {
        super();
    }

    /**
	 * @param aHtml A link in HTML format
	 * @return The value of the href parameter
	 */
	public static String getHref(String aHtml) {
		Matcher matcher = pattern.matcher(aHtml);
		if (matcher.find()) {
			return matcher.group(1);

		}
		return "";
	}
	
	/**
	 * @param aHtml A link in HTML format
     * @return The text in the HTML a element
	 */
	public static String getName(String aHtml) {
		Matcher matcher = pattern.matcher(aHtml);
		if (matcher.find()) {
			return matcher.group(2);

		}
		return "";
	}
	
    /**
     * Creates a link in this format    :
     * =HYPERLINK("<URL>";"<TITLE>")
     * 
     * e.g.:
     * =HYPERLINK("http://www.verinice.org";"verinice")
     * 
     * The link format is used in spreadsheet applications
     * like Excel or LibreOffice calc.
     * See: http://stackoverflow.com/questions/6563091/can-excel-interpret-the-urls-in-my-csv-as-hyperlinks
     * 
     * @param aHtml A link in HTML format
     * @return A link in this format: =HYPERLINK("<URL>";"<TITLE>")
     */
    public static String createLinkForSpreadsheet(String aHtml) {
        return URLUtil.createLinkForSpreadsheet(URLUtil.getHref(aHtml), URLUtil.getName(aHtml));
    }
	
    /**
     * Creates a link in this format    :
     * =HYPERLINK("<URL>";"<TITLE>")
     * 
     * e.g.:
     * =HYPERLINK("http://www.verinice.org";"verinice")
     * 
     * The link format is used in spreadsheet applications
     * like Excel or LibreOffice calc.
     * See: http://stackoverflow.com/questions/6563091/can-excel-interpret-the-urls-in-my-csv-as-hyperlinks
     * 
     * @param value A link in HTML format
     * @return A link in this format: =HYPERLINK("<URL>";"<TITLE>")
     */
    public static String createLinkForSpreadsheet(String url, String title) {
        StringBuilder sb = new StringBuilder();
        sb.append("=HYPERLINK(\"");
        sb.append(url);
        sb.append("\";\"");
        sb.append(title);
        sb.append("\")");
        String getLinkForSpreadsheet = sb.toString();
        return getLinkForSpreadsheet;
    }
	
	
}
