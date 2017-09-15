/*******************************************************************************
 * Copyright (c) 2017 Sebastian Hagedorn.
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
 *     Sebastian Hagedorn sh[at]sernet.de - initial API and implementation
 ******************************************************************************/
package sernet.verinice.bp.rcp;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.Perspective;
import sernet.gs.ui.rcp.main.actions.ShowBulkEditAction;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDragListener;
import sernet.gs.ui.rcp.main.bsi.dnd.BSIModelViewDropListener;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionElementTransfer;
import sernet.gs.ui.rcp.main.bsi.dnd.transfer.BaseProtectionGroupTransfer;
import sernet.gs.ui.rcp.main.bsi.editors.EditorFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.ILinkedWithEditorView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Messages;
import sernet.verinice.iso27k.rcp.action.CollapseAction;
import sernet.verinice.iso27k.rcp.action.ControlDropPerformer;
import sernet.verinice.iso27k.rcp.action.ExpandAction;
import sernet.verinice.iso27k.rcp.action.HideEmptyFilter;
import sernet.verinice.iso27k.rcp.action.MetaDropAdapter;
import sernet.verinice.model.bp.IBpModelListener;
import sernet.verinice.model.bp.elements.BpModel;
import sernet.verinice.model.bp.elements.ItNetwork;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.common.TypeParameter;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.rcp.IAttachedToPerspective;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.tree.TreeContentProvider;
import sernet.verinice.rcp.tree.TreeLabelProvider;
import sernet.verinice.rcp.tree.TreeUpdateListener;
import sernet.verinice.service.tree.ElementManager;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class BaseProtectionView extends RightsEnabledView 
    implements IAttachedToPerspective, ILinkedWithEditorView {
    
    private static final Logger LOG = Logger.getLogger(BaseProtectionView.class);
    
    private static int operations = DND.DROP_COPY | DND.DROP_MOVE;
    
    protected TreeViewer viewer;
    private TreeContentProvider contentProvider;
    private ElementManager elementManager;
    
    private DrillDownAdapter drillDownAdapter;
    private Object mutex = new Object();
    
    private IModelLoadListener modelLoadListener;
    private IBpModelListener modelUpdateListener;
    
    private Action doubleClickAction; 
    
    private ShowBulkEditAction bulkEditAction;
    
    private MetaDropAdapter metaDropAdapter;
    
    private ExpandAction expandAction; 
    private Action expandAllAction; 
    private CollapseAction collapseAction;   
    private Action collapseAllAction;
    
    public static final String ID = "sernet.verinice.bp.rcp.BaseProtectionView"; //$NON-NLS-1$
    
    public BaseProtectionView() {
        super();
        elementManager = new ElementManager();
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(final Composite parent) {
        super.createPartControl(parent);
        try {
            initView(parent);
            startInitDataJob();
        } catch (Exception e) {
            LOG.error("Error while creating organization view", e); //$NON-NLS-1$
            ExceptionUtil.log(e, "Error while creating base protection view.");
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.BASEPROTECTIONVIEW;
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getViewId()
     */
    @Override
    public String getViewId() {
        return ID;
    }
    
    /**
     * @param parent
     */
    protected void initView(Composite parent) {
        IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();
        if(CnAElementFactory.getInstance().isBpModelLoaded()) {
            CnAElementFactory.getInstance().reloadModelFromDatabase();
        }
        
        
        contentProvider = new TreeContentProvider(elementManager);
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        drillDownAdapter = new DrillDownAdapter(viewer);
        viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(new DecoratingLabelProvider(new TreeLabelProvider(), workbench.getDecoratorManager()));
//        toggleLinking(Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.LINK_TO_EDITOR));
        
        getSite().setSelectionProvider(viewer);
        hookContextMenu();
        makeActions();
        addActions();
        fillToolBar();
        hookDndListeners();
        
//        getSite().getPage().addPartListener(linkWithEditorPartListener);
        viewer.refresh(true);
    }
    
    protected void startInitDataJob() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MotITBPview: startInitDataJob"); //$NON-NLS-1$
        }
        // TODO: create own job name, replace ism-constant
        WorkspaceJob initDataJob = new WorkspaceJob("Loading data...") {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask("Loading data...", IProgressMonitor.UNKNOWN);
                    initData();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", "Error while loading data",e);
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        JobScheduler.scheduleInitJob(initDataJob);      
    }

    protected void initData() { 
        if (LOG.isDebugEnabled()) {
            LOG.debug("MotITBPVIEW: initData"); //$NON-NLS-1$
        }
        synchronized (mutex) {
            if(CnAElementFactory.isBpModelLoaded()) {
                if (modelUpdateListener == null ) {
                    // modellistener should only be created once!
                    if (LOG.isDebugEnabled()){
                        Logger.getLogger(this.getClass()).debug("Creating modelUpdateListener for MotITBPView."); //$NON-NLS-1$
                    }
                    modelUpdateListener = new TreeUpdateListener(viewer,elementManager);
                    CnAElementFactory.getInstance().getBpModel().addModITBOModelListener(modelUpdateListener);
                    Display.getDefault().syncExec(new Runnable(){
                        @Override
                        public void run() {
                            setInput(CnAElementFactory.getInstance().getBpModel());
                            viewer.refresh();
                        }
                    });
                }
            } else if(modelLoadListener==null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ISMView No model loaded, adding model load listener."); //$NON-NLS-1$
                }
                // model is not loaded yet: add a listener to load data when it's loaded
                modelLoadListener = new IModelLoadListener() {
                    
                    @Override
                    public void closed(BSIModel model) {
                        // nothing to do
                    }
                    
                    @Override
                    public void loaded(BSIModel model) {
                        // nothing to do
                    }
                    
                    @Override
                    public void loaded(ISO27KModel model) {
                        // nothing to do
                    }

                    @Override
                    public void loaded(BpModel model) {
                        startInitDataJob();
                    }
                    
                };
                CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
                
            }
        }
    }
    
    public void setInput(BpModel model) {
        viewer.setInput(model);
    }
    
    private void hookContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }           
        });
        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(menuMgr, viewer);
    }
    
    private void hookDndListeners() {
        Transfer[] dragTypes = new Transfer[] { BaseProtectionElementTransfer.getInstance(),
                                                BaseProtectionGroupTransfer.getInstance()
                                              };
        Transfer[] dropTypes = new Transfer[] { BaseProtectionElementTransfer.getInstance(),
                                                BaseProtectionGroupTransfer.getInstance()
                                              };
        

        viewer.addDragSupport(operations, dragTypes, new BSIModelViewDragListener(viewer));
        viewer.addDropSupport(operations, dropTypes, metaDropAdapter);
        
    }
     
    protected void fillContextMenu(IMenuManager manager) {
        ISelection selection = viewer.getSelection();
        if(selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size()==1) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            if(sel instanceof ItNetwork) {
                ItNetwork element = (ItNetwork) sel;
//                if(CnAElementHome.getInstance().isNewChildAllowed(element)) {
//                    MenuManager submenuNew = new MenuManager("&New","content/new"); //$NON-NLS-1$ //$NON-NLS-2$
//                    submenuNew.add(new AddGroup(element,AssetGroup.TYPE_ID,Asset.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,AuditGroup.TYPE_ID,Audit.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,ControlGroup.TYPE_ID,Control.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,DocumentGroup.TYPE_ID,Document.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,EvidenceGroup.TYPE_ID,Evidence.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,ExceptionGroup.TYPE_ID,sernet.verinice.model.iso27k.Exception.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,FindingGroup.TYPE_ID,Finding.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,IncidentGroup.TYPE_ID,Incident.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,IncidentScenarioGroup.TYPE_ID,IncidentScenario.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,InterviewGroup.TYPE_ID,Interview.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,PersonGroup.TYPE_ID,PersonIso.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,ProcessGroup.TYPE_ID,sernet.verinice.model.iso27k.Process.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,RecordGroup.TYPE_ID,Record.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,RequirementGroup.TYPE_ID,Requirement.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,ResponseGroup.TYPE_ID,Response.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,ThreatGroup.TYPE_ID,Threat.TYPE_ID));
//                    submenuNew.add(new AddGroup(element,VulnerabilityGroup.TYPE_ID,Vulnerability.TYPE_ID));
//                    manager.add(submenuNew);
//                }
            }
        }
        
        manager.add(new GroupMarker("content")); //$NON-NLS-1$
        manager.add(new Separator());
        manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
        manager.add(new Separator());
        manager.add(new GroupMarker("special")); //$NON-NLS-1$
