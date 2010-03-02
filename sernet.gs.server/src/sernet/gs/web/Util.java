/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.web;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

public class Util {

	public static String getMessage(String key) {
		return getMessage(key, null);
	}

	public static String getMessage(String key,Object params[]){
		String text = null;
		Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		ResourceBundle bundle = ResourceBundle.getBundle(
				ToDoBean.BOUNDLE_NAME, 
				locale);	
		try{
			text = bundle.getString(key);
		} catch(MissingResourceException e){
			text = "? " + key + " ?";
		}
		
		if(params != null){
			MessageFormat mf = new MessageFormat(text, locale);
			text = mf.format(params, new StringBuffer(), null).toString();
		}	
		return text;
	}

	public static void english() {
		FacesContext context = FacesContext.getCurrentInstance();
	    context.getViewRoot().setLocale(Locale.ENGLISH);
	}

	public static void german() {
		FacesContext context = FacesContext.getCurrentInstance();
	    context.getViewRoot().setLocale(Locale.GERMAN);
	}

	static void addInfo(String componentId, String text ) {
		addMessage(componentId, text, FacesMessage.SEVERITY_INFO );
	}

	static void addError(String componentId, String text ) {
		addMessage(componentId, text, FacesMessage.SEVERITY_ERROR );
	}

	private static void addMessage(String componentId, String text, Severity severity ) {
		 FacesMessage message = new FacesMessage(severity, text, null);
	     FacesContext context = FacesContext.getCurrentInstance();
	     UIComponent component = findComponent(context.getViewRoot(), componentId);
	     context.addMessage(component.getClientId(context), message);
	
	}

	private static UIComponent findComponent(UIComponent parent, String id) {
		UIComponent component = null;
		if (id.equals(parent.getId())) {
			component = parent;
		} else {
			Iterator<UIComponent> kids = parent.getFacetsAndChildren();
			while (kids.hasNext()) {
				UIComponent kid = kids.next();
				UIComponent found = findComponent(kid, id);
				if (found != null) {
					component = found;
					break;
				}
			}
		}
		return component;
	}

	protected static ClassLoader getCurrentClassLoader(Object defaultObject){	
		ClassLoader loader = Thread.currentThread().getContextClassLoader();	
		if(loader == null){
			loader = defaultObject.getClass().getClassLoader();
		}
	
		return loader;
	}

}
