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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.model.DefaultMenuModel;
import org.primefaces.model.MenuModel;

import sernet.gs.service.SecurityException;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.web.Util;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementComparator;
import sernet.verinice.model.common.ITitleAdaptor;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.IISO27kRoot;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.rcp.tree.ElementManager;
import sernet.verinice.service.commands.RemoveElement;
import sernet.verinice.service.iso27k.LoadModel;

/**
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class TreeBean implements IElementListener {

    private static final Logger LOG = Logger.getLogger(TreeBean.class);
    
    public static final String BOUNDLE_NAME = "sernet.verinice.web.TreeMessages"; //$NON-NLS-1$
    
    private static final ElementComparator<CnATreeElement> COMPARATOR = new ElementComparator<CnATreeElement>(new ITitleAdaptor<CnATreeElement>() {
        @Override
        public String getTitle(CnATreeElement element) {
            return element.getTitle();
        }
    });
    
    private static final int MAX_BREADCRUMB_SIZE = 4;
    
    private EditBean editBean;
    
    private AuthBean authBean;
    
    private CnATreeElement element;

    private ElementInformation elementInformation;
    
    private List<ElementInformation> children;
    
    private ElementManager manager;

    private MenuModel menuModel;
    
    private String pathId;
    
    private List<CnATreeElement> path = new LinkedList<CnATreeElement>();
    
    private List<IActionHandler> handlers;
    
    private boolean newVisible = false;
    
    public TreeBean() {
        super();
        manager = new ElementManager();
    }
    
    public CnATreeElement getElement() {
        if(element==null) {
            init();
        }
        return element;
    }

    public void setElement(CnATreeElement element) {     
        this.element = element;
        if(isGroup()) {
            loadChildren();
            createMenuModel();          
        } else {
            setHandlers(new ArrayList<IActionHandler>());
        }
        createHandlers();
    }

    private void loadChildren() {
        CnATreeElement[] elementArray = manager.getChildren(this.element);
        Arrays.sort(elementArray, COMPARATOR);
        children = new ArrayList<ElementInformation>(elementArray.length);
        for (CnATreeElement e : elementArray) {
            children.add(new ElementInformation(e));
        }
    }

    private void createHandlers() {
        List<IActionHandler> handlerList = new LinkedList<IActionHandler>();
        if(getAuthBean().getAddGroup()) {
            IActionHandler handler = HandlerFactory.getGroupHandler(getElement());
            if(handler!=null) {
                handlerList.add(handler);
            }
        }
        if(getAuthBean().getAddElement() && !(getElement() instanceof Organization)) {
            handlerList.addAll(HandlerFactory.getElementHandler(getElement()));
        }
        if(getAuthBean().getAddGroup() && getElement() instanceof Organization) {
            handlerList.addAll(HandlerFactory.getElementHandler(getElement()));
        }
        if(getAuthBean().getAddOrg() && getElement() instanceof ISO27KModel) {
            handlerList.add(HandlerFactory.getOrgHandler(getElement()));
        }
        for (IActionHandler handler : handlerList) {
            handler.addElementListeners(this);
        }
        setHandlers(handlerList);
    }

    private boolean isGroup() {
        return this.element!=null 
                && (this.element instanceof IISO27kGroup || this.element instanceof IISO27kRoot)
                && !(this.element instanceof Asset);
    }
    
    public ElementInformation getElementInformation() {
        return this.elementInformation ;
    }
    
    
    public void setElementInformation(ElementInformation elementInformation) {
        this.elementInformation = elementInformation;
        setElement(elementInformation.getElement());
    }

    public void init() {
        setElement(loadIsoModel());
        getEditBean().setSaveButtonHidden(true);
        getEditBean().clearChangeListener();
        getEditBean().addChangeListener(new IChangeListener() {          
            @Override
            public void elementChanged(CnATreeElement element) {
                manager.elementChanged(element);
                updatePath(element);
            }
        });
        createMenuModel();
        createHandlers();
    }

    protected void updatePath(CnATreeElement element) {
        if(path!=null && path.contains(element)) {
            path.set(path.indexOf(element), element);
            createMenuModel();
            String title = element.getTitle();
            if(title.length()>TaskBean.MAX_TITLE_LENGTH) {
                title = title.substring(0, TaskBean.MAX_TITLE_LENGTH-1) + "...";
            }
            getEditBean().setTitle(title);
        }       
    }

    public void showParent() {
        if(this.element!=null) {
            setElement(this.element.getParent());
            openElement();
        } else {
            init();
        }
    }
    
    private void createMenuModel() {
        menuModel = new DefaultMenuModel();     
        // Add home item     
        menuModel.addMenuItem(MenuItemFactory.createNavigationMenuItem());
        
        path.clear();
        createPath(this.getElement());
        Collections.reverse(path);
        
        Integer breadcrumbSize = calculateBreadcrumbSize();
        for (int i = breadcrumbSize; i < path.size(); i++) {
            CnATreeElement pathElement = path.get(i);
            MenuItem item = MenuItemFactory.createNavigationMenuItem();
            item.setValue(pathElement.getTitle());
            item.getAttributes().put("pathId", i );
            menuModel.addMenuItem(item);
        }
    }

    /**
     * @return
     */
    private Integer calculateBreadcrumbSize() {
        Integer n = 0;
        if(path.size()>MAX_BREADCRUMB_SIZE) {
            n = path.size() - MAX_BREADCRUMB_SIZE;
        }
        return n;
    }
    
    public void selectPath(ActionEvent event) {
        MenuItem selectedMenuItem = (MenuItem) event.getComponent();
        String id = null;
        if(selectedMenuItem.getAttributes().get("pathId")!=null) {
            id = selectedMenuItem.getAttributes().get("pathId").toString();
        }
        if(id!=null) {
            setElement( path.get(Integer.valueOf(id)));
        } else {
            setElement( loadIsoModel() );
        }
    }
    
    public void openElement() {
        try { 
            if(isEditable()) {
                getEditBean().setSaveMessage(Util.getMessage(TreeBean.BOUNDLE_NAME, "elementSaved"));
                getEditBean().setVisibleTags(Arrays.asList(EditBean.TAG_ALL));
                getEditBean().setSaveButtonHidden(true);
                getEditBean().setUuid(getElement().getUuid());
                String title = getElement().getTitle();
                if(title.length()>TaskBean.MAX_TITLE_LENGTH) {
                    title = title.substring(0, TaskBean.MAX_TITLE_LENGTH-1) + "...";
                }
                getEditBean().setTitle(title);
                getEditBean().setTypeId(getElement().getTypeId());
                getEditBean().addNoLabelType(SamtTopic.PROP_DESC);
                getEditBean().init();
                getEditBean().clearActionHandler();
                
                getLinkBean().setSelectedLink(null);
                getLinkBean().setSelectedLinkTargetName(null);
                getLinkBean().setSelectedLinkType(null);
            } else {
                getEditBean().clear();
                getLinkBean().clear();
            }
        } catch (Exception t) {
            LOG.error("Error while opening element", t); //$NON-NLS-1$
            Util.addError("elementTable", Util.getMessage("tree.open.failed")); //$NON-NLS-1$
        } 
    }
    
    public void delete() {
        final String componentElementtable = "elementTable";
        try {
            RemoveElement<CnATreeElement> command = new RemoveElement(getElement());
            command = ServiceFactory.lookupCommandService().executeCommand(command);
            manager.elementRemoved(getElement());
            getChildren().remove(getElementInformation());
            getEditBean().clear();
            getLinkBean().clear();
            if(isGroup()) {
                showParent();
            }
            Util.addInfo(componentElementtable, Util.getMessage(TreeBean.BOUNDLE_NAME, "deleted", new Object[]{getElementInformation().getTitle()}));
        } catch( SecurityException e) {
            LOG.error("SecurityException while deleting element.");
            Util.addError(componentElementtable, Util.getMessage(TreeBean.BOUNDLE_NAME, "delete.failed.security"));
        } catch( Exception e) {
            LOG.error("Error while deleting element.");
            Throwable t = e.getCause();
            if(t instanceof SecurityException) {
                Util.addError(componentElementtable, Util.getMessage(TreeBean.BOUNDLE_NAME, "delete.failed.security"));
            } else {
                Util.addError(componentElementtable, Util.getMessage(TreeBean.BOUNDLE_NAME, "delete.failed"));
            }
        }
    }

    private boolean isEditable() {
        return getElement()!=null 
               && !(getElement() instanceof ISO27KModel)
               && !(getElement() instanceof ImportIsoGroup);
    }

    @Override
    public void elementAdded(CnATreeElement element) {
        manager.elementAdded(element);
        getChildren().add(new ElementInformation(element));
        setNewVisible(false);
    }

    private void createPath(CnATreeElement element) {
        if(!isRoot(element)) {
            path.add(element);
            if(!isTopLevel(element)) {           
                createPath(element.getParent());
            }
        }
    }
    
    public void add() {
        
    }
    
    private boolean isRoot(CnATreeElement element) {
        return element.getTypeId().equals(ISO27KModel.TYPE_ID)
               || element.getTypeId().equals(BSIModel.TYPE_ID);
    }
    
    private boolean isTopLevel(CnATreeElement element) {
        String typeId = element.getTypeId();
        String parentTypeId = "DUMMY";
        if(element.getParent()!=null) {
            parentTypeId = element.getParent().getTypeId();
        }
        return ((typeId.equals(Organization.TYPE_ID) && !parentTypeId.equals(ImportIsoGroup.TYPE_ID)) 
               || (typeId.equals(ITVerbund.TYPE_ID)&& !parentTypeId.equals(ImportBsiGroup.TYPE_ID))
               || typeId.equals(ImportBsiGroup.TYPE_ID)
               || typeId.equals(ImportIsoGroup.TYPE_ID));
    }

    public MenuModel getMenuModel() {
        if(menuModel==null) {
            createMenuModel();
        }
        return menuModel;
    }

    public String getPathId() {
        return pathId;
    }

    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    public List<IActionHandler> getHandlers() {
        if(handlers==null) {
            init();
        }
        return handlers;
    }

    public void setHandlers(List<IActionHandler> handlers) {
        this.handlers = handlers;
    }

    public boolean getNewVisible() {
        return newVisible;
    }
    
    public void toggleNew() {
        this.newVisible = !this.newVisible;
    }
    
    public void showNew() {
        this.newVisible = true;
    }

    public void setNewVisible(boolean newVisible) {
        this.newVisible = newVisible;
    }

    public EditBean getEditBean() {
        return editBean;
    }

    public void setEditBean(EditBean editBean) {
        this.editBean = editBean;
    }
    
    public AuthBean getAuthBean() {
        return authBean;
    }

    public void setAuthBean(AuthBean authBean) {
        this.authBean = authBean;
    }

    public LinkBean getLinkBean() {
        return getEditBean().getLinkBean();
    }

    public List<ElementInformation> getChildren() {
        if(children==null) {
            init();
        }
        return children;
    }
    
    private ISO27KModel loadIsoModel() {
        ISO27KModel model = null;
        try {
            LoadModel loadModel = new LoadModel();
            loadModel = ServiceFactory.lookupCommandService().executeCommand(loadModel);
            model = loadModel.getModel();
            
        } catch(Exception e) {
            LOG.error("Error while loading model", e);
            throw new RuntimeException("Error while loading model", e);
        }
        return model;
    }
    
}
