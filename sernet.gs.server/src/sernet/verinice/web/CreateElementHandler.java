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
package sernet.verinice.web;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.web.Util;
import sernet.hui.common.connect.Entity;
import sernet.hui.common.connect.HitroUtil;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.CreateElement;

public class CreateElementHandler implements IActionHandler {

    private static final Logger LOG = Logger.getLogger(CreateElementHandler.class);
    
    private CnATreeElement parent;
    
    private String newElementType;
    
    private List<IElementListener> elementListeners;

    /**
     * @param parent
     * @param newElementType
     */
    public CreateElementHandler(CnATreeElement parent, String newElementType) {
        super();
        this.parent = parent;
        this.newElementType = newElementType;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.web.IActionHandler#execute()
     */
    @Override
    public void execute() {
        try {
            String title = null;
            if(newElementType!=null) {
                // load the localized title via HUITypeFactory from message bundle
                title = HitroUtil.getInstance().getTypeFactory().getMessage(newElementType);
                Entity parentEntity = parent.getEntity();
                Set<CnATreeElement> children = parent.getChildren();
                CnATreeElement grandParent = parent.getParent();
                CreateElement<CnATreeElement> saveCommand = new CreateElement<CnATreeElement>(parent, newElementType, title);
                saveCommand = ServiceFactory.lookupCommandService().executeCommand(saveCommand);
                CnATreeElement newElement = saveCommand.getNewElement();
                children.add(newElement);
                parent.setChildren(children);
                parent.setEntity(parentEntity);
                parent.setParent(grandParent);
                newElement.setParent(parent);
                for (IElementListener listener : getElementListeners()) {
                    listener.elementAdded(newElement);
                }
                Util.addInfo("elementTable", Util.getMessage(TreeBean.BOUNDLE_NAME, "element-created", new Object[]{title} ));
            }
            
        } catch (Exception e) {
            LOG.error("", e);
            Util.addError("CreateElementHandler", Util.getMessage(TreeBean.BOUNDLE_NAME, "createError"));
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.web.IActionHandler#getLabel()
     */
    @Override
    public String getLabel() {
        return "new-" + newElementType + "-label";
    }

    /* (non-Javadoc)
     * @see sernet.verinice.web.IActionHandler#setLabel(java.lang.String)
     */
    @Override
    public void setLabel(String label) {}
        
  
    /* (non-Javadoc)
     * @see sernet.verinice.web.IActionHandler#getIcon()
     */
    @Override
    public String getIcon() {
        String path = Icons.ICONS.get(this.newElementType);
        if(path==null ) {
            path = Icons.FOLDER;
        }
        return path;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.web.IActionHandler#setIcon(java.lang.String)
     */
    @Override
    public void setIcon(String path) {}

    public List<IElementListener> getElementListeners() {
        if(elementListeners==null) {
            elementListeners = new LinkedList<IElementListener>();
        }
        return elementListeners;
    }
    
    public void addElementListeners(IElementListener elementListener) {
        getElementListeners().add(elementListener);
    }

    public void setElementListeners(List<IElementListener> elementListeners) {
        this.elementListeners = elementListeners;
    }
    
}