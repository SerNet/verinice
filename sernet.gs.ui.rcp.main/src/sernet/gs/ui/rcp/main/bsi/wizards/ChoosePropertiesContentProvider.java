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
package sernet.gs.ui.rcp.main.bsi.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.views.ThreadSafeViewerUpdate;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.IEntityElement;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;

public class ChoosePropertiesContentProvider implements ITreeContentProvider {
	
	public ChoosePropertiesContentProvider() {
	}

	public Object[] getChildren(Object element) {
		if (element instanceof EntityType) {
			EntityType type = (EntityType) element;
			ArrayList<IEntityElement> result = new ArrayList<IEntityElement>();
			result.addAll(type.getPropertyTypes());
			result.addAll(type.getPropertyGroups());
			return (IEntityElement[]) result.toArray(new IEntityElement[result.size()]);
		}
		
		if (element instanceof PropertyGroup) {
			PropertyGroup group = (PropertyGroup) element;
			List<PropertyType> types = group.getPropertyTypes();
			return (PropertyType[]) types.toArray(new PropertyType[types.size()]);
		}
		
		return null;
		
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof EntityType ) {
			EntityType type = (EntityType) element;
			return type.getPropertyTypes().size() > 0
			|| type.getPropertyGroups().size() > 0;
		}
		
		if (element instanceof PropertyGroup) {
			PropertyGroup group = (PropertyGroup) element;
			return group.getPropertyTypes().size() > 0;
		}
		
		return false;
	}

	public Object[] getElements(Object inputElement) {
		Collection list = (Collection) inputElement;
		return (EntityType[]) list.toArray(new EntityType[list.size()]);
	}

	public void dispose() {

	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		(new ThreadSafeViewerUpdate((TreeViewer) viewer)).refresh();
	}
	
	

}
