/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin <dm[at]sernet[dot]de>.
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
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel Murygin <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.iso27k.rcp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Data model for selection lists (combo boxes)
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ComboModel<T> {

	private List<ComboModelObject<T>> objectList;
	
	IComboModelLabelProvider<T> labelProvider;
	
	IComboModelFilter<T> filter;
	
	int selectedIndex = -1;
	
	public ComboModel(IComboModelLabelProvider<T> labelProvider) {
		super();
		objectList = new ArrayList<ComboModelObject<T>>();
		this.labelProvider = labelProvider;
	}
	
	public void add(T object) {
		objectList.add(new ComboModelObject<T>(object, labelProvider.getLabel(object)));
	}
	
	public void add(int position, T object) {
        objectList.add(position, new ComboModelObject<T>(object, labelProvider.getLabel(object)));
    }
	
	public void addAll(Collection<T> objectList) {
	    for (T object : objectList) {
	        this.objectList.add(new ComboModelObject<T>(object, labelProvider.getLabel(object)));
        }
	}
	
	public void addNoSelectionObject() {
	    addNoSelectionObject(Messages.ComboModel_0);
	}
	
	public void addNoSelectionObject(String label) {
        this.objectList.add(0, new ComboModelObject<T>(null, label));
    }
	
	public void remove(int i) {
	    remove(getObject(i));
	}
	
	public boolean remove(T object) {
		boolean found = false;
		for (ComboModelObject<T> curent : objectList) {
			if(curent.equals(object)) {
				objectList.remove(curent);
				found = true;
				break;
			}
		}
		return found;
	}
	
	public void removeSelected() {
		remove(selectedIndex);
		if(selectedIndex>0 || getComboModelObjectList().size()==0) {
			selectedIndex--;
		}
	}
	
	public void clear() {
		objectList.clear();
		selectedIndex = -1;
	}
	
	public void sort() {
	    Collections.sort(objectList);
	}
	
    public void sort(Comparator<ComboModelObject<T>> comparator) {
        Collections.sort(objectList, comparator);
    }
	
	public int size() {
	    return objectList.size();
	}
	
	public int getSize() {
        return size();
    }
	
	public boolean isEmpty() {
        return size()<1;
    }
	
	public T getSelectedObject() {
		return (selectedIndex>=0) ? getComboModelObjectListFiltered().get(selectedIndex).getObject() : null;
	}
	
	public String getSelectedLabel() {
		return (selectedIndex>=0) ? getComboModelObjectListFiltered().get(selectedIndex).getLabel() : null;
	}
	
	public T getObject(int i) {
		return getComboModelObjectListFiltered().get(i).getObject();
	}
	
	public String getLabel(int i) {
		return getComboModelObjectListFiltered().get(i).getLabel();
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		if(selectedIndex<getComboModelObjectListFiltered().size()) {
		    this.selectedIndex = selectedIndex;
		}	
	}
	
	public void setSelectedObject(T object) {
	    this.selectedIndex = -1;
		int i = 0;
		for (ComboModelObject<T> current : getComboModelObjectList()) {
			if(object.equals(current.getObject())) {
				this.selectedIndex = i;
				break;
			}
			i++;
		}
	}

	public String[] getLabelArray() {
		List<String> labelList = new LinkedList<String>();
		for (ComboModelObject<T> object : getComboModelObjectListFiltered()) {
		    labelList.add(object.getLabel());
		}
		return labelList.toArray(new String[labelList.size()]);
	}
	
	public List<ComboModelObject<T>> getComboModelObjectListFiltered() {
	    if(filter!=null)  {
	        return createFilteredList();
	    } else {
	        return objectList;
	    }
      
    }
	
    private List<ComboModelObject<T>> createFilteredList() {
        List<ComboModelObject<T>> filteredList = new LinkedList<ComboModelObject<T>>();
        for (ComboModelObject<T> comboModelObject : objectList) {
            if(isVisible(comboModelObject)) {
                filteredList.add(comboModelObject);
            }
        }
        return filteredList;
    }

 
    private boolean isVisible(ComboModelObject<T> comboModelObject) {
        boolean visible = true;
        if(comboModelObject.getObject()!=null) {       
            visible = filter.isVisible(comboModelObject.getObject());
        }
        return visible;
    }

    public List<ComboModelObject<T>> getComboModelObjectList() {
	    return objectList;
	}
	
	public List<T> getObjectList() {
        List<T> list = new LinkedList<T>();
        for (ComboModelObject<T> object : objectList) {
            list.add(object.getObject());
        }
        return list;
    }

    public IComboModelFilter<T> getFilter() {
        return filter;
    }

    public void setFilter(IComboModelFilter<T> filter) {
        this.filter = filter;
    }

	
}
