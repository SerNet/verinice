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
import java.util.Collections;
import java.util.List;

/**
 * Data model for selection lists (combo boxes)
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ComboModel<T> {

	private List<ComboModelObject<T>> objectList;
	
	ComboModelLabelProvider<T> labelProvider;
	
	int selectedIndex = -1;
	
	public ComboModel(ComboModelLabelProvider<T> labelProvider) {
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
	
	public void addAll(List<T> objectList) {
	    objectList.addAll(objectList);
	}
	
	public void remove(int i) {
		objectList.remove(i);
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
		if(selectedIndex>0 || objectList.size()==0) {
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
		return (selectedIndex>=0) ? objectList.get(selectedIndex).getObject() : null;
	}
	
	public String getSelectedLabel() {
		return (selectedIndex>=0) ? objectList.get(selectedIndex).getLabel() : null;
	}
	
	public T getObject(int i) {
		return objectList.get(i).getObject();
	}
	
	public String getLabel(int i) {
		return objectList.get(i).getLabel();
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		if(selectedIndex>=objectList.size()) {
			throw new IllegalArgumentException("Selected index: " + selectedIndex + " is highter or equal than number of objects: " + objectList.size());
		}
		this.selectedIndex = selectedIndex;
	}
	
	public void setSelectedObject(T object) {
		int i = 0;
		for (ComboModelObject<T> current : objectList) {
			if(object.equals(current.getObject())) {
				this.selectedIndex = i;
				break;
			}
			i++;
		}
	}

	/**
	 * @return
	 */
	public String[] getLabelArray() {
		String[] array = new String[objectList.size()];
		int i = 0;
		for (ComboModelObject<T> object : objectList) {
			array[i]=object.getLabel();
			i++;
		}
		return array;
	}

	
}
