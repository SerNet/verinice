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

import javax.security.auth.callback.ConfirmationCallback;

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
import org.eclipse.ui.PlatformUI;

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
import sernet.gs.ui.rcp.main.common.model.CnAElementFactory;
import sernet.gs.ui.rcp.main.common.model.CnAElementHome;

/**
 * @author koderman@sernet.de
 *
 */
public class BSIModelViewCloseDBAction extends Action {
	private Shell shell;
	private BsiModelView bsiView;

	public BSIModelViewCloseDBAction(BsiModelView bsiView, Viewer viewer) {
		super("Schließe Datenbankverbindung");
		this.bsiView = bsiView;
		shell = viewer.getControl().getShell();
		setToolTipText("Schließt die Verbindung zur eingestellten Datenbank.");
		setImageDescriptor(ImageCache.getInstance().getImageDescriptor(ImageCache.DBCLOSE));
	}
	
	@Override
	public void run() {
		if (CnAElementHome.getInstance().isOpen()) {
			boolean confirm = MessageDialog.openConfirm(shell, "DB schließen?", 
			"Verbindung zur Datenbank wirklich schließen?");
			if (confirm) {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().closeAllEditors(true /* ask save */);
				CnAElementFactory.getInstance().closeModel();
				bsiView.setNullModel();
			}
		}
	}
}
