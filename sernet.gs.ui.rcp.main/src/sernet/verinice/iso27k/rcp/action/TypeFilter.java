/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp.action;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Organization;

/**
 * @author Daniel Murygin <dm@sernet.de>
 * 
 */
public class TypeFilter extends ViewerFilter {

    public static String[] ALL_TYPES = new String[]{"ALL_TYPES","ALL_TYPES"};
    
    private StructuredViewer viewer;
    private Set<String[]> visibleTypeSet = new HashSet<String[]>(1);
    

    public TypeFilter(StructuredViewer viewer) {
        this.viewer = viewer;
        visibleTypeSet.add(ALL_TYPES);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers
     * .Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object o) {
        boolean visible = (visibleTypeSet.contains(ALL_TYPES));
        if (o instanceof CnATreeElement && !visible) {
            CnATreeElement element = (CnATreeElement) o;
            visible = Organization.TYPE_ID.equals(element.getTypeId()) || (contains(visibleTypeSet,element.getTypeId()));
            if(!visible) {
                CnATreeElement withChildren = Retriever.checkRetrieveChildren(element);
                Set<CnATreeElement> children = withChildren.getChildren();
                for (CnATreeElement child : children) {
                    visible = select(viewer, withChildren, child);
                    if(visible) {
                        break;
                    }
                }
            }
        }
        if(!visible && parentElement instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) parentElement;
            visible = (visibleTypeSet.contains(element.getTypeId()));
            if(!visible && element.getParent()!=null) {
                CnATreeElement elementWithParent = Retriever.checkRetrieveParent((CnATreeElement) element);
                visible = checkParent(viewer, elementWithParent.getParent(), elementWithParent);
            }
        }       
        return visible;
    }
    

    
    private boolean checkParent(Viewer viewer, Object parentO, Object o) {
        boolean visible = (visibleTypeSet.contains(ALL_TYPES));
        if (o instanceof CnATreeElement && !visible) {
            CnATreeElement element = (CnATreeElement) o;
            visible =  contains(visibleTypeSet,element.getTypeId());          
        }
        if(!visible && parentO instanceof CnATreeElement) {
            CnATreeElement element = (CnATreeElement) parentO;
            visible = (visibleTypeSet.contains(element.getTypeId()));
            if(!visible && element.getParent()!=null) {
                CnATreeElement elementWithParent = Retriever.checkRetrieveParent((CnATreeElement) element);        
                visible = checkParent(viewer, elementWithParent.getParent(), elementWithParent);
            }
        }       
        return visible;
    }

    /**
     * @param visibleTypeSet2
     * @param typeId
     * @return
     */
    private boolean contains(Set<String[]> visibleTypeSet, String typeId) {
        boolean result = false;
        for (Iterator<String[]> iterator = visibleTypeSet.iterator(); iterator.hasNext() && !result;) {
            String[] strings = iterator.next();
            result = strings[0].equals(typeId) || strings[1].equals(typeId);          
        }
        return result;
    }

    public void addType(String[] type) {
        if(visibleTypeSet.contains(ALL_TYPES)) {
            visibleTypeSet.remove(ALL_TYPES);
            viewer.addFilter(this);
        }
        visibleTypeSet.add(type);
    }
    
    public void addAllTypes() {
        visibleTypeSet.clear();
        visibleTypeSet.add(ALL_TYPES);
        viewer.removeFilter(this);
    }
    
    public void reset() {
        addAllTypes();
    }
    
    public Set<String[]> getVisibleTypeSet() {
        return visibleTypeSet;
    }

    public void setVisibleTypeSet(Set<String[]> visibleTypeSet) {
        this.visibleTypeSet = visibleTypeSet;
        viewer.addFilter(this);
        viewer.refresh();
    }

}
