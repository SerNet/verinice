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

	private static final String DEFAULT_COMPONENT_ID = "massagePanel";

    public static String getMessage(String key) {
		return getMessage(ToDoBean.BOUNDLE_NAME, key, null);
	}
	
	public static String getMessage(String bundleName,String key) {
        return getMessage(bundleName,key, null);
    }

	public static String getMessage(String bundleName, String key,Object params[]){
	    String text = null;
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        ResourceBundle bundle = ResourceBundle.getBundle(
                bundleName, 
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
	
	public static String getMessage(String key,Object params[]){
	    return Util.getMessage(ToDoBean.BOUNDLE_NAME, key, params);
	}

	public static void english() {
		FacesContext context = FacesContext.getCurrentInstance();
	    context.getViewRoot().setLocale(Locale.ENGLISH);
	}

	public static void german() {
		FacesContext context = FacesContext.getCurrentInstance();
	    context.getViewRoot().setLocale(Locale.GERMAN);
	}

	public static void addInfo(String componentId, String text ) {
		addMessage(componentId, text, FacesMessage.SEVERITY_INFO );
	}

	public static void addError(String componentId, String text ) {
		addMessage(componentId, text, FacesMessage.SEVERITY_ERROR );
	}

	private static void addMessage(String componentId, String text, Severity severity ) {
		 FacesMessage message = new FacesMessage(severity, text, null);
	     FacesContext context = FacesContext.getCurrentInstance();
	     UIComponent component = findComponent(context.getViewRoot(), componentId);
	     if(component==null) {
	         component = findComponent(context.getViewRoot(), DEFAULT_COMPONENT_ID);
	     }
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
