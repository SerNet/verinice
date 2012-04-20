/**
 * License Agreement.
 *
 *  JBoss RichFaces - Ajax4jsf Component Library
 *
 * Copyright (C) 2007  Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.richfaces.component;

import java.util.Iterator;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.PhaseId;

import org.ajax4jsf.component.AjaxActionComponent;
import org.ajax4jsf.component.AjaxComponent;
import org.ajax4jsf.component.IterationStateHolder;
import org.ajax4jsf.event.AjaxSource;
import org.richfaces.component.util.MessageUtil;
import org.richfaces.event.SimpleTogglePanelSwitchEvent;


/**
 * JSF component class
 */
public abstract class UISimpleTogglePanel extends AjaxActionComponent implements AjaxComponent, AjaxSource, ActionSource, IterationStateHolder
//public abstract class UISimpleTogglePanel extends  UIInput implements AjaxComponent, AjaxSource, ActionSource
{

	public static final String COMPONENT_FAMILY = "javax.faces.Command";
    public static final String SERVER_SWITCH_TYPE = "server";
    public static final String CLIENT_SWITCH_TYPE = "client";
    public static final String AJAX_SWITCH_TYPE = "ajax";
    
    @Deprecated
    public static final boolean COLLAPSED = false;
    @Deprecated
    public static final boolean EXPANDED = true;
    
//    private transient Boolean openedSet = null;

    private transient Boolean wasOpened = Boolean.FALSE; 

    protected void setWasOpened(Boolean wasOpened) {
    	this.wasOpened = wasOpened;
    }
    
    protected boolean isWasOpened() {
        // Fix of this problem: https://community.jboss.org/message/58403
    	return (wasOpened!=null) ? wasOpened : false;
    }
    
    public abstract void setSwitchType(String switchType);

    public abstract String getSwitchType();

    public void setOpened(boolean opened) {
    	setValue(new Boolean(opened).toString());
    }

    public boolean isOpened() {
    	Object value = getValue();
    	if (value instanceof Boolean) {
    		return ((Boolean)value).booleanValue();
    	} else if (value instanceof String) {
			String s = (String) value;
			return Boolean.parseBoolean(s);
		}
    	return true;
    }

    public boolean getRendersChildren() {
        return true;
    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    public void processDecodes(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        setWasOpened(Boolean.valueOf(CLIENT_SWITCH_TYPE.equals(getSwitchType()) || isOpened()));
        
        if (isWasOpened()) {
            // Process all facets and children of this component
            Iterator<UIComponent> kids = getFacetsAndChildren();
            while (kids.hasNext()) {
                UIComponent kid = kids.next();
                kid.processDecodes(context);
            }
        }
        
        // Process this component itself
        try {
            decode(context);
        } catch (RuntimeException e) {
            context.renderResponse();
            throw e;
        }

    }

    /**
     * @throws NullPointerException {@inheritDoc}
     */
    public void processValidators(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }

        if (isWasOpened()) {
	        // Process all the facets and children of this component
        	Iterator<UIComponent> kids = getFacetsAndChildren();
        	while (kids.hasNext()) {
        		UIComponent kid = kids.next();
        		kid.processValidators(context);
        	}
        }
    }


    /**
     * @throws NullPointerException {@inheritDoc}
     */
    public void processUpdates(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        // Skip processing if our rendered flag is false
        if (!isRendered()) {
            return;
        }
        
        if (isWasOpened()) {
	        // Process all facets and children of this component
        	Iterator<UIComponent> kids = getFacetsAndChildren();
	        while (kids.hasNext()) {
	            UIComponent kid = kids.next();
	            kid.processUpdates(context);
	        }
        }
    }
    
    @Override
    public void queueEvent(FacesEvent event) {
    	if (event instanceof SimpleTogglePanelSwitchEvent && this.equals(event.getComponent())) {
			SimpleTogglePanelSwitchEvent switchEvent = (SimpleTogglePanelSwitchEvent) event;

			if (isImmediate()) {
				setOpened(switchEvent.isOpened());
			}
			
			switchEvent.setPhaseId(PhaseId.UPDATE_MODEL_VALUES);
		}

    	super.queueEvent(event);
    }
    
    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
    	super.broadcast(event);

    	if (event instanceof SimpleTogglePanelSwitchEvent) {
			SimpleTogglePanelSwitchEvent switchEvent = (SimpleTogglePanelSwitchEvent) event;

			setOpened(switchEvent.isOpened());
	        
			try {
	            updateModel();
	        } catch (RuntimeException e) {
	        	getFacesContext().renderResponse();
	        	throw e;
			}
		}
    }
    
    private Object value;
    
    protected Object getLocalValue() {
    	return value;
    }
    
    @Override
    public Object getValue() {
    	if (this.value != null) {
    		return this.value;
    	}
    	
    	ValueExpression ve = getValueExpression("value");
    	if (ve != null) {
        	FacesContext context = getFacesContext();
    		
    		try {
            	return ve.getValue(context.getELContext());
        	} catch (ELException e) {
        		throw new FacesException(e);
    		}
    	}
    	
    	return null;
    }
    
    @Override
    public void setValue(Object value) {
    	this.value = value;
    }
    
    @Override
    public Object saveState(FacesContext context) {
    	Object[] state = new Object[2];
    	
    	state[0] = super.saveState(context);
    	state[1] = this.value;
    	
    	return state;
    }
    
    @Override
    public void restoreState(FacesContext context, Object stateObject) {
    	Object[] state = (Object[]) stateObject;

    	super.restoreState(context, state[0]);
    	this.value = state[1];
    }
    
    public Object getIterationState() {
    	Object[] state = new Object[2];
    	
    	state[0] = this.value;
    	state[1] = this.wasOpened;
    	
    	return state;
    }
    
    public void setIterationState(Object stateObject) {
    	if (stateObject != null) {
    		Object[] state = (Object[]) stateObject;
    		
        	this.value = state[0];
        	this.wasOpened = (Boolean) state[1];
    	} else {
    		this.value = null;
    		this.wasOpened = null;
    	}
    }
    
    protected void updateModel() {
    	Object value = getLocalValue();
    	if (value != null) {
        	ValueExpression ve = getValueExpression("value");
        	FacesContext context = getFacesContext();
        	if (ve != null && !ve.isReadOnly(context.getELContext())) {
				try {
            		ve.setValue(context.getELContext(), value);
            		setValue(null);
	            } catch (ELException e) {
	                String messageStr = e.getMessage();
	                Throwable result = e.getCause();
	                while (null != result &&
	                     result.getClass().isAssignableFrom(ELException.class)) {
	                    messageStr = result.getMessage();
	                    result = result.getCause();
	                }
	                FacesMessage message;
	                if (null == messageStr) {
	                    message =
	                    	//not an UIInput, but constant is fine
	                    	MessageUtil.getMessage(context, UIInput.UPDATE_MESSAGE_ID, new Object[] {
	                    		MessageUtil.getLabel(context, this) 
	                    	});
	                } else {
	                    message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
	                                               messageStr,
	                                               messageStr);
	                }
	                
	                context.getExternalContext().log(message.getSummary(), result);
	                context.addMessage(getClientId(context), message);
	                context.renderResponse();
	            } catch (IllegalArgumentException e) {
	                FacesMessage message =
	                	MessageUtil.getMessage(context, UIInput.UPDATE_MESSAGE_ID, new Object[] {
	                    		MessageUtil.getLabel(context, this) 
	                    	}); 
	                
	                context.getExternalContext().log(message.getSummary(), e);
	                context.addMessage(getClientId(context), message);
	                context.renderResponse();
	            } catch (Exception e) {
	                FacesMessage message =
	                	MessageUtil.getMessage(context, UIInput.UPDATE_MESSAGE_ID, new Object[] {
	                    		MessageUtil.getLabel(context, this) 
	                    	}); 

	                context.getExternalContext().log(message.getSummary(), e);
	                context.addMessage(getClientId(context), message);
	                context.renderResponse();
	            }
        	}
    	}
    }

}

