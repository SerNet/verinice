/*******************************************************************************
 * Copyright (c) 2015 Benjamin Weißenfels.
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
 *     Benjamin Weißenfels <bw[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.model.search;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.connect.PropertyType;

/**
 *
 * Stores information about the occurences and highlighted fields of a verinice
 * search result.
 *
 * @author Benjamin Weißenfels <bw[at]sernet[dot]de>
 */
@SuppressWarnings("serial")
public class Occurence implements Serializable {

    public static final String HTML_OPEN_TAG = "<strong>";

    public static final String HTML_CLOSING_TAG = "</strong>";

    /**
     * The property id (@link {@link PropertyType#getId()} of the matching
     * result is the key, which comes from the HUITypeFactory.
     */
    Map<String, SortedSet<String>> entries;

    /**
     * Maps the property id (@link {@link PropertyType#getId()} to the
     * translated and human readable name.
     */
    Map<String, String> propertyId2PropertyName;

    public Occurence() {
        entries = new HashMap<String, SortedSet<String>>();
        propertyId2PropertyName = new HashMap<String, String>();
    }

    public void addFragment(String propertypeId, String translatedPropertyTypeName, String textFragment) {
        if (!entries.containsKey(propertypeId)) {
            entries.put(propertypeId, new TreeSet<String>(new NumericStringComparator()));
        }

        entries.get(propertypeId).add(textFragment);
        propertyId2PropertyName.put(propertypeId, translatedPropertyTypeName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, SortedSet<String>> entry : entries.entrySet()) {
            sb.append("[" + entry.getKey() + "]");
            for (String text : entry.getValue()) {
                sb.append(" ");
                sb.append(text);
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public SortedSet<String> getColumnIds() {
        TreeSet<String> treeSet = new TreeSet<String>(new NumericStringComparator());
        treeSet.addAll(entries.keySet());
        return treeSet;
    }

    /**
     * Returns all text fragments for a column. The matching substring is
     * highlighted by an html tag.
     *
     * <p>
     * Actually this does not work. Sadly the whole field is always wrapped,
     * which does not make sense.
     * </p>
     *
     * @param columnId
     *            the id (mostly this comes form {@link PropertyType#getId()})
     * @return A string wrapped by an html string
     */
    public SortedSet<String> getFragments(String columnId) {
        if (entries.containsKey(columnId)) {
            return entries.get(columnId);
        } else {
            return new TreeSet<String>(new NumericStringComparator());
        }
    }

    public String getNameOfPropertyId(String propertyId) {

        if (!propertyId2PropertyName.containsKey(propertyId)) {
            return "";
        }

        return propertyId2PropertyName.get(propertyId);
    }

    /**
     * Returns a sorted set of human readable and translated column names
     */
    public SortedSet<String> getColumnNames() {
        TreeSet<String> treeSet = new TreeSet<String>(new NumericStringComparator());
        treeSet.addAll(propertyId2PropertyName.values());
        return treeSet;
    }
}