/*******************************************************************************
 * Copyright (c) 2012 Daniel Murygin.
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
package sernet.verinice.rcp.tree;

import org.apache.log4j.Logger;

import sernet.verinice.model.common.CnATreeElement;

/**
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 *
 */
public abstract class ElementChecker {
    
    private static final Logger LOG = Logger.getLogger(ElementChecker.class);
    
    public static boolean checkNull(CacheObject e) {
        return e!=null && e.getElement()!=null && e.getElement().getUuid()!=null;
    }
    
    public static void logIfNull(CacheObject e, String message) {
        if(e==null) {
            LOG.warn("Object is null. " + message);
        } else if(e.getElement()==null) {
            LOG.warn("Element in object is null. " + message);
        } else if(e.getElement().getUuid()==null) {
            LOG.warn("UUIF of object is null. " + message);
        }
    }
    
    /**
     * Checks if parent is initialized.
     * 
     * If parent is not initialized an warning is logged.
     * Hibernates {@link LazyInitializationException} is catched. This method
     * never throws a exception.
     * 
     * @param cnATreeElement A CnATreeElement
     */
    public static  void checkParent(CnATreeElement element) {
       if(element==null) {
           return;
       }
       try {
           element.getParent();
       } catch( Exception e ) {
           LOG.warn("Parent of element is not initialized.", e);
       }      
    }
    
    /**
     * Checks if children-set is initialized.
     * 
     * If children-set is not initialized an warning is logged.
     * Hibernates {@link LazyInitializationException} is catched. This method
     * never throws a exception.
     * 
     * @param cnATreeElement A CnATreeElement
     */
    public static void checkChildrenSet(CnATreeElement element) {
       if(element==null) {
           return;
       }
       try {
           element.getChildren();
       } catch( Exception e ) {
           LOG.warn("Children-set of element is not initialized.", e);
       }      
    }
}
