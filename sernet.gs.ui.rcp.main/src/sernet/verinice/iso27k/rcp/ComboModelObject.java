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

import java.text.Collator;

/**
 * Object in a {@link ComboModel}
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
public class ComboModelObject<T> implements Comparable<ComboModelObject<T>> {

	private T object;

	private String label;
	
	private Collator collator = Collator.getInstance();
	
	public ComboModelObject(T object, String label) {
		super();
		this.object = object;
		this.label = label;
	}
	
	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((object == null) ? 0 : object.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj){
			return true;
		}
		if (obj == null){
			return false;
		}
		// FIXME ak this will not work when comparing proxies: 
		if (getClass() != obj.getClass()){
			return false;
		}
		ComboModelObject other = (ComboModelObject) obj;
		if (object == null) {
			if (other.object != null){
				return false;
			}
		} else if (!object.equals(other.object)){
			return false;
		}
		return true;
	}

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ComboModelObject<T> o) {
        int result = 1; // this is greater
        if(this.getLabel()!=null) {
            result = -1; // this is less
            if(o!=null && o.getLabel()!=null) {
                result = collator.compare(this.getLabel(), o.getLabel());
            }
        }
        return result;
    }
	
	
}
