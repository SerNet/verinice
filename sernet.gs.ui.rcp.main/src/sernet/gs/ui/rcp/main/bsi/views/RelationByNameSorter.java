/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak[at]sernet[dot]de>.
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
package sernet.gs.ui.rcp.main.bsi.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;

/**
 * @author koderman[at]sernet[dot]de
 * @version $Rev$ $LastChangedDate$ 
 * $LastChangedBy$
 *
 */
public class RelationByNameSorter extends ViewerSorter {
	
	private String sorterProperty;
	private IRelationTable view;
    private String[] sorterProperties;

	public RelationByNameSorter(IRelationTable view, String... sorterProperties) {
		this.view = view;
		this.sorterProperties = sorterProperties;
	}
	

		public boolean isSorterProperty(Object arg0, String arg1) {
		    for (String prop : sorterProperties) {
                if (arg1.equals(prop))
                    return true;
            }
		    return false;
		}
		
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (o1 == null || o2 == null)
				return 0;
			CnALink link1 = (CnALink) o1;
			CnALink link2 = (CnALink) o2;
			
			String link1UpId   = link1.getDependant().getTypeId();
			String link1DownId = link1.getDependency().getTypeId();
			String link2UpId   = link2.getDependant().getTypeId();
			String link2DownId = link2.getDependency().getTypeId();

			// if we have the same element on one side...
			if (link1UpId.equals(link2UpId)) {
			    // compare if we have a different category on the other side and sort by category first:
			    int compare = link1DownId.compareTo(link2DownId);
			    if (compare != 0)
			        return compare;
			}

			// the same but for reversed sides (since we don't know if were displaying the upward / downward direction:
			if (link1DownId.equals(link2DownId)) {
			    int compare = link1UpId.compareTo(link2UpId);
			    if (compare != 0)
			        return compare;
			}

			// categories are the same, so we sort by name within the category:
			String title1 = CnALink.getRelationObjectTitle(view.getInputElmt(), link1);
			String title2 = CnALink.getRelationObjectTitle(view.getInputElmt(), link2);

			return title1.compareTo(title2);
		}
}
