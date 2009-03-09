/*******************************************************************************
 * Copyright (c) 2009 Alexander Koderman <ak@sernet.de>.
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
 *     Alexander Koderman <ak@sernet.de> - initial API and implementation
 ******************************************************************************/
package sernet.gs.ui.rcp.main.bsi.views.actions;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import sernet.gs.ui.rcp.main.Activator;
import sernet.gs.ui.rcp.main.CnAWorkspace;
import sernet.gs.ui.rcp.main.ExceptionUtil;
import sernet.gs.ui.rcp.main.ImageCache;
import sernet.gs.ui.rcp.main.bsi.dialogs.MassnahmenViewFilterDialog;
import sernet.gs.ui.rcp.main.bsi.filter.BSISchichtFilter;
import sernet.gs.ui.rcp.main.bsi.filter.BSISearchFilter;
import sernet.gs.ui.rcp.main.bsi.filter.MassnahmenSiegelFilter;
import sernet.gs.ui.rcp.main.bsi.model.BSIModel;
import sernet.gs.ui.rcp.main.bsi.model.BausteinUmsetzung;
import sernet.gs.ui.rcp.main.bsi.model.MassnahmenUmsetzung;
import sernet.gs.ui.rcp.main.bsi.views.BsiModelView;
import sernet.gs.ui.rcp.main.bsi.views.Messages;
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;
import sernet.gs.ui.rcp.main.common.model.IProgress;
import sernet.gs.ui.rcp.main.common.model.ProgressAdapter;
import sernet.gs.ui.rcp.main.preferences.PreferenceConstants;

/**
 * 
 * 
 * @author koderman@sernet.de
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
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(
				ImageCache.DBCONNECT));
	}

	@Override
	public void run() {
		showDerbyWarning();
		//CnAElementFactory.getInstance().closeModel();
		try {
			CnAWorkspace.getInstance().createDatabaseConfig();
		} catch (Exception e) {
			ExceptionUtil.log(e,
					"Fehler beim Aktualisieren der DB-Konfiguration.");
		}
		createModel();
	}

	private void showDerbyWarning() {
		if (Activator.getDefault().getPluginPreferences().getBoolean(
				PreferenceConstants.FIRSTSTART)
				&& Activator.getDefault().getPluginPreferences().getString(
				PreferenceConstants.DB_DRIVER).equals(
				PreferenceConstants.DB_DRIVER_DERBY)
				) {

			MessageDialog
					.openInformation(
							new Shell(shell),
							"Datenbank nicht konfiguriert",
							"HINWEIS: Sie haben keine Datenbank konfiguriert. "
									+ "Verinice verwendet die integrierte "
									+ "Derby-Datenbank. Alternativ können Sie in den "
									+ "Einstellungen eine externe Datenbank angeben (Postgres / MySQL).\n\n"
									+ "Dieser Hinweis wird nicht erneut angezeigt.");
			
			Activator.getDefault().getPluginPreferences().setValue(PreferenceConstants.FIRSTSTART,
					false);
		}
	}
	

	private void createModel() {
		WorkspaceJob job = new WorkspaceJob(Messages.BsiModelView_0) {
			public IStatus runInWorkspace(final IProgressMonitor monitor) {
				try {
					monitor.beginTask("Starte OR-Mapper...", IProgressMonitor.UNKNOWN);
					monitor.setTaskName("Starte OR-Mapper...");
					BSIModel model = CnAElementFactory.getInstance()
							.loadOrCreateModel(new ProgressAdapter(monitor));
					bsiView.setModel(model);
				} catch (RuntimeException re) {
					ExceptionUtil
							.log(re,
									"Konnte keine Verbindung zur Datenbank herstellen.");
				} catch (Exception e) {
					ExceptionUtil
							.log(e,
									"Konnte keine Verbindung zur Datenbank herstellen.");
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

}
