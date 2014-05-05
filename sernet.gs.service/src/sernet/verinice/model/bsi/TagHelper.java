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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public final class TagHelper {
	
	private static final Pattern PATTERN = Pattern.compile("[, ]+");
	
	private TagHelper(){}

	public static Collection<String> getTags(String simpleValue) {
		String[] split = simpleValue.split("[, ]+"); //$NON-NLS-1$
		return removeEmptyTags(Arrays.asList(split));
	}

	private static Collection<String> removeEmptyTags(List<String> tags) {
		ArrayList<String> result = new ArrayList<String>(tags.size());
		for (String tag : tags) {
			if ( ! (tag.length() < 1 || tag.equals(" ")) ){ //$NON-NLS-1$
				result.add(tag);
			}
		}
		return result;
	}

	/**
	 * Takes a comma separated string of tag value (e.g. "foo, bar")
	 * and puts each tag into the given set.
	 * 
	 * <p>It is expected that the set is a {@link HashSet} since you
	 * usually want distinct values.</p>
	 * 
	 * @param set
	 * @param simpleValue
	 */
	public static void putInTags(Set<String> set, String simpleValue)
	{
		// TODO rschuster: The regex could be refined to not return
		// empty value.
	    if(simpleValue!=null) {
    		String[] split = PATTERN.split(simpleValue); //$NON-NLS-1$
    		for (String tag : split) {
    			if ( ! (tag.length() < 1 || tag.equals(" ")) ){ //$NON-NLS-1$
    				set.add(tag);
    			}
    		}
	    }
	}
}
