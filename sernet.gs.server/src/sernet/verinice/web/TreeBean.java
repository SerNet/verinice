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

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.MethodExpressionActionListener;

import org.apache.log4j.Logger;
import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.model.DefaultMenuModel;
import org.primefaces.model.MenuModel;

import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.bsi.ITVerbund;
import sernet.verinice.model.bsi.ImportBsiGroup;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.common.ElementComparator;
import sernet.verinice.model.common.ITitleAdaptor;
import sernet.verinice.model.iso27k.Asset;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.Document;
import sernet.verinice.model.iso27k.Evidence;
import sernet.verinice.model.iso27k.Finding;
import sernet.verinice.model.iso27k.IISO27kGroup;
import sernet.verinice.model.iso27k.IISO27kRoot;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.ImportIsoGroup;
import sernet.verinice.model.iso27k.Incident;
import sernet.verinice.model.iso27k.IncidentScenario;
import sernet.verinice.model.iso27k.Interview;
import sernet.verinice.model.iso27k.Organization;
import sernet.verinice.model.iso27k.PersonIso;
import sernet.verinice.model.iso27k.Record;
import sernet.verinice.model.iso27k.Requirement;
import sernet.verinice.model.iso27k.Response;
import sernet.verinice.model.iso27k.Threat;
import sernet.verinice.model.iso27k.Vulnerability;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.rcp.tree.ElementManager;
import sernet.verinice.service.iso27k.LoadModel;

/**
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class TreeBean {

    private static final Logger LOG = Logger.getLogger(TreeBean.class);
    
    private static final ElementComparator<CnATreeElement> COMPARATOR = new ElementComparator<CnATreeElement>(new ITitleAdaptor<CnATreeElement>() {
        @Override
        public String getTitle(CnATreeElement element) {
            return element.getTitle();
        }
    });
    
    private EditBean editBean;
    
    private CnATreeElement element;

    private ElementInformation elementInformation;
    
    private List<ElementInformation> children;
    
    private ElementManager manager;

    private MenuModel menuModel;
    
    private String pathId;
    
    List<CnATreeElement> path = new LinkedList<CnATreeElement>();
    
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
            CnATreeElement[] elementArray = manager.getChildren(this.element);
            Arrays.sort(elementArray, COMPARATOR);
            children = new ArrayList<ElementInformation>(elementArray.length);
            for (CnATreeElement e : elementArray) {
                children.add(new ElementInformation(e));
            }        
        }
        createMenuModel();
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
    }
    
    /**
     * @param element2
     */
    protected void updatePath(CnATreeElement element) {
        if(path!=null && path.contains(element)) {
            path.set(path.indexOf(element), element);
            createMenuModel();
            getEditBean().setTitle(element.getTitle());
        }       
    }

    public void showParent() {
        if(this.element!=null) {
            setElement(this.element.getParent());
        } else {
            init();
        }
    }
    
    /**
     * 
     */
    private void createMenuModel() {
        menuModel = new DefaultMenuModel();
        FacesContext facesCtx = FacesContext.getCurrentInstance();
        ELContext elementCtx = facesCtx.getELContext();
        ExpressionFactory factory = facesCtx.getApplication().getExpressionFactory();
        MethodExpression methodExpression = factory.createMethodExpression(elementCtx, "#{tree.selectPath}", Void.class,new Class[]{ActionEvent.class});
        MethodExpressionActionListener actionListener = new MethodExpressionActionListener(methodExpression);
        
        // home item
        MenuItem item = new MenuItem();
        item.setStyle("padding: 0;");
        item.setUpdate(":tableForm,:navForm");
        item.addActionListener(actionListener);      
        menuModel.addMenuItem(item);
        
        path.clear();
        addPath(this.getElement());
        Collections.reverse(path);
        Integer n = 0;
        for (CnATreeElement element : path) {
            item = new MenuItem();
            item.setValue(element.getTitle());
            item.setStyle("padding: 0;");
            item.setUpdate(":tableForm,:navForm");
            item.addActionListener(actionListener);
            item.getAttributes().put("pathId", n );
            menuModel.addMenuItem(item);
            n++;
        }
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
            getEditBean().setSaveButtonHidden(true);
            getEditBean().setUuid(getElement().getUuid());
            getEditBean().setTitle(getElement().getTitle());
            getEditBean().setTypeId(getElement().getTypeId());
            getEditBean().addNoLabelType(SamtTopic.PROP_DESC);
            getEditBean().init();
            getEditBean().clearActionHandler();
            
            getLinkBean().setSelectedLink(null);
            getLinkBean().setSelectedLinkTargetName(null);
            getLinkBean().setSelectedLinkType(null);
        } catch (Throwable t) {
            LOG.error("Error while opening task", t); //$NON-NLS-1$
        } 
    }

    /**
     * @param element2
     * @param parents
     */
    private void addPath(CnATreeElement element) {
        if(!isRoot(element)) {
            path.add(element);
            if(!isTopLevel(element)) {           
                addPath(element.getParent());
            }
        }
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

    /**
     * @return the menuModel
     */
    public MenuModel getMenuModel() {
        if(menuModel==null) {
            createMenuModel();
        }
        return menuModel;
    }

    /**
     * @return the pathId
     */
    public String getPathId() {
        return pathId;
    }

    /**
     * @param pathId the pathId to set
     */
    public void setPathId(String pathId) {
        this.pathId = pathId;
    }

    /**
     * @return the editBean
     */
    public EditBean getEditBean() {
        return editBean;
    }

    /**
     * @param editBean the editBean to set
     */
    public void setEditBean(EditBean editBean) {
        this.editBean = editBean;
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
