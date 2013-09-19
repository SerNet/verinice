/*******************************************************************************
 * Copyright (c) 2012 Sebastian Hagedorn <sh@sernet.de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *     You should have received a copy of the GNU General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Sebastian Hagedorn <sh@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.editors.BSIElementEditorInput;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.gs.ui.rcp.main.common.model.PlaceHolder;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.gs.ui.rcp.main.service.crudcommands.LoadPolymorphicCnAElementById;
import sernet.hui.common.VeriniceContext;
import sernet.hui.common.connect.HUITypeFactory;
import sernet.springclient.RightsServiceClient;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.ICommandService;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.LinkWithEditorPartListener;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.validation.CnAValidation;

/**
 *
 */
public class CnAValidationView extends ViewPart implements ILinkedWithEditorView  {
    
    static final Logger LOG = Logger.getLogger(CnAValidationView.class);
    
    public static final String ID = "sernet.verinice.validation.CnAValidationView";
    private static final String STD_LOAD_ERRMSG = "Error while loading data";
    
    private TableViewer viewer;

    private TableSorter tableSorter = new TableSorter();
    private ICommandService commandService;
    
    private Action doubleClickAction;
    
    private Action refreshAction;
    
    private CnAValidationContentProvider contentProvider = new CnAValidationContentProvider(this);
    
    private IPartListener2 linkWithEditorPartListener = new LinkWithEditorPartListener(this);
    
    private boolean isLinkingActive = true;
    
    private CnATreeElement currentCnaElement;
    
    private IModelLoadListener modelLoadListener;
    
    private ISelectionListener selectionListener;
    
    private RightsServiceClient rightsService;
    
    public CnAValidationView(){
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new FillLayout());
        createTable(parent);
        getSite().setSelectionProvider(viewer);
        hookPageSelection();

        addBSIModelListeners();
        addISO27KModelListeners();
        hookModelLoadListener();
        
