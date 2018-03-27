/*******************************************************************************
 * Copyright (c) 2016 Daniel Murygin <dm{a}sernet{dot}de>.
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Daniel Murygin <dm{a}sernet{dot}de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.service;

/**
 * This class contains public static methods to handle
 * Strings.
 * 
 * Do not instantiate this class.
 *
 * @author Daniel Murygin <dm{a}sernet{dot}de>
 */
public abstract class StringUtil {

    private StringUtil() {
        // do not instantiate this class, use public static methods
    }
    
    /**
     * Convert a string to a string usable as a file name
     * by replacing all special characters in the string which are
     * illegal in file names.
     * 
     * @param s A String
     * @return Converted string usable as a file name
     */
    public static String convertToFileName(String s) {
        String filename = ""; //$NON-NLS-1$
        if(s!=null) {
            filename = s.replace(' ', '_');
            filename = filename.replace("ä", "\u00E4"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ü", "\u00FC"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ö", "\u00F6"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ä", "\u00C4"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ü", "\u00DC"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("Ö", "\u00D6"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("ß", "\u00DF"); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(":", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("\\", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(";", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("<", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace(">", ""); //$NON-NLS-1$ //$NON-NLS-2$
            filename = filename.replace("|", ""); //$NON-NLS-1$ //$NON-NLS-2$
           }
        return filename;
    }

    /**
     * Truncates a text if it is longer than maxWidth.
     * If text is truncated ellipses ("…") are added in the end.
     * @return the truncated text
     */
    public static String truncate(String text, int maxWidth) {
        if (text == null || text.length() <= maxWidth) {
            return text;
        }
        StringBuilder sb = new StringBuilder(maxWidth);
        sb.append(text, 0, maxWidth - 1).append("…");
        return sb.toString();
    }
}
