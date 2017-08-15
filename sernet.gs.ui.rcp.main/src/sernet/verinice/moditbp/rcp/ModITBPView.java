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
package sernet.verinice.moditbp.rcp;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.DrillDownAdapter;

import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.IModelLoadListener;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Messages;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.model.iso27k.IModITBPModelListener;
import sernet.verinice.model.iso27k.ISO27KModel;
import sernet.verinice.model.moditbp.elements.ITNetwork;
import sernet.verinice.model.moditbp.elements.ModITBPModel;
import sernet.verinice.rcp.RightsEnabledView;
import sernet.verinice.rcp.tree.TreeContentProvider;
import sernet.verinice.rcp.tree.TreeLabelProvider;
import sernet.verinice.rcp.tree.TreeUpdateListener;
import sernet.verinice.service.tree.ElementManager;

/**
 * @author Sebastian Hagedorn sh[at]sernet.de
 *
 */
public class ModITBPView extends RightsEnabledView {
    
    private static final Logger LOG = Logger.getLogger(ModITBPView.class);
    
    protected TreeViewer viewer;
    private TreeContentProvider contentProvider;
    private ElementManager elementManager;
    
    private DrillDownAdapter drillDownAdapter;
    private Object mutex = new Object();
    
    private IModelLoadListener modelLoadListener;
    private IModITBPModelListener modelUpdateListener;
    
    public static final String ID = "sernet.verinice.moditbp.rcp.ModITBPView"; //$NON-NLS-1$
    
    public ModITBPView() {
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
            ExceptionUtil.log(e, Messages.ISMView_2);
        }
    }

    /* (non-Javadoc)
     * @see sernet.verinice.rcp.RightsEnabledView#getRightID()
     */
    @Override
    public String getRightID() {
        return ActionRightIDs.MODITBPMODELVIEW;
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
        if(CnAElementFactory.getInstance().isIsoModelLoaded()) {
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
//        makeActions();
//        addActions();
//        fillToolBar();
//        hookDNDListeners();
        
//        getSite().getPage().addPartListener(linkWithEditorPartListener);
        viewer.refresh(true);
    }
    
    protected void startInitDataJob() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("MotITBPview: startInitDataJob"); //$NON-NLS-1$
        }
        // TODO: create own job name, replace ism-constant
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.ISMView_InitData) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.ISMView_InitData, IProgressMonitor.UNKNOWN);
                    initData();
                } catch (Exception e) {
                    LOG.error("Error while loading data.", e); //$NON-NLS-1$
                    status= new Status(Status.ERROR, "sernet.gs.ui.rcp.main", Messages.ISMView_4,e); //$NON-NLS-1$
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
            if(CnAElementFactory.isModITBPModelLoaded()) {
                if (modelUpdateListener == null ) {
                    // modellistener should only be created once!
                    if (LOG.isDebugEnabled()){
                        Logger.getLogger(this.getClass()).debug("Creating modelUpdateListener for MotITBPView."); //$NON-NLS-1$
                    }
                    modelUpdateListener = new TreeUpdateListener(viewer,elementManager);
                    CnAElementFactory.getInstance().getModITBPModel().addModITBOModelListener(modelUpdateListener);
                    Display.getDefault().syncExec(new Runnable(){
                        @Override
                        public void run() {
                            setInput(CnAElementFactory.getInstance().getModITBPModel());
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
                    public void loaded(ModITBPModel model) {
                        startInitDataJob();
                    }
                    
                };
                CnAElementFactory.getInstance().addLoadListener(modelLoadListener);
                
            }
        }
    }
    
    public void setInput(ModITBPModel model) {
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
    
    
    protected void fillContextMenu(IMenuManager manager) {
        ISelection selection = viewer.getSelection();
        if(selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size()==1) {
            Object sel = ((IStructuredSelection) selection).getFirstElement();
            if(sel instanceof ITNetwork) {
                ITNetwork element = (ITNetwork) sel;
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
//        manager.add(expandAction);
//        manager.add(collapseAction);
        drillDownAdapter.addNavigationActions(manager); 
    }
    


}
