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
package sernet.verinice.model.bsi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TagHelper {

    private static final Pattern PATTERN = Pattern.compile("[^, ]+"); //$NON-NLS-1$

    public static Collection<String> getTags(String simpleValue) {
        List<String> strings = new LinkedList<>();
        addTagsToCollection(simpleValue, strings);
        return Collections.unmodifiableList(strings);
    }

    /**
     * Takes a comma separated string of tag value (e.g. "foo, bar") and puts
     * each tag into the given set.
     * 
     * <p>
     * It is expected that the set is a {@link HashSet} since you usually want
     * distinct values.
     * </p>
     * 
     * @param set
     * @param simpleValue
     */
    public static void putInTags(Set<String> set, String simpleValue) {
        if (simpleValue != null) {
            addTagsToCollection(simpleValue, set);
        }
    }

    private static void addTagsToCollection(String simpleValue, Collection<String> strings) {
        Matcher m = PATTERN.matcher(simpleValue);
        while (m.find()) {
            strings.add(m.group());
        }
    }

    private TagHelper() {
    }

}
