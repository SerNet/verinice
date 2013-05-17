/*******************************************************************************
 * Copyright (c) 2009 Daniel Murygin <dm[at]sernet[dot]de>.
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *     This program is distributed in the hope that it will be useful,    
 * but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU Lesser General Public License for more details.
 *     You should have received a copy of the GNU Lesser General Public 
 * License along with this program. 
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Daniel <dm[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.verinice.samt.rcp;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import sernet.verinice.iso27k.rcp.ISMView;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.action.HideEmptyFilter;
import sernet.verinice.model.common.TypeParameter;
import sernet.verinice.model.iso27k.Audit;
import sernet.verinice.model.iso27k.AuditGroup;
import sernet.verinice.model.iso27k.Control;
import sernet.verinice.model.iso27k.ControlGroup;
import sernet.verinice.model.samt.SamtTopic;
import sernet.verinice.rcp.IAttachedToPerspective;

/**
 * Main view for Self-Assessments (SAMT)
 * 
 * This is a extended version of the {@link ISMView} which reduces 
 * the functionality of it's bas class.
 * 
 * @author Daniel Murygin <dm[at]sernet[dot]de> 
 */
@SuppressWarnings("restriction")
public class SamtView extends ISMView implements IAttachedToPerspective  {

    private static final Logger LOG = Logger.getLogger(SamtView.class);

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "sernet.verinice.samt.rcp.views.SamtView"; //$NON-NLS-1$

    
    private AddISAToOrganisation addIsaAction;
    
    /**
     * The constructor.
     */
    public SamtView() {
        super();
    }

    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ISMView#initView(org.eclipse.swt.widgets.Composite)
     */
    protected void initView(Composite parent) {
        super.initView(parent);
        makeActions();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.iso27k.rcp.ISMView#initData()
     */
    @Override
    protected void initData() {
        super.initData();
        WorkspaceJob initDataJob = new WorkspaceJob(Messages.SamtView_1) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) {
                IStatus status = Status.OK_STATUS;
                try {
                    monitor.beginTask(Messages.SamtView_2, IProgressMonitor.UNKNOWN);
                    Display.getDefault().syncExec(new Runnable() {
                        public void run() {
                            expand();
                        }
                    });
                } catch (Exception e) {
                    LOG.error("Error while expanding tree", e); //$NON-NLS-1$
                    status = new Status(IStatus.ERROR, "sernet.verinice.samt.rcp", Messages.SamtView_4, e); //$NON-NLS-1$
                } finally {
                    monitor.done();
                }
                return status;
            }
        };
        if(Activator.getDefault().getPreferenceStore().getBoolean(SamtPreferencePage.EXPAND_ISA)) {      
            JobScheduler.scheduleInitJob(initDataJob);
        }
    }
    
    private void makeActions() {
        addIsaAction = new AddISAToOrganisation(getViewSite().getWorkbenchWindow());
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ISMView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    protected void fillContextMenu(IMenuManager manager) {
        super.fillContextMenu(manager);
        manager.add(new Separator());
        manager.add(new GroupMarker("workflow")); 
        manager.appendToGroup("workflow",addIsaAction);
    }
    
    protected void expand() {
        final int maxExpandLvl = 5;
        viewer.expandToLevel(maxExpandLvl);
    }

    /*
     * (non-Javadoc)
     * 
     * @see sernet.verinice.iso27k.rcp.ISMView#createHideEmptyFilter()
     */
    @Override
    protected HideEmptyFilter createHideEmptyFilter() {
        HideEmptyFilter filter = new HideEmptyFilter(viewer);
        filter.setHideEmpty(true);
        return filter;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.iso27k.rcp.ISMView#createTypeFilter()
     */
    @Override
    protected TypeParameter createTypeParameter() {
        TypeParameter filter = new TypeParameter();
        filter.addType(new String[]{Audit.TYPE_ID,AuditGroup.TYPE_ID});
        filter.addType(new String[]{Control.TYPE_ID,ControlGroup.TYPE_ID});
        filter.addType(new String[]{SamtTopic.TYPE_ID,ControlGroup.TYPE_ID});
        return filter;
    }
    
    /* (non-Javadoc)
     * @see sernet.verinice.rcp.IAttachedToPerspective#getPerspectiveId()
     */
    public String getPerspectiveId() {
        return SamtPerspective.ID;
    }

}