//        manager.add(bulkEditAction);
//        manager.add(accessControlEditAction);
//        manager.add(naturalizeAction);
//        manager.add(new Separator());
        manager.add(expandAction);
        manager.add(collapseAction);
        drillDownAdapter.addNavigationActions(manager); 
    }
    
    private void makeActions() {
        
        doubleClickAction = new Action() {
            @Override
            public void run() {
                if(viewer.getSelection() instanceof IStructuredSelection) {
                    Object sel = ((IStructuredSelection) viewer.getSelection()).getFirstElement();      
                    EditorFactory.getInstance().updateAndOpenObject(sel);
                }
            }
        };
        
        makeExpandAndCollapseActions();
     
        bulkEditAction = new ShowBulkEditAction(getViewSite().getWorkbenchWindow(), "Bulk edit...");
        
        
        BSIModelViewDropListener bsiDropAdapter;
        metaDropAdapter = new MetaDropAdapter(viewer);
        bsiDropAdapter = new BSIModelViewDropListener(viewer);
        metaDropAdapter.addAdapter(bsiDropAdapter);

    }


    protected void makeExpandAndCollapseActions() {
        expandAction = new ExpandAction(viewer, contentProvider);
        expandAction.setText("Expand children");
        expandAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));
        
        collapseAction = new CollapseAction(viewer);
        collapseAction.setText("Collapse chilren");
        collapseAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
        
        expandAllAction = new Action() {
            @Override
            public void run() {
                expandAll();
            }
        };
        expandAllAction.setText("Expand all");
        expandAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.EXPANDALL));

        collapseAllAction = new Action() {
            @Override
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAllAction.setText("Collapse all");
        collapseAllAction.setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.COLLAPSEALL));
    }
    
    protected void expandAll() {
        viewer.expandAll();
    }
    
    /**
     * Override this in subclasses to hide empty groups
     * on startup.
     * 
     * @return a HideEmptyFilter
     */
    protected HideEmptyFilter createHideEmptyFilter() {
        return new HideEmptyFilter(viewer);
    }

    /**
     * Override this in subclasses to hide empty groups
     * on startup.
     * 
     * @return a {@link TypeParameter}
     */
    protected TypeParameter createTypeParameter() {
        return new TypeParameter();
    }
    
    private void addActions() {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                doubleClickAction.run();
            }
        });
        
        viewer.addSelectionChangedListener(expandAction);
        viewer.addSelectionChangedListener(collapseAction);
    }
    
    protected void fillToolBar() {
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager manager = bars.getToolBarManager();
        manager.add(expandAllAction);
        manager.add(collapseAllAction);
        drillDownAdapter.addNavigationActions(manager);
//        manager.add(filterAction);
//        manager.add(linkWithEditorAction);
    }


    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ILinkedWithEditorView#editorActivated(org.eclipse.ui.IEditorPart)
     */
    @Override
    public void editorActivated(IEditorPart activeEditor) {
        // TODO Auto-generated method stub
        // DO THIS when model is loading correctly
    }


    @Override
    public String getPerspectiveId() {
        // TODO: implement own base protection perspective
        return Perspective.ID;
    }
    
    @Override
    public void dispose() {
        elementManager.clearCache();
        if(CnAElementFactory.isBpModelLoaded()) {
            CnAElementFactory.getInstance().getBpModel().removeBpModelListener(modelUpdateListener);
        }
        CnAElementFactory.getInstance().removeLoadListener(modelLoadListener);
//        getSite().getPage().removePartListener(linkWithEditorPartListener);
        super.dispose();
    }
    

}