        makeActions();
        hookActions();
        
        
        fillLocalToolBar();
        getSite().getPage().addPartListener(linkWithEditorPartListener);
    }
    
    private void makeActions() {

        refreshAction = new Action(Messages.ValidationView_8, SWT.NONE){
            public void run(){
                loadValidations();
            }
        };
        
        refreshAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.RELOAD));

        doubleClickAction = new Action(){
            public void run() {
                if (viewer.getSelection() instanceof IStructuredSelection && ((IStructuredSelection) viewer.getSelection()).getFirstElement() instanceof CnAValidation) {
                    try {
                        CnAValidation validation = (CnAValidation)((IStructuredSelection)viewer.getSelection()).getFirstElement();
                        LoadPolymorphicCnAElementById command = new LoadPolymorphicCnAElementById(new Integer[]{validation.getElmtDbId()});
                        command = ServiceFactory.lookupCommandService().executeCommand(command);
                        if(command.getElements() != null && !command.getElements().isEmpty() && command.getElements().get(0) != null){
                            EditorFactory.getInstance().updateAndOpenObject(command.getElements().get(0));
                        } else {
                            MessageDialog.openError(getSite().getShell(), "Error", "Object not found.");
                        }
                    } catch (Exception t){
                        LOG.error("Error while opening element.", t); //$NON-NLS-1$
                    }
                }
            }
        };
    }

    private void hookActions() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
    }

    private void hookPageSelection() {
        selectionListener = new ISelectionListener() {
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                pageSelectionChanged(part, selection);
            }
        };
        getSite().getPage().addPostSelectionListener(selectionListener);
    }

    protected void pageSelectionChanged(IWorkbenchPart part, ISelection selection) {
        Object element = ((IStructuredSelection) selection).getFirstElement();
        elementSelected(element);
        if (element instanceof CnATreeElement) {
           currentCnaElement = (CnATreeElement)element;
        }
    }
    
    private void createTable(Composite parent) {
        TableColumn elementTypeColumn;
        TableColumn elementNameColumn;
        TableColumn propertyColumn;
        TableColumn hintColumn;
        
        final int typeColumnWidth = 80;
        final int nameColumnWidth = 150;
        final int propertyColumnWidth = 100;
        final int hintColumnWidth = 200;
        
        viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new ValidationLabelProvider());
        Table table = viewer.getTable();
        
        elementTypeColumn = new TableColumn(table, SWT.LEFT);
        elementTypeColumn.setWidth(typeColumnWidth);
        elementTypeColumn.setText(Messages.ValidationView_5);
        elementTypeColumn.addSelectionListener(new SortSelectionAdapter(this, elementTypeColumn, 0));
             
        elementNameColumn = new TableColumn(table, SWT.LEFT);
        elementNameColumn.setWidth(nameColumnWidth);
        elementNameColumn.setText(Messages.ValidationView_4);
        elementNameColumn.addSelectionListener(new SortSelectionAdapter(this, elementNameColumn, 1));
        
        propertyColumn = new TableColumn(table, SWT.LEFT);
        propertyColumn.setWidth(propertyColumnWidth);
        propertyColumn.setText(Messages.ValidationView_6);
        propertyColumn.addSelectionListener(new SortSelectionAdapter(this, propertyColumn, 2));

        hintColumn = new TableColumn(table, SWT.LEFT);
        hintColumn.setText(Messages.ValidationView_7);
        hintColumn.setWidth(hintColumnWidth);
        hintColumn.addSelectionListener(new SortSelectionAdapter(this, hintColumn, 3));

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        viewer.setSorter(tableSorter);

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        // emtpy
    }
    
    protected void startInitDataJob() {
        WorkspaceJob initDataJob = new WorkspaceJob(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData) {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    Activator.inheritVeriniceContextState();
                    loadValidations();
                } catch (Exception e) {
                    LOG.error(STD_LOAD_ERRMSG, e); //$NON-NLS-1$
                    status = new Status(Status.ERROR, "sernet.gs.ui.rcp.main", STD_LOAD_ERRMSG, e); //$NON-NLS-1$ //$NON-NLS-2$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);
    }

    private void loadValidations(){
        List<CnAValidation> validations = Collections.emptyList();
        if(currentCnaElement != null){
            final LoadValidationJob job = new LoadValidationJob(currentCnaElement.getScopeId());

            BusyIndicator.showWhile(null, new Runnable() {          
                @Override
                public void run() {
                    job.loadValidations();
                }
            });

            validations = job.getValidations();
            RefreshValidationView refresh = new RefreshValidationView(validations, viewer);
            refresh.refresh();
        }
    }
    
    public static Display getDisplay() {
        Display display = Display.getCurrent();
        //may be null if outside the UI thread
        if (display == null){
           display = Display.getDefault();
        }
        return display;       
     }
    
    @Override
    public void dispose() {
        super.dispose();
        getSite().getPage().removePostSelectionListener(selectionListener);
        getSite().getPage().removePartListener(linkWithEditorPartListener);
        removeModelListeners();
    }
    
    /**
     * 
     */
    protected void removeModelListeners() {
        CnAElementFactory.getLoadedModel().removeBSIModelListener(contentProvider);
        CnAElementFactory.getInstance().getISO27kModel().removeISO27KModelListener(contentProvider);
    }
    
    protected void addISO27KModelListeners() {
        WorkspaceJob initDataJob = new WorkspaceJob(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData) {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    if (CnAElementFactory.isIsoModelLoaded()) {
                        CnAElementFactory.getInstance().getISO27kModel().addISO27KModelListener(contentProvider);
                    }
                } catch (Exception e) {
                    LOG.error(STD_LOAD_ERRMSG, e); //$NON-NLS-1$
                    status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.ValidationView_3,e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);          
    }
    
    protected void addBSIModelListeners() {
        WorkspaceJob initDataJob = new WorkspaceJob(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData) {
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(sernet.verinice.iso27k.rcp.Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    if (CnAElementFactory.isModelLoaded()) {
                        CnAElementFactory.getInstance().getLoadedModel().addBSIModelListener(contentProvider);
                    }
                } catch (Exception e) {
                    LOG.error(STD_LOAD_ERRMSG, e); //$NON-NLS-1$
                    status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.ValidationView_3,e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);      
    }
    
    private void hookModelLoadListener() {
        this.modelLoadListener = new IModelLoadListener() {

            public void closed(BSIModel model) {
                removeModelListeners();
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        viewer.setInput(new PlaceHolder("")); //$NON-NLS-1$
                    }
                });
            }

            public void loaded(BSIModel model) {
                synchronized (modelLoadListener) {
                    startInitDataJob();
                    addBSIModelListeners();
                }
            }

            @Override
            public void loaded(ISO27KModel model) {
                synchronized (modelLoadListener) {
                    startInitDataJob();
                    addISO27KModelListeners();   
                }
            }
            
        };
        CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
    }
      
    private static class ValidationLabelProvider extends LabelProvider implements ITableLabelProvider {      
        
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof PlaceHolder) {
                return null;
            }
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            try {
                if (element instanceof PlaceHolder) {
                    if (columnIndex == 1) {
                        PlaceHolder ph = (PlaceHolder) element;
                        return ph.getTitle();
                    }
                    return ""; //$NON-NLS-1$
                }
                CnAValidation validation = (CnAValidation) element;
                switch (columnIndex) {
                case 0:
                    return HUITypeFactory.getInstance().getMessage(validation.getElementType()); //$NON-NLS-1$
                case 1:
                    return validation.getElmtTitle(); //$NON-NLS-1$
                case 2:
                    return HUITypeFactory.getInstance().getMessage(validation.getPropertyId()); //$NON-NLS-1$
                case 3:
                    return validation.getHintId(); //$NON-NLS-1$
                default:
                    return null;
                }
            } catch (Exception e) {
                LOG.error("Error while getting column text", e); //$NON-NLS-1$
                throw new RuntimeException(e);
            }
        }
 

    }
    
    private void fillLocalToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(this.refreshAction);
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
    
    private static class TableSorter extends ViewerSorter {
        private int propertyIndex;
        private static final int DEFAULT_SORT_COLUMN = 0;
        private static final int DESCENDING = 1;
        private static final int ASCENDING = 0;
        private int direction = ASCENDING;

        public TableSorter() {
            super();
            this.propertyIndex = DEFAULT_SORT_COLUMN;
            this.direction = ASCENDING;
        }

        public void setColumn(int column) {
            if (column == this.propertyIndex) {
                // Same column as last sort; toggle the direction
                direction = (direction == ASCENDING) ? DESCENDING : ASCENDING;
            } else {
                // New column; do an ascending sort
                this.propertyIndex = column;
                direction = ASCENDING;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface
         * .viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            CnAValidation a1 = (CnAValidation) e1;
            CnAValidation a2 = (CnAValidation) e2;
            int rc = 0;
            if (e1 == null) {
                if (e2 != null) {
                    rc = 1;
                }
            } else if (e2 == null) {
                rc = -1;
            } else {
                // e1 and e2 != null
                switch (propertyIndex) {
                case 0:
                    if(a1.getElementType() != null && a2.getElementType() != null){
                        rc = a1.getElementType().compareTo(a2.getElementType());
                        break;
                    }
                case 1:
                    if(a1.getElmtTitle() != null && a2.getElmtTitle() != null){
                        rc = a1.getElmtTitle().compareTo(a2.getElmtTitle());
                        break;
                    }
                case 2:
                    rc = a1.getPropertyId().compareTo(a2.getPropertyId());
                    break;
                case 3:
                    rc = a1.getHintId().compareTo(a2.getHintId());
                    break;
                default:
                    rc = 0;
                }
            }
            
            // If descending order, flip the direction
            if (direction == DESCENDING) {
                rc = -rc;
            }
            return rc;
        }

    }

    private static class SortSelectionAdapter extends SelectionAdapter {
        private CnAValidationView validationView;
        private TableColumn column;
        private int index;

        public SortSelectionAdapter(CnAValidationView validationView, TableColumn column, int index) {
            super();
            this.validationView = validationView;
            this.column = column;
            this.index = index;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            validationView.tableSorter.setColumn(index);
            int dir = validationView.viewer.getTable().getSortDirection();
            if (validationView.viewer.getTable().getSortColumn() == column) {
                dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
            } else {

                dir = SWT.DOWN;
            }
            validationView.viewer.getTable().setSortDirection(dir);
            validationView.viewer.getTable().setSortColumn(column);
            validationView.viewer.refresh();
        }

    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ILinkedWithEditorView#editorActivated(org.eclipse.ui.IEditorPart)
     */
    @Override
    public void editorActivated(IEditorPart editor) {
        if (!isLinkingActive() || !getViewSite().getPage().isPartVisible(this) || editor == null) {
            return;
        }
        CnATreeElement element = BSIElementEditorInput.extractElement(editor);
        if (element == null) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Element in editor :" + element.getUuid());
            LOG.debug("Loading validationElements of element now..."); //$NON-NLS-1$
        }
        elementSelected(element);
    }
    
    protected void elementSelected(Object element) {
        try {
            if (element instanceof CnATreeElement) {
                setCurrentCnaElement((CnATreeElement) element);
                loadValidations();

            } 
        } catch (Exception e) {
            LOG.error("Error while loading validations", e); //$NON-NLS-1$
        }
    }
    
    /**
     * @return
     */
    private boolean isLinkingActive() {
        return isLinkingActive;
    }
    
    public void setCurrentCnaElement(CnATreeElement currentCnaElement) {
        if(currentCnaElement != null){
            this.currentCnaElement = currentCnaElement;
            if(isLinkingActive()){
                StructuredSelection selection = (StructuredSelection)viewer.getSelection();
                CnAValidation selectedValidation = null;
                if(selection != null){
                    selectedValidation = (CnAValidation)selection.getFirstElement();
                }
                Object input = viewer.getInput();
                CnAValidation validationToSelect = determineValidationToSelect(input);
                boolean changeSelectedElement = false;
                
                if((selectedValidation != null && !selectedValidation.getElmtDbId().equals(this.currentCnaElement.getDbId())) || selectedValidation == null){
                    changeSelectedElement = true;
                } 
                if(validationToSelect != null && changeSelectedElement){
                    viewer.setSelection(new StructuredSelection(validationToSelect), true);
                }
            }
        }
    }

    private CnAValidation determineValidationToSelect(Object input) {
        if(input instanceof ArrayList){
            ArrayList inputList = (ArrayList)input;
            for(Object o : inputList){
                if(o instanceof CnAValidation){
                    CnAValidation validation = (CnAValidation)o;
                    if(validation.getElmtDbId().equals(this.currentCnaElement.getDbId())){
                        return validation;
                    }
                }
            }
        }
        return null;
    }
    
    public void reloadAll(){
        loadValidations();
    }
    
    /**
     * @return the rightsService
     */
    public RightsServiceClient getRightsService() {
        if(rightsService==null) {
            rightsService = (RightsServiceClient) VeriniceContext.get(VeriniceContext.RIGHTS_SERVICE);
        }
        return rightsService;
    }
    
    public String getRightID() {
        return ActionRightIDs.CNAVALIDATION;
    }

}
