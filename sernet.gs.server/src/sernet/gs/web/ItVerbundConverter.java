/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
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
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.web;

import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import sernet.gs.service.ServerInitializer;

public class ItVerbundConverter implements Converter {

	private AssetNavigationBean toDoBean;
	
	public ItVerbundConverter(AssetNavigationBean toDoBean) {
		super();
		this.toDoBean = toDoBean;
	}

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String text) {
		Object value = null;
		if(toDoBean!=null && text!=null) {
			ServerInitializer.inheritVeriniceContextState();
			List<ItVerbundWrapper> itVerbundList = toDoBean.getItVerbundList();
			if(itVerbundList!=null) {
				for (Iterator<ItVerbundWrapper> iterator = itVerbundList.iterator(); iterator.hasNext();) {
				    ItVerbundWrapper itVerbundWrapper = iterator.next();
					if(text.equals(itVerbundWrapper.getItVerbund().getTitle())) {
						value = itVerbundWrapper.getItVerbund();
						break;
					}
				}
			}
		}
		return value;
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
		ServerInitializer.inheritVeriniceContextState();
		ItVerbundWrapper itVerbundWrapper = (ItVerbundWrapper) value;
		return (itVerbundWrapper==null) ? null : itVerbundWrapper.getItVerbund().getTitle();
	}

}
