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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sernet.gs.service.NumericStringComparator;
import sernet.hui.common.connect.EntityType;
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

    public static final String TITLE_COLUMN = "title";

    public static final String HTML_OPEN_TAG = "<strong>";

    public static final String HTML_CLOSING_TAG = "</strong>";

    private final Pattern pattern = Pattern.compile(Occurence.HTML_OPEN_TAG + ".*" + Occurence.HTML_CLOSING_TAG);

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

    /**
     * Stores an text fragment of elastic search response to an corresponding
     * field/propertyId. A fragment may contain several matches, which are all
     * wrapped by {@link #HTML_OPEN_TAG} and {@link #HTML_CLOSING_TAG}.
     * 
     * @param propertypeId
     *            The field/proerytId in which contains the matching string
     * @param translatedPropertyTypeName
     *            Human readable name of the property id (@link
     *            {@link EntityType#getName()}
     * @param textFragment
     *            The fragment provided by elastic search.
     */
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
            sb.append("[").append(getNameOfPropertyId(entry.getKey())).append("]");
            for (String text : entry.getValue()) {
                sb.append(" ").append(text);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Returns all oolumn ids which contains marked matches.
     * 
     */
    public SortedSet<String> getColumnIds() {
        TreeSet<String> treeSet = new TreeSet<String>(new NumericStringComparator());
        treeSet.addAll(entries.keySet());
        return treeSet;
    }

    /**
     * Returns all tokens which are marked as matches by elastic search 
     * 
     */
    public SortedSet<String> getMatches(String columnId) {

        SortedSet<String> fragments = getFragments(columnId);
        SortedSet<String> matches = new TreeSet<String>(new NumericStringComparator());

        for (String fragment : fragments) {
            Set<String> markedTokens = filterMarkedTokens(fragment);
            matches.addAll(stripOfMarkingTags(markedTokens));
        }

        return matches;
    }

    private Set<String> stripOfMarkingTags(Set<String> fragments) {
        Set<String> result = new HashSet<String>();
        for (String fragment : fragments) {
            result.add(fragment.replace(Occurence.HTML_OPEN_TAG, "").replace(Occurence.HTML_CLOSING_TAG, ""));

        }

        return result;
    }

    private Set<String> filterMarkedTokens(String fragment) {

        Matcher matcher = pattern.matcher(fragment);
        Set<String> matches = new HashSet<String>();

        while (matcher.find()) {
            matches.add(matcher.group());
        }

        return matches;
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
     * Returns a sorted set of human readable and translated column names.
     */
    public SortedSet<String> getColumnNames() {
        TreeSet<String> treeSet = new TreeSet<String>(new NumericStringComparator());
        treeSet.addAll(propertyId2PropertyName.values());
        return treeSet;
    }

    /**
     * Returns a sorted set of human readable and translated column names,
     * without the title column.
     */
    public SortedSet<String> getColumnNamesWithoutTitle() {
        Map<String, String> props = new TreeMap<String, String>(propertyId2PropertyName);
        props.remove(TITLE_COLUMN);
        return new TreeSet<String>(props.values());
    }
}