/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak[at]sernet[dot]de>.
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
 *     Alexander Koderman <ak[at]sernet[dot]de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.actions;

import java.io.IOException;

import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.ProgressAdapter;
import sernet.verinice.iso27k.rcp.JobScheduler;
import sernet.verinice.model.bsi.BSIModel;
import sernet.verinice.rcp.StatusResult;

/**
 * 
 * 
 * @author koderman[at]sernet[dot]de
 * 
 */
public class BSIModelViewOpenDBAction extends Action {
	private Shell shell;

	private BsiModelView bsiView;

	public BSIModelViewOpenDBAction(BsiModelView bsiView, Viewer viewer) {
		super("Öffne Datenbankverbindung");
		this.bsiView = bsiView;
		shell = viewer.getControl().getShell();
		setToolTipText("Öffnet eine Verbindung zur konfigurierten Datenbank.");
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.DBCONNECT));
	}

	@Override
	public void run() {
		Activator.showDerbyWarning(shell);
		//CnAElementFactory.getInstance().closeModel();
		try {
			CnAWorkspace.getInstance().createDatabaseConfig();
		} catch (IllegalStateException e) {
			ExceptionUtil.log(e, "Fehler beim Aktualisieren der DB-Konfiguration.");		
			return;
		} catch (IOException ioe) {
			ExceptionUtil.log(ioe, "Fehler beim Aktualisieren der DB-Konfiguration.");		
			return;
		}
		
		// The methods below are using WorkspaceJobs. The order in which they
		// are run is guaranteed by the 'mutex' instance.
		
		StatusResult result = Activator.startServer();
		
		// Since the loading of the model depends on having a running server
		// a result object is returned which can be used by the worker that load
		// the model. By the time the worker is active the result object has
		// been set up.
		createModel(result);
	}

	private void createModel(final StatusResult serverStartResult) {
		WorkspaceJob job = new WorkspaceJob(Messages.BsiModelView_0) {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				// If server could not be started for whatever reason do not try to
				// load the model either.
				if (serverStartResult.status == Status.CANCEL_STATUS) {
					return Status.CANCEL_STATUS;
				}
				
				Activator.inheritVeriniceContextState();
				
				try {
					monitor.beginTask("Starte OR-Mapper...", IProgressMonitor.UNKNOWN);
					monitor.setTaskName("Starte OR-Mapper...");
					BSIModel model = CnAElementFactory.getInstance().loadOrCreateModel(new ProgressAdapter(monitor));
					bsiView.setModel(model);
				} catch (RuntimeException re) {
					ExceptionUtil.log(re, "Konnte keine Verbindung zur Datenbank herstellen.");
				} catch (Exception e) {
					ExceptionUtil.log(e, "Konnte keine Verbindung zur Datenbank herstellen.");
				}
				return Status.OK_STATUS;
			}
		};
		job.setRule(JobScheduler.getInitMutex());
		job.setUser(true);
		job.schedule();
	}

}
