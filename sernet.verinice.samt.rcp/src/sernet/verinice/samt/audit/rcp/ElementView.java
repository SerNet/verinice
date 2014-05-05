/*******************************************************************************
 * Copyright (c) 2010 Daniel Murygin.
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
package sernet.verinice.samt.audit.rcp;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.service.RetrieveInfo;
import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.bsi.views.TreeViewerCache;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.hui.common.connect.Entity;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.rcp.ISMViewContentProvider;
import sernet.verinice.iso27k.rcp.ISMViewLabelProvider;
import sernet.verinice.iso27k.rcp.ISO27KModelViewUpdate;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.service.Retriever;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.iso27k.Organization;

/**
 * Abstract view with tree viewer to show {@link CnATreeElement}s of specific types
 * and of {@link Group}s which contains these types.
 * 
 * Subclasses implements methods to load the desired elements.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public abstract class ElementView extends ViewPart {

    private static final Logger LOG = Logger.getLogger(ElementView.class);
    
    private static volatile ISO27KModel dummy = null;
    
    protected static synchronized ISO27KModel getDummy() {
        if(dummy==null) {
            dummy = new ISO27KModel();
            dummy.setEntity(new Entity(ISO27KModel.TYPE_ID));
        }
        return dummy;
    }
    
    protected TreeViewer viewer;
    
    protected TreeViewerCache cache = new TreeViewerCache();
    
    protected ISMViewContentProvider contentProvider;
    
    private ISO27KModelViewUpdate modelUpdateListener;
    
    private IModelLoadListener modelLoadListener;
    
    private ISelectionListener selectionListener;
    
    private Action doubleClickAction;
    
    private ICommandService commandService;

    private Integer selectedId;
    
    private Object selection;
    
    protected CnATreeElement selectedGroup;
    
    private CnATreeElement elementToLink;
    
    private Audit selectedAudit;
    
    private Organization selectedOrganization;
    
    private Label textLink, textGroup;

    /**
     * @return {@link CnATreeElement}s to show in this view
     * @throws CommandException
     */
    protected abstract List<? extends CnATreeElement> getElementList() throws CommandException;
    
    /**
     * Loads {@link CnATreeElement}s which are linked to the element with primary key
     * selectedId. After loading elements are shown in the view
     * 
     * @param selectedId primary key of a {@link CnATreeElement}
     * @return Elements linked to primary key selectedId
     * @throws CommandException
     */
    protected abstract List<? extends CnATreeElement> getLinkedElements(int selectedId) throws CommandException;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {
        try {
            initView(parent);
            startInitDataJob();
        } catch (Exception e) {
            LOG.error("Error while creating view", e);  //$NON-NLS-1$
            ExceptionUtil.log(e, Messages.ElementView_1);
        }
        
    }

    protected void initView(Composite parent) {
        GridLayout gridLayoutParent = new GridLayout(1, true);
        parent.setLayout(gridLayoutParent);
        
        Composite compositeInfo = new Composite(parent,SWT.NONE);
        compositeInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout gridLayoutInfo = new GridLayout(2, false);
        compositeInfo.setLayout(gridLayoutInfo);
        
        Label labelLink = new Label(compositeInfo,SWT.NONE);
        labelLink.setText(Messages.ElementView_2);
        labelLink.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        textLink = new Label(compositeInfo,SWT.NONE);
        textLink.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        contentProvider = new ISMViewContentProvider(cache, new ElementViewTreeCommandFactory(), new ElementViewParentLoader());
        
        viewer.setContentProvider(contentProvider);
        
        IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();
        viewer.setLabelProvider(new DecoratingLabelProvider(new ISMViewLabelProvider(cache), workbench.getDecoratorManager()));
        
        // make the viewer a selection provider
        getSite().setSelectionProvider(viewer);
        // listen to other selection provider
        addSelectionListener();
        
        hookContextMenu();
        makeActions();
        addActions();
        fillToolBar();
        hookDndListeners();
    }
    
    /**
     * 
     */
    protected void startInitDataJob() {
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.ElementView_4) {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.ElementView_5, IProgressMonitor.UNKNOWN);
                    initData();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e);  //$NON-NLS-1$
                    status= new Status(Status.ERROR, "sernet.verinice.samt.audit.rcp", Messages.ElementView_8,e);  //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);      
    }

    protected void initData() throws CommandException { 
        if(CnAElementFactory.isIsoModelLoaded()) {
            if (modelUpdateListener == null ) {
                // modellistener should only be created once!
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating modelUpdateListener for ISMView.");  //$NON-NLS-1$
                }
                Activator.inheritVeriniceContextState();
                modelUpdateListener = createISO27KModelViewUpdate();
                CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(modelUpdateListener);
                final List<? extends CnATreeElement> elementList = getElementList();
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {
                        setInput(elementList);
                    }
                });
            }
        } else if(modelLoadListener==null) {
            // model is not loaded yet: add a listener to load data when it's laoded
            modelLoadListener = new IModelLoadListener() {
                public void closed(BSIModel model) {
                    // nothing to do
                }
                public void loaded(BSIModel model) {
                    // nothing to do
                }

                @Override
                public void loaded(ISO27KModel model) {
                    synchronized (modelLoadListener) {
                        startInitDataJob();
                    }
                }          
            };
            CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
        }
    }
    
    private Set<CnATreeElement> editElementSet = new HashSet<CnATreeElement>();
    /**
     * @param newElement
     */
    public void registerforEdit(CnATreeElement newElement) {
        synchronized (editElementSet) {
            editElementSet.add(newElement);
        }       
    }
    
    protected ISO27KModelViewUpdate createISO27KModelViewUpdate() {
        return new ISO27KModelViewUpdate(viewer,cache) {
            public void linkAdded(CnALink link) {
                reload();
                // Open the editors registered before in AddAction
                synchronized (editElementSet) {
                    openEqualLinkEditors(link);
                }
            }
            @Override
            public void databaseChildRemoved(CnATreeElement child) {              
                super.databaseChildRemoved(child);
                reload();
            }
            @Override
            public void databaseChildAdded(CnATreeElement child) {
                child = reloadParent(child);
                super.databaseChildAdded(child);
            }
         };
    }
    
    private void addSelectionListener() {
        selectionListener = new ISelectionListener() {
            /* (non-Javadoc)
             * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
             */
            public void selectionChanged(IWorkbenchPart sourcePart, ISelection selection) {
                pageSelectionChanged(sourcePart, selection);
            }
        };
        getSite().getPage().addPostSelectionListener(selectionListener);
    }
    
    /**
     * @param part
     * @param selection
     */
    protected void pageSelectionChanged(IWorkbenchPart sourcePart, ISelection selection) {
        if(selection instanceof IStructuredSelection && sourcePart instanceof ElementView) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if(element instanceof CnATreeElement) {
                CnATreeElement treeElement = (CnATreeElement) element;
                boolean sourceIsThisView = this.equals(sourcePart);
                setPageSelection(element, treeElement, sourceIsThisView);
            }
        }
    }

    private void setPageSelection(Object element, CnATreeElement treeElement, boolean sourceIsThisView) {
        if(!sourceIsThisView) {
            // check if treeElement has a relation to elements in this view
            if(checkRelations(treeElement)) {   
                loadElements(treeElement);
                setElementToLink(treeElement);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Selected link element, Type: " + treeElement.getObjectType() + ", name: " + treeElement.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
                } 
            }
            if(treeElement!=null && treeElement.getTypeId().equals(Audit.TYPE_ID)) {
                setSelectedAudit((Audit)treeElement);
            }
            if(treeElement!=null && treeElement.getTypeId().equals(Organization.TYPE_ID)) {
                setSelectedOrganization((Organization)treeElement);
            }
        } else {         
            if(element instanceof Group ) {
                CnATreeElement selectedElement = (CnATreeElement) element;
                setSelectedGroup(selectedElement);                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Selected group, Type: " + selectedGroup.getObjectType() + ", name: " + selectedGroup.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else {
                setSelectedGroup(null);                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing selected group, Type: " + selectedGroup.getObjectType() + ", name: " + selectedGroup.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }

    private void loadElements(Object element) {
        this.selection = element;
        try {
            if(element==null) {
                final List<? extends CnATreeElement> elementList = getElementList();
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {
                        setInput(elementList);
                    }
                });
            } else if(element instanceof CnATreeElement) {
                CnATreeElement treeElement = (CnATreeElement)element;
                
                // check if treeElement has a relation to elements in this view
                if(checkRelations(treeElement)) {                                      
                    selectedId = treeElement.getDbId();
                    final List<? extends CnATreeElement> elementList = getLinkedElements(selectedId);
                    Display.getDefault().syncExec(new Runnable(){
                        public void run() {
                            setInput(elementList);
                        }
                    });
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unknown element selected: " + element); //$NON-NLS-1$
                }
            }
        } catch(Exception e) {
            LOG.error("Error while loading linked elements", e); //$NON-NLS-1$
        }
    }
    
    /**
     * Checks if selectedGroup is in elementList. 
     * If not selectedGroup is set to null.
     * 
     * @param elementList list of {@link CnATreeElement}s
     */
    protected void checkSelectedGroup(List elementList) {
        if(selectedGroup!=null && (elementList==null || !elementList.contains(getSelectedGroup()))) {    
            if (LOG.isDebugEnabled()) {
                LOG.debug("Removing selected group, Type: " + getSelectedGroup().getObjectType() + ", name: " + getSelectedGroup().getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            setSelectedGroup(null);          
        }
    }
    
    /**
     * Returns true if treeElement has a relation to elements in this view.
     * Implement this in classes extending this view.
     * 
     * @param treeElement 
     * @return true if treeElement has a relation to elements in this view
     */
    protected boolean checkRelations(CnATreeElement treeElement) {
        return true;
    }

    public void reload() {
        loadElements(this.selection);
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                viewer.refresh();
            }
        });     
    }

    public void setInput(List list) {
        if(list!=null && !list.isEmpty()) {
            viewer.setInput(list);
        } else {
            viewer.setInput(getDummy());
        }
    }
    

    public void setIcon(Image icon) {
        this.setTitleImage(icon);
    }
    
    public void setViewTitle(String title) {
        setPartName(title);
    }
    
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }           
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }

    /**
     * @param manager
     */
    protected void fillContextMenu(IMenuManager manager) {
        // TODO Auto-generated method stub
        
    }

    /**
     * 
     */
    protected void makeActions() {
        doubleClickAction = new Action() {
            public void run() {
                if(viewer.getSelection() instanceof IStructuredSelection) {
                    Object sel = ((IStructuredSelection) viewer.getSelection()).getFirstElement();      
                    EditorFactory.getInstance().updateAndOpenObject(sel);
                }
            }
        };
        
    }
    
    private void addActions() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    /**
     * 
     */
    protected void fillToolBar() {
        //IActionBars bars = getViewSite().getActionBars();
        //IToolBarManager manager = bars.getToolBarManager();
        //manager.add(someAction);
    }

    protected void hookDndListeners() {   
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(modelUpdateListener);
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
        getSite().getPage().removePostSelectionListener(selectionListener);
        super.dispose();
    }
   
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

    }
    
    protected CnATreeElement getSelectedGroup() {
        return selectedGroup;
    }

    protected void setSelectedGroup(CnATreeElement selectedGroup) {
        this.selectedGroup = selectedGroup;
        if(textGroup!=null) {
            textGroup.setText((selectedGroup!=null) ? selectedGroup.getTitle() : ""); //$NON-NLS-1$
        }
    }
    
    public void setElementToLink(CnATreeElement element) {
        this.elementToLink = element;
        textLink.setText((element!=null) ? element.getTitle() : ""); //$NON-NLS-1$
    }

    public CnATreeElement getElementToLink() {
        return elementToLink;
    }

    public void setSelectedAudit(Audit selectedAudit) {
        this.selectedAudit = selectedAudit;
    }

    public Audit getSelectedAudit() {
        return selectedAudit;
    }

    protected Organization getSelectedOrganization() {
        return selectedOrganization;
    }

    protected void setSelectedOrganization(Organization selectedOrganization) {
        this.selectedOrganization = selectedOrganization;
    }

    public ICommandService getCommandService() {
        if (commandService == null) {
            commandService = createCommandServive();
        }
        return commandService;
    }

    private ICommandService createCommandServive() {
        return ServiceFactory.lookupCommandService();
    }

    private CnATreeElement reloadParent(CnATreeElement child) {
        // reload the parent, put is to the cache
        RetrieveInfo ri = RetrieveInfo.getPropertyChildrenInstance().setParent(true);
        CnATreeElement parent = Retriever.retrieveElement(child.getParent(), ri);
        cache.clear(parent);      
        cache.addObject(parent);
        child.setParentAndScope(parent);
        return child;
    }

    private void openEqualLinkEditors(CnALink link) {
        Set<CnATreeElement> workSet =  new HashSet<CnATreeElement>(editElementSet);
        for (CnATreeElement element : workSet) {
            if(element.equals(link.getDependant())) {
                EditorFactory.getInstance().openEditor(link.getDependant());
                editElementSet.remove(element);
            }
            if(element.equals(link.getDependency())) {
                EditorFactory.getInstance().openEditor(link.getDependency());
                editElementSet.remove(element);
            }
        }
        if(editElementSet.size()>0) {
            LOG.warn("Two links added at the same time");
        }
    }

}