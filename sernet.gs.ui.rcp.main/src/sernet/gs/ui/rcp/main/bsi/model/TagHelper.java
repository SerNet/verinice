/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TagHelper {

	public static Collection<String> getTags(String simpleValue) {
		String[] split = simpleValue.split("[, ]+"); //$NON-NLS-1$
		return removeEmptyTags(Arrays.asList(split));
	}

	private static Collection<String> removeEmptyTags(List<String> tags) {
		ArrayList<String> result = new ArrayList<String>(tags.size());
		for (String tag : tags) {
			if ( ! (tag.length() < 1 || tag.equals(" ")) ) //$NON-NLS-1$
				result.add(tag);
		}
		return result;
	}

}
