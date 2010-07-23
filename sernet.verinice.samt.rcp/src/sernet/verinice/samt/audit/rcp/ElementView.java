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
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ExceptionUtil;
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
import sernet.verinice.model.common.CnATreeElement;
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
    
    protected static ISO27KModel DUMMY = null;
    
    protected static ISO27KModel getDummy() {
        if(DUMMY==null) {
            DUMMY = new ISO27KModel();
            DUMMY.setEntity(new Entity(ISO27KModel.TYPE_ID));
            DUMMY.setTitel("empty");
        }
        return DUMMY;
    }
    
    private TreeViewer viewer;
    
    private TreeViewerCache cache = new TreeViewerCache();
    
    private ISMViewContentProvider contentProvider;
    
    private ISO27KModelViewUpdate modelUpdateListener;
    
    private IModelLoadListener modelLoadListener;
    
    private ISelectionListener selectionListener;
    
    private ICommandService commandService;

    protected Integer selectedId;
    
    protected Object selection;
    
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
    abstract protected List<CnATreeElement> getLinkedElements(int selectedId) throws CommandException;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {
        try {
            initView(parent);
            startInitDataJob();
        } catch (Exception e) {
            LOG.error("Error while creating view", e); 
            ExceptionUtil.log(e, "Error while opening Audit-View.");
        }
        
    }

    protected void initView(Composite parent) {
        contentProvider = new ISMViewContentProvider(cache);
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(contentProvider);
        
        IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();
        viewer.setLabelProvider(new DecoratingLabelProvider(new ISMViewLabelProvider(cache), workbench.getDecoratorManager()));
        
        // make the viewer a selection provider
        getSite().setSelectionProvider(viewer);
        // listen to other selection provider
        addSelectionListener();
        
        hookContextMenu();
        makeActions();
        fillToolBar();
        hookDndListeners();
    }
    
    /**
     * 
     */
    protected void startInitDataJob() {
        WorkspaceJob initDataJob = new WorkspaceJob("Loading data...") {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask("Loading data...", IProgressMonitor.UNKNOWN);
                    initData();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); 
                    status= new Status(Status.ERROR, "sernet.verinice.samt.audit.rcp", "Error while loading data.",e); 
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
                    LOG.debug("Creating modelUpdateListener for ISMView."); 
                }
                Activator.inheritVeriniceContextState();
                modelUpdateListener = new ISO27KModelViewUpdate(viewer,cache);
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
        if(!this.equals(sourcePart)) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            loadElements(element);
        }    
    }

    private void loadElements(Object element) {
        this.selection = element;
        try {
            if(element instanceof Organization || element==null) {
                final List<? extends CnATreeElement> elementList = getElementList();
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {
                        setInput(elementList);
                    }
                });
            } else if(element instanceof CnATreeElement) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("CnATreeElement selected: " + ((CnATreeElement)element).getTitle());
                }
                selectedId = ((CnATreeElement)element).getDbId();
                final List<CnATreeElement> elementList = getLinkedElements(selectedId);
                Display.getDefault().syncExec(new Runnable(){
                    public void run() {
                        setInput(elementList);
                    }
                });
                setDescription(((CnATreeElement)element).getTitle());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Unknown element selected: " + element);
                }
            }
        } catch(Exception e) {
            LOG.error("Error while loading linked elements", e);
        }
    }
    
    public void reload() {
        loadElements(this.selection);
        viewer.refresh();
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
    
    public void setDescription(String title) {
        this.setContentDescription(title);
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
        // TODO Auto-generated method stub
        
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
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // TODO Auto-generated method stub

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
