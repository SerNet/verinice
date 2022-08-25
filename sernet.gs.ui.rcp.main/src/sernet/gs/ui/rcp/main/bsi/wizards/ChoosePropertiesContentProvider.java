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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import sernet.gs.ui.rcp.main.bsi.views.ThreadSafeViewerUpdate;
import sernet.hui.common.connect.EntityType;
import sernet.hui.common.connect.IEntityElement;
import sernet.hui.common.connect.PropertyGroup;
import sernet.hui.common.connect.PropertyType;

public class ChoosePropertiesContentProvider implements ITreeContentProvider {

    public Object[] getChildren(Object element) {
        if (element instanceof EntityType) {
            EntityType type = (EntityType) element;
            ArrayList<IEntityElement> result = new ArrayList<>(
                    type.getPropertyTypes().size() + type.getPropertyGroups().size());
            result.addAll(type.getPropertyTypes());
            result.addAll(type.getPropertyGroups());
            return result.toArray(new IEntityElement[result.size()]);
        }

        if (element instanceof PropertyGroup) {
            PropertyGroup group = (PropertyGroup) element;
            Collection<PropertyType> types = group.getPropertyTypes();
            return types.toArray(new PropertyType[types.size()]);
        }

        return null;

    }

    public Object getParent(Object element) {
        return null;
    }

    public boolean hasChildren(Object element) {
        if (element instanceof EntityType) {
            EntityType type = (EntityType) element;
            return !type.getPropertyTypes().isEmpty() || !type.getPropertyGroups().isEmpty();
        }

        if (element instanceof PropertyGroup) {
            PropertyGroup group = (PropertyGroup) element;
            return !group.getPropertyTypes().isEmpty();
        }

        return false;
    }

    public Object[] getElements(Object inputElement) {
        Collection list = (Collection) inputElement;
        return list.toArray(new EntityType[list.size()]);
    }

    @Override
    public void dispose() {
        // nothing to do
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        (new ThreadSafeViewerUpdate((TreeViewer) viewer)).refresh();
    }

}
