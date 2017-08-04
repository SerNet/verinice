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
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
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
public class MotITBPView extends RightsEnabledView {
    
    private static final Logger LOG = Logger.getLogger(MotITBPView.class);
    
    protected TreeViewer viewer;
    private TreeContentProvider contentProvider;
    private ElementManager elementManager;
    
    private DrillDownAdapter drillDownAdapter;
    private Object mutex = new Object();
    
    private IModelLoadListener modelLoadListener;
    private IModITBPModelListener modelUpdateListener;
    
    public static final String ID = "sernet.verinice.moditbp.rcp.MotITBPView"; //$NON-NLS-1$
    
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
        // TODO replace this later with unique actionID
        return ActionRightIDs.ISMVIEW;
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
//        hookContextMenu();
//        makeActions();
//        addActions();
//        fillToolBar();
//        hookDNDListeners();
        
//        getSite().getPage().addPartListener(linkWithEditorPartListener);
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
    


}
