/*******************************************************************************
 * Copyright (c) 2010 Alexander Koderman <ak@sernet.de>.
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
package sernet.gs.ui.rcp.main.bsi.views;

import java.util.HashSet;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.common.model.CnALink;
import sernet.gs.ui.rcp.main.common.model.CnATreeElement;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;

/**
 * @author koderman@sernet.de
 * @version $Rev$ $LastChangedDate$ $LastChangedBy$
 * 
 */
public class RelationViewContentProvider implements IStructuredContentProvider {


	private IRelationTable view;

	public RelationViewContentProvider(IRelationTable view) {
		this.view = view;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		if (newInput instanceof PlaceHolder)
			return;
		CnATreeElement inputElmt = (CnATreeElement) newInput;
		view.setInputElmt(inputElmt);
		view.getViewer().refresh();
	}

	public void dispose() {
	}

	public Object[] getElements(Object obj) {
		if (obj instanceof PlaceHolder) {
			return new Object[] { obj };
		}

		HashSet<CnALink> result = new HashSet<CnALink>();
		result.addAll(view.getInputElmt().getLinksDown());
		result.addAll(view.getInputElmt().getLinksUp());
		return (CnALink[]) result.toArray(new CnALink[result.size()]);
	}
}
