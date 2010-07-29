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

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

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
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnALink;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.Group;
import sernet.verinice.model.iso27k.IISO27kGroup;
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
    
    protected static ISO27KModel DUMMY = null;
    
    protected static ISO27KModel getDummy() {
        if(DUMMY==null) {
            DUMMY = new ISO27KModel();
            DUMMY.setEntity(new Entity(ISO27KModel.TYPE_ID));
        }
        return DUMMY;
    }
    
    protected TreeViewer viewer;
    
    protected TreeViewerCache cache = new TreeViewerCache();
    
    protected ISMViewContentProvider contentProvider;
    
    private ISO27KModelViewUpdate modelUpdateListener;
    
    private IModelLoadListener modelLoadListener;
    
    private ISelectionListener selectionListener;
    
    private Action doubleClickAction;
    
    private ICommandService commandService;

    protected Integer selectedId;
    
    protected Object selection;
    
    protected CnATreeElement selectedGroup;
    
    private CnATreeElement selectedElement;
    
    private Audit selectedAudit;
    
    private Organization selectedOrganization;
    
    private Label textLink, textGroup;

    /**
     * @return {@link CnATreeElement}s to show in this view
     * @throws CommandException
     */
    abstract protected List<? extends CnATreeElement> getElementList() throws CommandException;
    
    /**
     * Loads {@link CnATreeElement}s which are linked to the element with primary key
     * selectedId. After loading elements are shown in the view
     * 
     * @param selectedId primary key of a {@link CnATreeElement}
     * @return Elements linked to primary key selectedId
     * @throws CommandException
     */
    abstract protected List<? extends CnATreeElement> getLinkedElements(int selectedId) throws CommandException;
    
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
        
        Label labelGroup = new Label(compositeInfo,SWT.NONE);
        labelGroup.setText(Messages.ElementView_3);
        labelGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        textGroup = new Label(compositeInfo,SWT.NONE);
        textGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        contentProvider = new ISMViewContentProvider(cache);
        
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
    
    protected ISO27KModelViewUpdate createISO27KModelViewUpdate() {
        return new ISO27KModelViewUpdate(viewer,cache) {
            /* (non-Javadoc)
             * @see sernet.verinice.iso27k.model.IISO27KModelListener#linkAdded(sernet.gs.ui.rcp.main.common.model.CnALink)
             */
            public void linkAdded(CnALink link) {
                reload();
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
        if(selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if(element instanceof CnATreeElement) {
                boolean sourceIsThisView = this.equals(sourcePart);
                if(!sourceIsThisView) {
                    loadElements(element);
                    CnATreeElement selectedElement = (CnATreeElement) element;
                    setSelectedElement(selectedElement);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Selected link element, Type: " + selectedElement.getObjectType() + ", name: " + selectedElement.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
                    } 
                }            
                if(element instanceof IISO27kGroup && sourceIsThisView) {
                    CnATreeElement selectedElement = (CnATreeElement) element;
                    setSelectedGroup(selectedElement);
                   
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Selected group, Type: " + selectedGroup.getObjectType() + ", name: " + selectedGroup.getTitle()); //$NON-NLS-1$ //$NON-NLS-2$
                    }
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
    
    /**
     * 
     */
    private void hookContextMenu() {
        // TODO Auto-generated method stub
        
    }

    /**
     * 
     */
    private void makeActions() {
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
        // TODO Auto-generated method stub
        
    }

    /**
     * 
     */
    private void hookDndListeners() {
        // TODO Auto-generated method stub
        
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
        textGroup.setText((selectedGroup!=null) ? selectedGroup.getTitle() : ""); //$NON-NLS-1$
    }
    
    public void setSelectedElement(CnATreeElement element) {
        this.selectedElement = element;
        textLink.setText((element!=null) ? element.getTitle() : ""); //$NON-NLS-1$
        if(element!=null && element.getTypeId().equals(Audit.TYPE_ID)) {
            setSelectedAudit((Audit)element);
        }
        if(element!=null && element.getTypeId().equals(Organization.TYPE_ID)) {
            setSelectedOrganization((Organization)element);
        }
    }

    public CnATreeElement getSelectedElement() {
        return selectedElement;
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

}