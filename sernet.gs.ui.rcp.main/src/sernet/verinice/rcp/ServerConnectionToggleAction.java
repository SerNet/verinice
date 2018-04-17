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
package sernet.verinice.rcp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.Workbench;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.actions.RightsEnabledAction;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;
import sernet.gs.ui.rcp.main.service.ServiceFactory;
import sernet.verinice.interfaces.ActionRightIDs;
import sernet.verinice.interfaces.CommandException;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.iso27k.rcp.Mutex;
import sernet.verinice.model.common.CnATreeElement;
import sernet.verinice.service.commands.ExportCommand;
import sernet.verinice.service.commands.SyncParameter;
import sernet.verinice.service.sync.VeriniceArchive;

/**
 * ServerConnectionToggleAction is switching connection mode from server to standalone or vice versa.
 * {@link ServerConnectionToggleDialog} is opened to ask user which organizations is copied.
 * After that every selected organization is exported to a single VNA archive:
 * <CLIENT_WORKSPACE>/conf/client-server-transport-<N>.vna
 * Last step is a restarting the application. 
 * 
 * After restarting {@link StartupImporter} is importing the verinice archives from <CLIENT_WORKSPACE>/conf/.
 *
 * @author Daniel Murygin <dm[at]sernet[dot]de>
 */
@SuppressWarnings("restriction")
public class ServerConnectionToggleAction extends RightsEnabledAction {
    
    private static final Logger LOG = Logger.getLogger(ServerConnectionToggleAction.class);
    
    public static final String SOURCE_ID = "transfer"; //$NON-NLS-1$
    
    public static final String ID = "sernet.verinice.rcp.ServerConnectionToggleAction"; //$NON-NLS-1$
    
    private static ISchedulingRule iSchedulingRule = new Mutex();
    
    private String filePathBase;
    
    private ServerConnectionToggleDialog dialog;
    
    public ServerConnectionToggleAction() {
        super(ActionRightIDs.XMLEXPORT, Preferences.isServerMode() ? Messages.ServerConnectionToggleAction_0 : Messages.ServerConnectionToggleAction_1);
        setId(ID);
        StringBuilder sb = new StringBuilder();
        sb.append(CnAWorkspace.getInstance().getConfDir()).append(File.separatorChar).append(StartupImporter.SERVER_TRANSPORT_BASENAME);
        filePathBase = sb.toString();
    }

    /* (non-Javadoc)
     * @see sernet.gs.ui.rcp.main.actions.RightsEnabledAction#doRun()
     */
    @Override
    public void doRun() {       
        dialog = new ServerConnectionToggleDialog(Display.getCurrent().getActiveShell());    
        if( dialog.open() == Dialog.OK ) {
            String title = Messages.ServerConnectionToggleAction_2;
            if (Preferences.isStandalone()) {
                title = Messages.ServerConnectionToggleAction_3;
            }
            WorkspaceJob exportJob = new WorkspaceJob(title) {
                @Override
                public IStatus runInWorkspace(final IProgressMonitor monitor) {
                    IStatus status = Status.OK_STATUS;
                    try {
                        monitor.beginTask(Messages.ServerConnectionToggleAction_4, IProgressMonitor.UNKNOWN);
                        export();                      
                    } catch (Exception e) {
                        LOG.error("Error while exporting data.", e); //$NON-NLS-1$
                        status= new Status(Status.ERROR, "sernet.verinice.samt.rcp", Messages.ServerConnectionToggleAction_6,e);  //$NON-NLS-1$
                    } finally {
                        monitor.done();
                        this.done(status);
                    }
                    return status;
                }
            };
            exportJob.addJobChangeListener(new JobChangeAdapter() {                     
                /* (non-Javadoc)
                 * @see org.eclipse.core.runtime.jobs.JobChangeAdapter#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
                 */
                @Override
                public void done(IJobChangeEvent event) {
                    if(Status.OK_STATUS.equals(event.getResult())) {
                        restart();
                    }
                }
            });
            JobScheduler.scheduleJob(exportJob,iSchedulingRule);
        }
    }
    
    /**
     * Exports all scopes to one VNA and selects the scope-id which occurs
     * most often in scopes. or creates a new one if there is no scope id.
     * 
     * @throws CommandException
     * @throws IOException
     */
    private void export() throws CommandException, IOException {
        export(new ArrayList<CnATreeElement>(dialog.getSelectedElementSet()), 0); 
    }
    
    private void export(List<CnATreeElement> elementList,int i) throws CommandException, IOException {
        String sourceId = readSourceId(elementList);
        if(sourceId==null) {
            sourceId = SOURCE_ID;
        }
        Activator.inheritVeriniceContextState();
        ExportCommand exportCommand = new ExportCommand(elementList, sourceId, true, SyncParameter.EXPORT_FORMAT_VERINICE_ARCHIV);
        exportCommand = ServiceFactory.lookupCommandService().executeCommand(exportCommand);
        FileUtils.writeByteArrayToFile(new File(createFilePath(i)), exportCommand.getResult());      
    }

    /**
     * Returns source-id which occurs most often in {@link ElementListSelectionDialog#}
     * 
     * @param elementList A list with {@link CnATreeElement}s
     * @return A source id
     */
    private String readSourceId(List<CnATreeElement> elementList) {
        String sourceId = null;
        Map<String, Integer> sourceIdMap = new Hashtable<String, Integer>();
        for (CnATreeElement element : elementList) {
            String cur = element.getSourceId();
            if(cur!=null) {  
                Integer n = sourceIdMap.get(cur);
                if(n==null) {
                    n = 0;
                }
                n++;
                sourceIdMap.put(cur, n);
            }
        }
        int max = 0;
        for (String cur : sourceIdMap.keySet()) {
            int n = sourceIdMap.get(cur);
            if (n>max) {
                max=n;
                sourceId=cur;
            }
        }
        return sourceId;
    }

    /**
     * @param i
     * @return
     */
    private String createFilePath(int i) {
        StringBuilder sb = new StringBuilder(filePathBase);
        return sb.append("-").append(i).append(VeriniceArchive.EXTENSION_VERINICE_ARCHIVE).toString();
    }

    private void restart() {
        Display.getDefault().syncExec(new Runnable() {       
            @Override
            public void run() {
                IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();    
                
                if (Preferences.isStandalone()) {
                    prefs.setValue(PreferenceConstants.VNSERVER_URI, dialog.getServerUrl());
                }       
                
                String newMode = PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER;
                if (prefs.getString(PreferenceConstants.OPERATION_MODE).equals(PreferenceConstants.OPERATION_MODE_INTERNAL_SERVER)) {
                    newMode = PreferenceConstants.OPERATION_MODE_REMOTE_SERVER;
                } 
                prefs.setValue(PreferenceConstants.OPERATION_MODE, newMode);
                prefs.setValue(PreferenceConstants.RESTART, true);
                
                Workbench.getInstance().restart();
            }
        });      
    }
}
